package app.zcode.pocket.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.zcode.pocket.data.AcpClient
import app.zcode.pocket.data.AgentRow
import app.zcode.pocket.data.HubApi
import app.zcode.pocket.data.HubInstance
import app.zcode.pocket.data.Prefs
import app.zcode.pocket.data.ProjectRow
import app.zcode.pocket.data.ProjectSessions
import app.zcode.pocket.data.workspaceName
import app.zcode.pocket.protocol.AcpEvent
import app.zcode.pocket.protocol.AcpProtocol
import app.zcode.pocket.protocol.ChatReducer
import app.zcode.pocket.protocol.ChatState
import app.zcode.pocket.protocol.Phase
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URLEncoder

data class HubUiState(
    val loading: Boolean = false,
    val instances: List<HubInstance> = emptyList(),
    val projects: List<ProjectRow> = emptyList(),
    val agents: List<AgentRow> = emptyList(),
    val quota: JSONObject? = null,
    val workflows: JSONObject? = null,
    val error: String? = null,
    val lastRefresh: Long = 0L,
)

data class CreateState(val busy: Boolean = false, val message: String? = null)

data class ChatRequest(val instanceId: String, val sessionId: String?, val cwd: String?, val newSession: Boolean)

class PocketViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = Prefs(app)

    private val _hub = MutableStateFlow(HubUiState())
    val hub: StateFlow<HubUiState> = _hub.asStateFlow()

    private val _chat = MutableStateFlow(ChatState(phase = Phase.Ready))
    val chat: StateFlow<ChatState> = _chat.asStateFlow()

    private val _create = MutableStateFlow(CreateState())
    val create: StateFlow<CreateState> = _create.asStateFlow()

    private val _projectSessions = MutableStateFlow<Map<String, ProjectSessions>>(emptyMap())
    val projectSessions: StateFlow<Map<String, ProjectSessions>> = _projectSessions.asStateFlow()

    private var acp: AcpClient? = null
    private var pollJob: Job? = null
    private var currentChat: ChatRequest? = null

    val hubUrl: String get() = prefs.hubUrl
    val token: String get() = prefs.token
    val configured: Boolean get() = prefs.token.isNotBlank()

    fun saveConfig(url: String, token: String) {
        prefs.hubUrl = url
        prefs.token = token
        refresh()
    }

    private fun api(): HubApi = HubApi(prefs.hubUrl, prefs.token)

    // ---- hub polling -------------------------------------------------------

    fun startPolling() {
        if (pollJob?.isActive == true) return
        pollJob = viewModelScope.launch {
            while (isActive) {
                refresh()
                delay(4000)
            }
        }
    }

    fun stopPolling() {
        pollJob?.cancel()
        pollJob = null
    }

    fun refresh() {
        if (prefs.token.isBlank()) return
        viewModelScope.launch {
            _hub.update { it.copy(loading = true) }
            try {
                val api = api()
                val instances = async { api.instances() }
                val projects = async { api.projects() }
                val agents = async { api.agents() }
                val quota = async { runCatching { api.quota() }.getOrNull() }
                val workflows = async { runCatching { api.workflowOverview() }.getOrNull() }
                _hub.update {
                    it.copy(
                        loading = false,
                        error = null,
                        instances = instances.await(),
                        projects = projects.await(),
                        agents = agents.await(),
                        quota = quota.await(),
                        workflows = workflows.await(),
                        lastRefresh = System.currentTimeMillis(),
                    )
                }
            } catch (e: Exception) {
                _hub.update { it.copy(loading = false, error = e.message ?: "hub unreachable") }
            }
        }
    }

    // ---- chat --------------------------------------------------------------

    fun openChat(req: ChatRequest) {
        currentChat = req
        acp?.close()
        _chat.value = ChatState(phase = Phase.Connecting)
        val wsUrl = prefs.hubUrl.trimEnd('/')
            .replaceFirst("https", "wss")
            .replaceFirst("http", "ws") +
            "/acp?instance=${req.instanceId}&token=${URLEncoder.encode(prefs.token, "UTF-8")}"
        val client = AcpClient(wsUrl, object : AcpClient.Listener {
            override fun onConnected() {
                viewModelScope.launch { setupSession(req) }
            }

            override fun onEvent(event: AcpEvent) {
                _chat.update { s -> ChatReducer.reduce(s, event) }
            }

            override fun onDisconnected(reason: String) {
                _chat.update { it.copy(phase = Phase.Disconnected, error = reason) }
            }
        })
        acp = client
        client.connect()
    }

    private suspend fun setupSession(req: ChatRequest) {
        val client = acp ?: return
        try {
            client.request(
                "initialize",
                JSONObject()
                    .put("protocolVersion", 1) // MUST be the number 1
                    .put("clientCapabilities", JSONObject().put("fs", JSONObject().put("readTextFile", true).put("writeTextFile", true)))
                    .put("clientInfo", JSONObject().put("name", "zcode-pocket").put("version", "1.0.0")),
                30_000L,
            )
            val result = if (req.newSession || req.sessionId == null) {
                client.request("session/new", AcpProtocol.newSessionParams(req.cwd), 60_000L)
            } else {
                client.request("session/load", AcpProtocol.loadParams(req.sessionId, req.cwd), 180_000L)
            }
            val sid = result.optString("sessionId").ifEmpty { req.sessionId.orEmpty() }
            _chat.update { s ->
                var next = s.copy(phase = Phase.Ready, sessionId = sid, error = null)
                val cfg = AcpProtocol.parseConfigOptions(result.optJSONArray("configOptions"))
                if (cfg.isNotEmpty()) next = ChatReducer.reduce(next, AcpEvent.ConfigOptions(cfg))
                next
            }
        } catch (e: Exception) {
            _chat.update { it.copy(phase = Phase.Failed, error = e.message ?: "session setup failed") }
        }
    }

    fun send(text: String) {
        val client = acp ?: return
        val sid = _chat.value.sessionId ?: return
        val trimmed = text.trim()
        if (trimmed.isEmpty() || _chat.value.isRunning) return
        // Our own prompt is never echoed back by the hub — render it locally.
        _chat.update { s -> ChatReducer.reduce(s.copy(phase = Phase.Running), AcpEvent.UserEcho(null, trimmed)) }
        viewModelScope.launch {
            try {
                val res = client.request("session/prompt", AcpProtocol.promptParams(sid, trimmed))
                _chat.update { s -> ChatReducer.reduce(s, ChatReducer.endedFromPromptResponse(res)) }
            } catch (e: Exception) {
                _chat.update { it.copy(phase = Phase.Ready, error = e.message ?: "prompt failed") }
            }
        }
    }

    fun cancelTurn() {
        val client = acp ?: return
        val sid = _chat.value.sessionId ?: return
        client.notify("session/cancel", AcpProtocol.cancelParams(sid))
    }

    fun answerPermission(optionId: String) {
        val client = acp ?: return
        val perm = _chat.value.permission ?: return
        client.respond(perm.reqId, AcpProtocol.permissionResponseParams(optionId))
        _chat.update { it.copy(permission = null) }
    }

    fun setConfig(configId: String, value: String) {
        val client = acp ?: return
        val sid = _chat.value.sessionId ?: return
        viewModelScope.launch {
            try {
                client.request("session/set_config_option", AcpProtocol.setConfigParams(sid, configId, value), 30_000L)
            } catch (e: Exception) {
                _chat.update { it.copy(error = e.message) }
            }
        }
    }

    fun reconnect() {
        currentChat?.let { openChat(it) }
    }

    fun closeChat() {
        acp?.close()
        acp = null
        _chat.value = ChatState(phase = Phase.Ready)
    }

    // ---- project actions ---------------------------------------------------

    /** History listing for one project (incubates its serve bridge on the machine). */
    fun loadProjectSessions(workspacePath: String) {
        viewModelScope.launch {
            try {
                val ps = api().projectSessions(workspacePath)
                _projectSessions.update { it + (workspacePath to ps) }
            } catch (e: Exception) {
                _create.value = CreateState(false, e.message ?: "history listing failed")
            }
        }
    }

    /** POST /api/instances, then wait for the new bridge to register (~20 s). */
    fun createSessionIn(workspacePath: String, onReady: (ChatRequest) -> Unit) {
        viewModelScope.launch {
            _create.value = CreateState(busy = true, message = "Starting a session in ${workspaceName(workspacePath)}…")
            try {
                val api = api()
                val id = api.createInstance(workspacePath)
                var found: HubInstance? = null
                val deadline = System.currentTimeMillis() + 30_000
                while (System.currentTimeMillis() < deadline) {
                    found = api.instances().firstOrNull { it.id == id }
                    if (found != null) break
                    delay(2000)
                }
                if (found == null) {
                    _create.value = CreateState(false, "New bridge ($id) did not register within 30 s — check the machine.")
                    return@launch
                }
                _create.value = CreateState(false, null)
                onReady(ChatRequest(id, null, workspacePath, newSession = true))
            } catch (e: Exception) {
                _create.value = CreateState(false, e.message ?: "session create failed")
            }
        }
    }

    fun dismissCreateMessage() {
        _create.update { it.copy(message = null) }
    }

    override fun onCleared() {
        acp?.close()
        super.onCleared()
    }
}
