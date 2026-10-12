package app.zcode.pocket.data

import app.zcode.pocket.protocol.AcpEvent
import app.zcode.pocket.protocol.AcpProtocol
import app.zcode.pocket.protocol.ServerFrame
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * ACP over the hub's WebSocket (`/acp?instance=…`), one JSON-RPC message per
 * text frame. Request/response pairs suspend; notifications and server→client
 * requests become [AcpEvent]s for the reducer.
 */
class AcpClient(private val url: String, private val listener: Listener) {

    interface Listener {
        fun onConnected()
        fun onEvent(event: AcpEvent)
        fun onDisconnected(reason: String)
    }

    private val client = OkHttpClient.Builder()
        .pingInterval(25, TimeUnit.SECONDS) // hub pings at 30s; keep the NAT mapping warm
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private var nextId = 1
    private val pending = ConcurrentHashMap<String, CompletableDeferred<ServerFrame.Response>>()

    @Volatile private var closing = false

    fun connect() {
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) = listener.onConnected()

            override fun onMessage(webSocket: WebSocket, text: String) = handle(text)

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                if (!closing) listener.onDisconnected(reason.ifBlank { "closed ($code)" })
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                if (!closing) listener.onDisconnected(t.message ?: "connection failed")
            }
        })
    }

    fun close() {
        closing = true
        webSocket?.close(1000, "bye")
        webSocket = null
        pending.values.forEach { it.cancel() }
        pending.clear()
    }

    suspend fun request(method: String, params: JSONObject, timeoutMs: Long = 900_000L): JSONObject {
        val id = synchronized(this) { nextId++ }
        val deferred = CompletableDeferred<ServerFrame.Response>()
        pending[id.toString()] = deferred
        val sent = webSocket?.send(AcpProtocol.request(id, method, params)) ?: false
        if (!sent) {
            pending.remove(id.toString())
            throw HubException("not connected")
        }
        val resp = try {
            withTimeout(timeoutMs) { deferred.await() }
        } finally {
            pending.remove(id.toString())
        }
        resp.error?.let { throw HubException(it.optString("message", it.toString())) }
        return resp.result ?: JSONObject()
    }

    fun notify(method: String, params: JSONObject) {
        webSocket?.send(AcpProtocol.notification(method, params))
    }

    fun respond(id: Any, result: JSONObject) {
        webSocket?.send(AcpProtocol.response(id, result))
    }

    private fun handle(text: String) {
        when (val frame = AcpProtocol.parseServerFrame(text)) {
            is ServerFrame.Response -> pending.remove(frame.id.toString())?.complete(frame)
            is ServerFrame.Notification -> AcpProtocol.toEvents(frame).forEach(listener::onEvent)
            is ServerFrame.ServerRequest -> listener.onEvent(AcpProtocol.toEvent(frame))
            null -> {}
        }
    }
}
