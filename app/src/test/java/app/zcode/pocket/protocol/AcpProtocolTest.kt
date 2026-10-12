package app.zcode.pocket.protocol

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AcpProtocolTest {

    @Test
    fun parsesResponseByIdAndResult() {
        val frame = AcpProtocol.parseServerFrame("""{"jsonrpc":"2.0","id":7,"result":{"sessionId":"sess_x"}}""")
        val r = frame as ServerFrame.Response
        assertEquals(7, r.id)
        assertEquals("sess_x", r.result?.optString("sessionId"))
    }

    @Test
    fun parsesErrorResponse() {
        val frame = AcpProtocol.parseServerFrame("""{"jsonrpc":"2.0","id":7,"error":{"code":-32603,"message":"boom"}}""")
        val r = frame as ServerFrame.Response
        assertTrue(r.error != null)
    }

    @Test
    fun parsesUpdateNotificationAndMapsChunk() {
        val raw = """{"jsonrpc":"2.0","method":"session/update","params":{"update":{"sessionUpdate":"agent_message_chunk","content":{"type":"text","text":"hi"},"messageId":"msg_1"}}}"""
        val frame = AcpProtocol.parseServerFrame(raw) as ServerFrame.Notification
        val events = AcpProtocol.toEvents(frame)
        assertEquals(AcpEvent.MessageChunk("msg_1", "hi"), events.single())
    }

    @Test
    fun parsesTurnStateNotification() {
        val raw = """{"jsonrpc":"2.0","method":"$/zcode/turnState","params":{"sessionId":"s","running":true}}"""
        val frame = AcpProtocol.parseServerFrame(raw) as ServerFrame.Notification
        assertEquals(AcpEvent.TurnState(true), AcpProtocol.toEvents(frame).single())
    }

    @Test
    fun parsesToolCallStart() {
        val raw = """{"jsonrpc":"2.0","method":"session/update","params":{"update":{"sessionUpdate":"tool_call","toolCallId":"call-1","title":"Read: a.txt","kind":"read","status":"pending"}}}"""
        val events = AcpProtocol.toEvents(AcpProtocol.parseServerFrame(raw) as ServerFrame.Notification)
        assertEquals(AcpEvent.ToolStart("call-1", "Read: a.txt", "read", "pending"), events.single())
    }

    @Test
    fun parsesUsageUpdate() {
        val raw = """{"jsonrpc":"2.0","method":"session/update","params":{"update":{"sessionUpdate":"usage_update","used":1234,"size":500000}}}"""
        val events = AcpProtocol.toEvents(AcpProtocol.parseServerFrame(raw) as ServerFrame.Notification)
        assertEquals(AcpEvent.UsageUpdate(1234L, 500000L), events.single())
    }

    @Test
    fun permissionRequestMapsToEventWithOptions() {
        val raw = """{"jsonrpc":"2.0","id":11,"method":"session/request_permission","params":{"sessionId":"s","toolCall":{"title":"Write file x"},"options":[{"optionId":"allow","name":"Allow once","kind":"allow"},{"optionId":"always","name":"Always allow","kind":"allow_always"}]}}"""
        val frame = AcpProtocol.parseServerFrame(raw) as ServerFrame.ServerRequest
        val event = AcpProtocol.toEvent(frame) as AcpEvent.PermissionRequest
        assertEquals(11, event.reqId)
        assertEquals("Write file x", event.title)
        assertEquals(2, event.options.size)
        assertEquals("Allow once", event.options[0].name)
    }

    @Test
    fun protocolVersionIsTheNumberOne() {
        val raw = JSONObject(AcpProtocol.initializeRequest(1))
        assertEquals(1, raw.getJSONObject("params").getInt("protocolVersion"))
    }

    @Test
    fun cancelIsANotificationWithoutId() {
        val raw = AcpProtocol.notification("session/cancel", AcpProtocol.cancelParams("sess_x"))
        assertTrue(!raw.contains("\"id\""))
        assertEquals("sess_x", JSONObject(raw).getJSONObject("params").getString("sessionId"))
    }

    @Test
    fun loadParamsCarryMcpServersEvenWhenEmpty() {
        val raw = AcpProtocol.loadParams("sess_x", null)
        assertTrue(raw.getJSONArray("mcpServers").length() == 0)
        assertEquals("sess_x", raw.getString("sessionId"))
    }

    @Test
    fun permissionResponseMatchesObservedShape() {
        val raw = AcpProtocol.response(3, AcpProtocol.permissionResponseParams("allow"))
        val obj = JSONObject(raw)
        assertEquals(3, obj.getInt("id"))
        assertEquals("selected", obj.getJSONObject("result").getJSONObject("outcome").getString("outcome"))
        assertEquals("allow", obj.getJSONObject("result").getJSONObject("outcome").getString("optionId"))
    }

    @Test
    fun newSessionParamsPinsCwd() {
        val raw = AcpProtocol.newSessionParams("C:\\proj")
        assertEquals("C:\\proj", raw.getString("cwd"))
        assertTrue(raw.getJSONArray("mcpServers").length() == 0)
    }

    @Test
    fun configOptionsParseFromObservedSeenShape() {
        val arr = org.json.JSONArray(
            """[{"id":"model","name":"Model","category":"model","type":"select","currentValue":"grok-build-oauth\\grok-4.7","options":[{"value":"grok-build-oauth\\grok-4.7","name":"Grok Build (OAuth) › grok-4.7"}]}]""",
        )
        val opts = AcpProtocol.parseConfigOptions(arr)
        assertEquals(1, opts.size)
        assertEquals("model", opts[0].category)
        assertEquals("grok-4.7", opts[0].shortValue)
    }
}
