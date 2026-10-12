package app.zcode.pocket.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import app.zcode.pocket.vm.ChatRequest
import app.zcode.pocket.vm.PocketViewModel

sealed interface Screen {
    data object Chats : Screen
    data object Projects : Screen
    data object Agents : Screen
    data object Machine : Screen
    data object Settings : Screen
    data object Chat : Screen
}

private data class Tab(val screen: Screen, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab(Screen.Chats, "Chats", Icons.Filled.Home),
    Tab(Screen.Projects, "Projects", Icons.AutoMirrored.Filled.List),
    Tab(Screen.Agents, "Agents", Icons.Filled.Person),
    Tab(Screen.Machine, "Machine", Icons.Filled.Info),
)

@Composable
fun PocketApp(vm: PocketViewModel) {
    var screen by remember { mutableStateOf<Screen>(Screen.Chats) }
    val hub by vm.hub.collectAsState()
    val chat by vm.chat.collectAsState()
    val create by vm.create.collectAsState()
    val projectSessions by vm.projectSessions.collectAsState()

    LaunchedEffect(Unit) { vm.startPolling() }
    DisposableEffect(Unit) { onDispose { vm.stopPolling() } }

    fun openChat(req: ChatRequest) {
        vm.openChat(req)
        screen = Screen.Chat
    }

    when (val s = screen) {
        Screen.Chat -> ChatScreen(
            state = chat,
            onSend = vm::send,
            onStop = vm::cancelTurn,
            onPermission = vm::answerPermission,
            onConfig = vm::setConfig,
            onReconnect = vm::reconnect,
            onBack = {
                vm.closeChat()
                screen = Screen.Chats
            },
        )

        Screen.Settings -> SettingsScreen(vm, onBack = { screen = Screen.Chats })

        else -> Scaffold(
            bottomBar = {
                NavigationBar {
                    for (t in tabs) {
                        NavigationBarItem(
                            selected = s == t.screen,
                            onClick = { screen = t.screen },
                            icon = { Icon(t.icon, contentDescription = t.label) },
                            label = { Text(t.label) },
                        )
                    }
                }
            },
        ) { pad ->
            Box(Modifier.padding(pad).fillMaxSize()) {
                when (s) {
                    Screen.Chats -> SessionsScreen(
                        state = hub,
                        onOpen = ::openChat,
                        onRefresh = vm::refresh,
                        onSettings = { screen = Screen.Settings },
                    )

                    Screen.Projects -> ProjectsScreen(
                        hub = hub,
                        sessionsByPath = projectSessions,
                        create = create,
                        onLoad = vm::loadProjectSessions,
                        onResume = { inst, sid, path -> openChat(ChatRequest(inst, sid, path, newSession = false)) },
                        onCreate = { path -> vm.createSessionIn(path, ::openChat) },
                    )

                    Screen.Agents -> AgentsScreen(hub)

                    Screen.Machine -> MachineScreen(hub, onRefresh = vm::refresh)

                    else -> {}
                }
            }
        }
    }

    create.message?.let { msg ->
        AlertDialog(
            onDismissRequest = vm::dismissCreateMessage,
            title = { Text("Session") },
            text = { Text(if (create.busy) msg else msg) },
            confirmButton = {
                TextButton(onClick = vm::dismissCreateMessage) { Text("OK") }
            },
        )
    }
}
