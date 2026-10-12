package app.zcode.pocket.protocol

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The reducer is tested against the REAL frames captured from the live hub on
 * 2026-10-12 (src/test/resources/frames.jsonl) — the same tool-using turn that
 * exercised the acceptance script.
 */
class ChatReducerTest {

    private fun fixtureFrames(): List<String> {
        val stream = javaClass.getResourceAsStream("/frames.jsonl") ?: error("missing fixture frames.jsonl")
        return stream.bufferedReader().readLines()
    }

    @Test
    fun realFramesReduceToACoherentConversation() {
        val state = ChatReducer.reduceFrames(ChatState(), fixtureFrames())

        // The hub truncates the auto-title at 80 chars (docs/REMOTE-CLIENTS.md).
        assertEquals("Read the file playbook/VERSION with the Read tool, then answer with its exact co", state.title)

        val tool = state.timeline.filterIsInstance<TimelineItem.Tool>().single()
        assertEquals("completed", tool.status)
        assertTrue(tool.title.startsWith("Read:"))
        assertTrue(tool.detail?.contains("0.8.1") == true)

        val messages = state.timeline.filterIsInstance<TimelineItem.Message>()
        assertTrue("assistant answer missing", messages.any { it.role == Role.ASSISTANT && it.text.contains("0.8.1") })
        assertTrue("turninfo footer missing", messages.any { it.role == Role.TURNINFO && it.text.contains("completed") })

        assertEquals(37106L, state.usage?.used)
        assertEquals(500000L, state.usage?.size)
        assertTrue(state.commands.any { it.name == "auto" })
        assertTrue(state.configOptions.any { it.category == "model" })
        assertTrue(state.configOptions.any { it.category == "mode" })
        assertEquals(Phase.Ready, state.phase)
    }

    @Test
    fun chunksWithSameMessageIdAppendIntoOneMessage() {
        val s1 = ChatReducer.reduce(ChatState(), AcpEvent.MessageChunk("msg_a", "Hel"))
        val s2 = ChatReducer.reduce(s1, AcpEvent.MessageChunk("msg_a", "lo"))
        val msgs = s2.timeline.filterIsInstance<TimelineItem.Message>()
        assertEquals(1, msgs.size)
        assertEquals("Hello", msgs[0].text)
    }

    @Test
    fun turninfoChunksBecomeSeparateFooterMessages() {
        val s = ChatReducer.reduce(ChatState(), AcpEvent.MessageChunk("turninfo_1", "done"))
        assertEquals(Role.TURNINFO, (s.timeline[0] as TimelineItem.Message).role)
    }

    @Test
    fun permissionShowsThenCancelClears() {
        val s = ChatReducer.reduce(ChatState(), AcpEvent.PermissionRequest("req1", "Write file", listOf(PermOption("allow", "Allow", "allow"))))
        assertNotNull(s.permission)
        val s2 = ChatReducer.reduce(s, AcpEvent.PermissionCancelled("req1"))
        assertNull(s2.permission)
    }

    @Test
    fun toolLifecyclePendingThenCompleted() {
        var s = ChatReducer.reduce(ChatState(), AcpEvent.ToolStart("t1", "Read: x", "read", "pending"))
        assertEquals("pending", (s.timeline[0] as TimelineItem.Tool).status)
        s = ChatReducer.reduce(s, AcpEvent.ToolUpdate("t1", "in_progress", null, null))
        assertEquals("in_progress", (s.timeline[0] as TimelineItem.Tool).status)
        s = ChatReducer.reduce(s, AcpEvent.ToolUpdate("t1", "completed", "Renamed", "output"))
        val t = s.timeline[0] as TimelineItem.Tool
        assertEquals("completed", t.status)
        assertEquals("Renamed", t.title)
        assertEquals("output", t.detail)
    }

    @Test
    fun turnEndedResetsStreamingAndPhase() {
        var s = ChatReducer.reduce(ChatState(phase = Phase.Running), AcpEvent.MessageChunk("m", "text"))
        s = ChatReducer.reduce(s, AcpEvent.TurnState(true))
        assertEquals(Phase.Running, s.phase)
        s = ChatReducer.reduce(s, ChatReducer.endedFromPromptResponse(org.json.JSONObject().put("stopReason", "end_turn")))
        assertEquals(Phase.Ready, s.phase)
        assertTrue(s.timeline.filterIsInstance<TimelineItem.Message>().all { !it.streaming })
        assertEquals("end_turn", s.lastStopReason)
    }
}
