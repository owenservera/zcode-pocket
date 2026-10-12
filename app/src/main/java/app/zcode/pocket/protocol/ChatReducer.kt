package app.zcode.pocket.protocol

import org.json.JSONObject

/**
 * The chat state machine. Pure: events in, state out, no Android and no I/O,
 * so the whole conversation model is unit-tested against real captured frames
 * (ChatReducerTest) rather than only exercised on a device.
 */

sealed interface TimelineItem {
    data class Message(val id: String, val role: Role, val text: String, val streaming: Boolean) : TimelineItem
    data class Tool(val toolCallId: String, val title: String, val kind: String?, val status: String, val detail: String?) : TimelineItem
    data class Plan(val entries: List<String>) : TimelineItem
}

enum class Role { USER, ASSISTANT, THOUGHT, TURNINFO, ERROR }

enum class Phase { Connecting, Ready, Running, Disconnected, Failed }

data class Usage(val used: Long, val size: Long) {
    val fraction: Float get() = if (size > 0L) (used.toFloat() / size.toFloat()).coerceIn(0f, 1f) else 0f
}

data class Permission(val reqId: Any, val title: String, val options: List<PermOption>)

data class ChatState(
    val phase: Phase = Phase.Connecting,
    val sessionId: String? = null,
    val title: String? = null,
    val timeline: List<TimelineItem> = emptyList(),
    val permission: Permission? = null,
    val usage: Usage? = null,
    val configOptions: List<ConfigOption> = emptyList(),
    val commands: List<SlashCommand> = emptyList(),
    val lastStopReason: String? = null,
    val error: String? = null,
) {
    val isRunning: Boolean get() = phase == Phase.Running
    val mode: ConfigOption? get() = configOptions.firstOrNull { it.category == "mode" }
    val model: ConfigOption? get() = configOptions.firstOrNull { it.category == "model" }
}

object ChatReducer {

    fun reduce(state: ChatState, event: AcpEvent): ChatState = when (event) {
        is AcpEvent.MessageChunk ->
            appendChunk(state, event.messageId, if (event.messageId?.startsWith("turninfo_") == true) Role.TURNINFO else Role.ASSISTANT, event.text)

        is AcpEvent.ThoughtChunk -> appendChunk(state, event.messageId, Role.THOUGHT, event.text)
        is AcpEvent.UserEcho -> appendChunk(state, event.messageId, Role.USER, event.text)

        is AcpEvent.ToolStart -> {
            val existing = state.timeline.indexOfFirst { it is TimelineItem.Tool && it.toolCallId == event.toolCallId }
            val item = TimelineItem.Tool(event.toolCallId, event.title, event.kind, event.status, null)
            state.copy(timeline = if (existing >= 0) state.timeline.toMutableList().also { it[existing] = item } else state.timeline + item)
        }

        is AcpEvent.ToolUpdate -> {
            val idx = state.timeline.indexOfFirst { it is TimelineItem.Tool && it.toolCallId == event.toolCallId }
            if (idx < 0) {
                state.copy(timeline = state.timeline + TimelineItem.Tool(event.toolCallId, event.title ?: "tool call", null, event.status ?: "pending", event.detail))
            } else {
                val old = state.timeline[idx] as TimelineItem.Tool
                val item = old.copy(
                    title = event.title ?: old.title,
                    status = event.status ?: old.status,
                    detail = event.detail ?: old.detail,
                )
                state.copy(timeline = state.timeline.toMutableList().also { it[idx] = item })
            }
        }

        is AcpEvent.PlanUpdate ->
            // An empty plan is what the bridge sends when there is nothing to
            // show; rendering an empty card would be noise.
            if (event.entries.isEmpty()) state
            else state.copy(timeline = state.timeline.filterNot { it is TimelineItem.Plan } + TimelineItem.Plan(event.entries))

        is AcpEvent.UsageUpdate -> state.copy(usage = Usage(event.used, event.size))

        is AcpEvent.SessionInfo -> state.copy(title = event.title ?: state.title)

        is AcpEvent.ConfigOptions -> {
            val merged = state.configOptions.toMutableList()
            for (o in event.options) {
                val i = merged.indexOfFirst { it.id == o.id }
                if (i >= 0) merged[i] = o else merged.add(o)
            }
            state.copy(configOptions = merged)
        }

        is AcpEvent.CommandsUpdate -> state.copy(commands = event.commands)

        is AcpEvent.TurnState -> state.copy(
            phase = if (event.running) Phase.Running else if (state.phase == Phase.Running) Phase.Ready else state.phase,
        )

        is AcpEvent.PermissionRequest -> state.copy(
            permission = Permission(event.reqId, event.title, event.options),
        )

        is AcpEvent.PermissionCancelled -> {
            val p = state.permission ?: return state
            if (event.reqId == null || event.reqId.toString() == p.reqId.toString()) state.copy(permission = null) else state
        }

        is AcpEvent.TurnEnded -> state.copy(
            phase = if (state.phase == Phase.Failed) Phase.Failed else Phase.Ready,
            lastStopReason = event.stopReason,
            permission = null,
            timeline = state.timeline.map { if (it is TimelineItem.Message) it.copy(streaming = false) else it },
        )

        is AcpEvent.Unknown -> state
    }

    /** Test/utility helper: parse and reduce a batch of raw server frames. */
    fun reduceFrames(state: ChatState, frames: List<String>): ChatState {
        var s = state
        for (raw in frames) {
            when (val f = AcpProtocol.parseServerFrame(raw)) {
                is ServerFrame.Notification -> AcpProtocol.toEvents(f).forEach { s = reduce(s, it) }
                is ServerFrame.ServerRequest -> s = reduce(s, AcpProtocol.toEvent(f))
                is ServerFrame.Response -> {
                    val r = f.result
                    if (r != null) {
                        // session/new result carries modes + configOptions.
                        AcpProtocol.parseConfigOptions(r.optJSONArray("configOptions")).takeIf { it.isNotEmpty() }
                            ?.let { s = reduce(s, AcpEvent.ConfigOptions(it)) }
                    }
                }
                null -> {}
            }
        }
        return s
    }

    /** Turn-end knowledge from a `session/prompt` response object. */
    fun endedFromPromptResponse(response: JSONObject): AcpEvent.TurnEnded =
        AcpEvent.TurnEnded(response.optString("stopReason", "end_turn"), response.optJSONObject("usage"))

    private fun appendChunk(state: ChatState, messageId: String?, role: Role, text: String): ChatState {
        if (text.isEmpty()) return state
        // Find the newest message of this id+role and append; messageIds are
        // stable per message, and a turninfo footer has its own id.
        val idx = state.timeline.indexOfLast { it is TimelineItem.Message && it.role == role && (messageId == null || it.id == messageId) }
        return if (idx >= 0) {
            val old = state.timeline[idx] as TimelineItem.Message
            val updated = old.copy(text = old.text + text, streaming = true)
            state.copy(timeline = state.timeline.toMutableList().also { it[idx] = updated })
        } else {
            state.copy(timeline = state.timeline + TimelineItem.Message(messageId ?: "msg_${state.timeline.size}", role, text, streaming = true))
        }
    }
}
