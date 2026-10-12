package app.zcode.pocket.protocol

import org.json.JSONArray
import org.json.JSONObject

/**
 * The ACP-over-WebSocket protocol as the zcode-acp hub speaks it.
 *
 * Written against real captured frames (see src/test/resources/frames.jsonl),
 * not against guesses: every update shape here was observed live on 2026-10-12.
 * Pure Kotlin + org.json — no Android types — so the whole protocol surface is
 * unit-testable on the JVM.
 */

data class ConfigOption(
    val id: String,
    val name: String,
    val category: String,
    val currentValue: String,
    val options: List<Option>,
) {
    data class Option(val value: String, val name: String)

    /** "Grok Build (OAuth) › grok-4.7" -> "grok-4.7" for chip display. */
    val shortValue: String
        get() = options.firstOrNull { it.value == currentValue }
            ?.name?.substringAfterLast('\u203a')?.trim()
            ?.takeIf { it.isNotEmpty() } ?: currentValue
}

data class SlashCommand(val name: String, val description: String)
data class PermOption(val optionId: String, val name: String, val kind: String?)

sealed interface ServerFrame {
    data class Response(val id: Any, val result: JSONObject?, val error: JSONObject?) : ServerFrame
    data class Notification(val method: String, val params: JSONObject?) : ServerFrame
    data class ServerRequest(val id: Any, val method: String, val params: JSONObject?) : ServerFrame
}

sealed interface AcpEvent {
    data class MessageChunk(val messageId: String?, val text: String) : AcpEvent
    data class ThoughtChunk(val messageId: String?, val text: String) : AcpEvent
    data class UserEcho(val messageId: String?, val text: String) : AcpEvent
    data class ToolStart(val toolCallId: String, val title: String, val kind: String?, val status: String) : AcpEvent
    data class ToolUpdate(val toolCallId: String, val status: String?, val title: String?, val detail: String?) : AcpEvent
    data class PlanUpdate(val entries: List<String>) : AcpEvent
    data class UsageUpdate(val used: Long, val size: Long) : AcpEvent
    data class SessionInfo(val title: String?) : AcpEvent
    data class ConfigOptions(val options: List<ConfigOption>) : AcpEvent
    data class CommandsUpdate(val commands: List<SlashCommand>) : AcpEvent
    data class TurnState(val running: Boolean) : AcpEvent
    data class PermissionRequest(val reqId: Any, val title: String, val options: List<PermOption>) : AcpEvent
    data class PermissionCancelled(val reqId: Any?) : AcpEvent
    data class TurnEnded(val stopReason: String, val usage: JSONObject?) : AcpEvent
    data class Unknown(val kind: String) : AcpEvent
}

object AcpProtocol {

    // ---- parsing -----------------------------------------------------------

    fun parseServerFrame(text: String): ServerFrame? {
        val obj = try { JSONObject(text) } catch (_: Exception) { return null }
        val id = obj.opt("id")
        val method = obj.optString("method", "")
        val hasResult = obj.has("result")
        val hasError = obj.has("error")
        return when {
            id != null && method.isNotEmpty() -> ServerFrame.ServerRequest(id, method, obj.optJSONObject("params"))
            id != null && (hasResult || hasError) -> ServerFrame.Response(
                id,
                obj.optJSONObject("result") ?: (if (hasResult) JSONObject() else null),
                obj.optJSONObject("error"),
            )
            method.isNotEmpty() -> ServerFrame.Notification(method, obj.optJSONObject("params"))
            else -> null
        }
    }

    fun toEvents(frame: ServerFrame.Notification): List<AcpEvent> {
        if (frame.method == "$/zcode/turnState") {
            return listOf(AcpEvent.TurnState(frame.params?.optBoolean("running") == true))
        }
        if (frame.method == "$/cancel_request") {
            val p = frame.params
            return listOf(AcpEvent.PermissionCancelled(p?.opt("requestId")))
        }
        if (frame.method != "session/update") return listOf(AcpEvent.Unknown(frame.method))
        val update = frame.params?.optJSONObject("update") ?: return emptyList()
        return when (val kind = update.optString("sessionUpdate", "")) {
            "agent_message_chunk" -> listOf(
                AcpEvent.MessageChunk(update.optString("messageId").ifEmpty { null }, textOf(update)),
            )
            "agent_thought_chunk" -> listOf(
                AcpEvent.ThoughtChunk(update.optString("messageId").ifEmpty { null }, textOf(update)),
            )
            "user_message_chunk" -> listOf(
                AcpEvent.UserEcho(update.optString("messageId").ifEmpty { null }, textOf(update)),
            )
            "tool_call" -> listOf(
                AcpEvent.ToolStart(
                    toolCallId = update.optString("toolCallId"),
                    title = update.optString("title").ifEmpty { "tool call" },
                    kind = update.optString("kind").ifEmpty { null },
                    status = update.optString("status").ifEmpty { "pending" },
                ),
            )
            "tool_call_update" -> listOf(
                AcpEvent.ToolUpdate(
                    toolCallId = update.optString("toolCallId"),
                    status = update.optString("status").ifEmpty { null },
                    title = update.optString("title").ifEmpty { null },
                    detail = detailOf(update),
                ),
            )
            "plan" -> {
                val entries = update.optJSONArray("entries") ?: JSONArray()
                listOf(AcpEvent.PlanUpdate((0 until entries.length()).map { entries.optJSONObject(it)?.optString("content") ?: entries.optString(it) }))
            }
            "usage_update" -> listOf(
                AcpEvent.UsageUpdate(update.optLong("used"), update.optLong("size")),
            )
            "session_info_update" -> listOf(
                AcpEvent.SessionInfo(update.optString("title").ifEmpty { null }),
            )
            "config_option_update" -> listOf(
                AcpEvent.ConfigOptions(parseConfigOptions(update.optJSONArray("configOptions"))),
            )
            "available_commands_update" -> {
                val arr = update.optJSONArray("availableCommands") ?: JSONArray()
                listOf(AcpEvent.CommandsUpdate((0 until arr.length()).mapNotNull { i ->
                    val c = arr.optJSONObject(i) ?: return@mapNotNull null
                    SlashCommand(c.optString("name"), c.optString("description"))
                }))
            }
            else -> listOf(AcpEvent.Unknown(kind))
        }
    }

    fun toEvent(frame: ServerFrame.ServerRequest): AcpEvent = when (frame.method) {
        "session/request_permission" -> {
            val p = frame.params
            val tool = p?.optJSONObject("toolCall")?.optString("title").orEmpty().ifEmpty { "Permission required" }
            val arr = p?.optJSONArray("options") ?: JSONArray()
            val options = (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                PermOption(o.optString("optionId"), o.optString("name").ifEmpty { o.optString("optionId") }, o.optString("kind").ifEmpty { null })
            }
            AcpEvent.PermissionRequest(frame.id, tool, options)
        }
        else -> AcpEvent.Unknown(frame.method)
    }

    private fun textOf(update: JSONObject): String {
        val content = update.optJSONObject("content") ?: return ""
        return content.optString("text")
    }

    /** tool_call_update carries the output in `content[].content.text` (observed). */
    private fun detailOf(update: JSONObject): String? {
        val out = update.optString("rawOutput").ifEmpty { null }
        if (out != null) return out.take(4000)
        val arr = update.optJSONArray("content") ?: return null
        val sb = StringBuilder()
        for (i in 0 until arr.length()) {
            val c = arr.optJSONObject(i) ?: continue
            val inner = c.optJSONObject("content") ?: c
            val t = inner.optString("text")
            if (t.isNotEmpty()) sb.append(t)
        }
        return sb.toString().take(4000).ifEmpty { null }
    }

    fun parseConfigOptions(arr: JSONArray?): List<ConfigOption> {
        if (arr == null) return emptyList()
        return (0 until arr.length()).mapNotNull { i ->
            val o = arr.optJSONObject(i) ?: return@mapNotNull null
            val opts = o.optJSONArray("options") ?: JSONArray()
            ConfigOption(
                id = o.optString("id"),
                name = o.optString("name"),
                category = o.optString("category"),
                currentValue = o.optString("currentValue"),
                options = (0 until opts.length()).mapNotNull { j ->
                    val v = opts.optJSONObject(j) ?: return@mapNotNull null
                    ConfigOption.Option(v.optString("value"), v.optString("name"))
                },
            )
        }
    }

    // ---- client -> server messages ----------------------------------------

    fun initializeRequest(id: Int): String = JSONObject()
        .put("jsonrpc", "2.0").put("id", id).put("method", "initialize")
        .put("params", JSONObject()
            .put("protocolVersion", 1) // MUST be the number 1 (REMOTE-CLIENTS.md)
            .put("clientCapabilities", JSONObject().put("fs", JSONObject().put("readTextFile", true).put("writeTextFile", true)))
            .put("clientInfo", JSONObject().put("name", "zcode-pocket").put("version", "1.0.0")))
        .toString()

    fun request(id: Int, method: String, params: JSONObject): String =
        JSONObject().put("jsonrpc", "2.0").put("id", id).put("method", method).put("params", params).toString()

    fun notification(method: String, params: JSONObject): String =
        JSONObject().put("jsonrpc", "2.0").put("method", method).put("params", params).toString()

    fun response(id: Any, result: JSONObject): String =
        JSONObject().put("jsonrpc", "2.0").put("id", id).put("result", result).toString()

    fun newSessionParams(cwd: String?): JSONObject = JSONObject().apply {
        if (!cwd.isNullOrBlank()) put("cwd", cwd)
        put("mcpServers", JSONArray())
    }

    fun loadParams(sessionId: String, cwd: String?): JSONObject = JSONObject().apply {
        put("sessionId", sessionId)
        if (!cwd.isNullOrBlank()) put("cwd", cwd)
        put("mcpServers", JSONArray()) // required by the SDK params schema, even empty
    }

    fun promptParams(sessionId: String, text: String): JSONObject = JSONObject()
        .put("sessionId", sessionId)
        .put("prompt", JSONArray().put(JSONObject().put("type", "text").put("text", text)))

    fun cancelParams(sessionId: String): JSONObject = JSONObject().put("sessionId", sessionId)

    fun setConfigParams(sessionId: String, configId: String, value: String): JSONObject = JSONObject()
        .put("sessionId", sessionId).put("configId", configId).put("value", value)

    fun permissionResponseParams(optionId: String): JSONObject = JSONObject()
        .put("outcome", JSONObject().put("outcome", "selected").put("optionId", optionId))
}
