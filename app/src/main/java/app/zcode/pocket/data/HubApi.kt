package app.zcode.pocket.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class HubException(message: String) : Exception(message)

/**
 * The hub's REST surface (docs/REMOTE-CLIENTS.md). Observation endpoints only —
 * every call is a GET except session-create. The token guards all of them.
 */
class HubApi(private val baseUrl: String, private val token: String) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(40, TimeUnit.SECONDS)
        .build()

    private val base = baseUrl.trimEnd('/')
    private val jsonType = "application/json; charset=utf-8".toMediaType()

    private suspend fun get(path: String): String = withContext(Dispatchers.IO) {
        val req = Request.Builder().url(base + path)
            .header("Authorization", "Bearer $token")
            .build()
        client.newCall(req).execute().use { resp ->
            val body = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) throw HubException("HTTP ${resp.code} ${body.take(160).trim()}")
            body
        }
    }

    private suspend fun post(path: String, bodyJson: String): String = withContext(Dispatchers.IO) {
        val req = Request.Builder().url(base + path)
            .header("Authorization", "Bearer $token")
            .post(bodyJson.toRequestBody(jsonType))
            .build()
        client.newCall(req).execute().use { resp ->
            val body = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) throw HubException("HTTP ${resp.code} ${body.take(160).trim()}")
            body
        }
    }

    suspend fun health(): Boolean =
        try { get("/api/health").trim().startsWith("ok") } catch (_: Exception) { false }

    suspend fun instances(probe: Boolean = true): List<HubInstance> =
        Parse.instances(JSONArray(get("/api/instances" + if (probe) "?probe=1" else "")))

    suspend fun projects(): List<ProjectRow> = Parse.projects(JSONArray(get("/api/projects")))

    suspend fun projectSessions(workspacePath: String): ProjectSessions =
        Parse.projectSessions(JSONObject(get("/api/projects/sessions?workspacePath=" + enc(workspacePath))))

    /** Starts a session-create on the machine; returns the NEW instance id. */
    suspend fun createInstance(workspacePath: String, sessionId: String? = null): String {
        val body = JSONObject().put("workspacePath", workspacePath).apply { if (sessionId != null) put("sessionId", sessionId) }
        val r = JSONObject(post("/api/instances", body.toString()))
        val id = r.optString("id")
        if (id.isEmpty()) throw HubException("create returned no instance id")
        return id
    }

    suspend fun agents(): List<AgentRow> =
        Parse.agents(JSONObject(get("/api/settings/agents")).optJSONArray("agents"))

    suspend fun quota(): JSONObject = JSONObject(get("/api/quota"))

    suspend fun workflowOverview(): JSONObject = JSONObject(get("/api/workflow-overview"))

    private fun enc(s: String): String = URLEncoder.encode(s, "UTF-8")
}
