package app.zcode.pocket.data

import org.json.JSONArray
import org.json.JSONObject

// REST payload models for the hub. Fields are additive-only in the hub's
// contract, so every parser tolerates missing keys.

data class HubInstance(
    val id: String,
    val workspace: String,
    val origin: String,
    val sessions: List<HubSession>,
)

data class HubSession(
    val sessionId: String,
    val title: String?,
    val status: String?,
    val updatedAt: Long,
)

data class ProjectRow(val workspacePath: String, val sessions: Int, val lastActive: Long)

data class ProjectSessions(val instanceId: String?, val sessions: List<ProjectSessionRow>)

data class ProjectSessionRow(
    val sessionId: String,
    val title: String?,
    val updatedAt: String?,
    val live: Boolean,
    val running: Boolean,
)

data class AgentRow(
    val name: String,
    val description: String,
    val model: String?,
    val thoughtLevel: String?,
    val enabled: Boolean,
    val readOnly: Boolean,
)

object Parse {
    fun instances(arr: JSONArray): List<HubInstance> = (0 until arr.length()).mapNotNull { i ->
        val o = arr.optJSONObject(i) ?: return@mapNotNull null
        val sess = o.optJSONArray("sessions") ?: JSONArray()
        HubInstance(
            id = o.optString("id"),
            workspace = o.optString("workspace"),
            origin = o.optString("origin", "editor"),
            sessions = (0 until sess.length()).mapNotNull { j ->
                val s = sess.optJSONObject(j) ?: return@mapNotNull null
                HubSession(
                    sessionId = s.optString("sessionId"),
                    title = s.optString("title").ifEmpty { null },
                    status = s.optString("status").ifEmpty { null },
                    updatedAt = s.optLong("updatedAt"),
                )
            },
        )
    }

    fun projects(arr: JSONArray): List<ProjectRow> = (0 until arr.length()).mapNotNull { i ->
        val o = arr.optJSONObject(i) ?: return@mapNotNull null
        ProjectRow(o.optString("workspacePath"), o.optInt("sessions"), o.optLong("lastActive"))
    }

    fun projectSessions(o: JSONObject): ProjectSessions {
        val inst = o.optJSONObject("instance")
        val arr = o.optJSONArray("sessions") ?: JSONArray()
        return ProjectSessions(
            instanceId = inst?.optString("id")?.ifEmpty { null },
            sessions = (0 until arr.length()).mapNotNull { i ->
                val s = arr.optJSONObject(i) ?: return@mapNotNull null
                ProjectSessionRow(
                    sessionId = s.optString("sessionId"),
                    title = s.optString("title").ifEmpty { null },
                    updatedAt = s.optString("updatedAt").ifEmpty { null },
                    live = s.optBoolean("live"),
                    running = s.optBoolean("running"),
                )
            },
        )
    }

    fun agents(arr: JSONArray?): List<AgentRow> = if (arr == null) emptyList() else (0 until arr.length()).mapNotNull { i ->
        val o = arr.optJSONObject(i) ?: return@mapNotNull null
        val fm = o.optJSONObject("frontmatter") ?: JSONObject()
        AgentRow(
            name = o.optString("name"),
            description = fm.optString("description").ifEmpty { o.optString("description") },
            model = fm.optString("model").ifEmpty { null },
            thoughtLevel = fm.optString("thoughtLevel").ifEmpty { null },
            enabled = o.optBoolean("enabled", true),
            readOnly = o.optBoolean("readOnly", false),
        )
    }
}

/** "C:\0-BlackBoxProject-0\zcode-setup" -> "zcode-setup" (either separator). */
fun workspaceName(path: String): String =
    path.trimEnd('\\', '/').substringAfterLast('\\').substringAfterLast('/').ifEmpty { path }
