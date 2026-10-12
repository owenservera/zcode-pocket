package app.zcode.pocket.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import app.zcode.pocket.protocol.ChatState
import app.zcode.pocket.protocol.PermOption
import app.zcode.pocket.protocol.Permission
import app.zcode.pocket.protocol.Phase
import app.zcode.pocket.protocol.Role
import app.zcode.pocket.protocol.SlashCommand
import app.zcode.pocket.protocol.TimelineItem
import app.zcode.pocket.protocol.Usage
import app.zcode.pocket.ui.theme.ZpTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ChatScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private fun sampleState() = ChatState(
        phase = Phase.Ready,
        title = "Fix login bug",
        timeline = listOf(
            TimelineItem.Message("u1", Role.USER, "What broke?", streaming = false),
            TimelineItem.Message("a1", Role.ASSISTANT, "The token expired. I restarted it.", streaming = false),
            TimelineItem.Tool("t1", "Read: config", "read", "completed", "token: ok"),
        ),
        usage = Usage(50_000, 500_000),
        commands = listOf(SlashCommand("compact", "")),
    )

    @Test
    fun chatRendersMessagesToolCardsAndUsage() {
        compose.setContent {
            ZpTheme {
                ChatScreen(
                    state = sampleState(),
                    onSend = {}, onStop = {}, onPermission = {}, onConfig = { _, _ -> }, onReconnect = {}, onBack = {},
                )
            }
        }
        compose.onNodeWithText("What broke?").assertIsDisplayed()
        compose.onNodeWithText("The token expired. I restarted it.").assertIsDisplayed()
        compose.onNodeWithText("Read: config").assertIsDisplayed()
        compose.onNodeWithText("completed").assertIsDisplayed()
        compose.onNodeWithText("50k / 500k").assertIsDisplayed()
    }

    @Test
    fun permissionSheetShowsOptions() {
        val state = sampleState().copy(
            permission = Permission("r1", "Write file", listOf(PermOption("allow", "Allow", null), PermOption("deny", "Deny", null))),
        )
        compose.setContent {
            ZpTheme {
                ChatScreen(
                    state = state,
                    onSend = {}, onStop = {}, onPermission = {}, onConfig = { _, _ -> }, onReconnect = {}, onBack = {},
                )
            }
        }
        compose.onNodeWithText("Permission required").assertIsDisplayed()
        compose.onNodeWithText("Write file").assertIsDisplayed()
        compose.onNodeWithText("Allow").assertIsDisplayed()
        compose.onNodeWithText("Deny").assertIsDisplayed()
    }

    @Test
    fun disconnectShowsReconnectBanner() {
        val state = sampleState().copy(phase = Phase.Disconnected, error = "connection dropped")
        compose.setContent {
            ZpTheme {
                ChatScreen(
                    state = state,
                    onSend = {}, onStop = {}, onPermission = {}, onConfig = { _, _ -> }, onReconnect = {}, onBack = {},
                )
            }
        }
        compose.onNodeWithText("connection dropped").assertIsDisplayed()
    }
}
