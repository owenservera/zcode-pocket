package app.zcode.pocket.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.zcode.pocket.protocol.ChatState
import app.zcode.pocket.protocol.ConfigOption
import app.zcode.pocket.protocol.Permission
import app.zcode.pocket.protocol.Phase
import app.zcode.pocket.protocol.Role
import app.zcode.pocket.protocol.SlashCommand
import app.zcode.pocket.protocol.TimelineItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    state: ChatState,
    onSend: (String) -> Unit,
    onStop: () -> Unit,
    onPermission: (String) -> Unit,
    onConfig: (String, String) -> Unit,
    onReconnect: () -> Unit,
    onBack: () -> Unit,
) {
    var draft by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(state.timeline.size, state.isRunning) {
        if (state.timeline.isNotEmpty()) listState.animateScrollToItem(state.timeline.size - 1)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatusDot(running = state.isRunning)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                state.title ?: "New session",
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Text(
                            statusLine(state),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
            )
        },
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            if (state.configOptions.isNotEmpty()) ConfigChipsRow(state.configOptions, onConfig)
            state.usage?.let { UsageBar(it.used, it.size, it.fraction) }

            Box(Modifier.weight(1f)) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.timeline.size) { i -> TimelineRow(state.timeline[i]) }
                    if (state.phase == Phase.Connecting) {
                        item {
                            Text(
                                "Attaching to the bridge…",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            state.error?.let { ErrorBanner(it, onRetry = onReconnect) }

            if (state.commands.isNotEmpty() && draft.startsWith("/") && !draft.contains(' ')) {
                CommandHints(state.commands, draft) { name -> draft = "/$name " }
            }

            Composer(
                draft = draft,
                onDraft = { draft = it },
                running = state.isRunning,
                sendable = state.phase == Phase.Ready || state.isRunning || state.phase == Phase.Connecting,
                onSend = { onSend(draft); draft = "" },
                onStop = onStop,
            )
        }
    }

    state.permission?.let { p -> PermissionSheet(p, onAnswer = onPermission) }
}

private fun statusLine(state: ChatState): String {
    val phase = when (state.phase) {
        Phase.Connecting -> "attaching"
        Phase.Ready -> if (state.lastStopReason == "cancelled") "cancelled" else "idle"
        Phase.Running -> "working"
        Phase.Disconnected -> "disconnected"
        Phase.Failed -> "failed"
    }
    val model = state.model?.shortValue
    return if (model != null) "$phase · $model" else phase
}

@Composable
private fun TimelineRow(item: TimelineItem) {
    when (item) {
        is TimelineItem.Message -> when (item.role) {
            Role.USER -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp),
                    modifier = Modifier.fillMaxWidth(0.88f),
                ) {
                    Text(
                        item.text,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
            }

            Role.ASSISTANT -> MarkdownText(text = if (item.streaming) item.text + "\u258d" else item.text)

            Role.THOUGHT -> Text(
                "thinking: " + item.text.replace('\n', ' '),
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )

            Role.TURNINFO -> Text(
                item.text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Role.ERROR -> Text(item.text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }

        is TimelineItem.Tool -> ToolCard(item)

        is TimelineItem.Plan -> Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(10.dp)) {
                Text("PLAN", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                for (e in item.entries) {
                    Text("• $e", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

@Composable
private fun ConfigChipsRow(options: List<ConfigOption>, onConfig: (String, String) -> Unit) {
    var expandedId by remember { mutableStateOf<String?>(null) }
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        for (opt in options) {
            Box {
                AssistChip(
                    onClick = { expandedId = if (expandedId == opt.id) null else opt.id },
                    label = { Text("${opt.category}: ${opt.shortValue}", style = MaterialTheme.typography.labelSmall) },
                )
                DropdownMenu(expanded = expandedId == opt.id, onDismissRequest = { expandedId = null }) {
                    for (o in opt.options) {
                        DropdownMenuItem(
                            text = { Text(o.name, style = MaterialTheme.typography.bodySmall) },
                            onClick = {
                                expandedId = null
                                if (o.value != opt.currentValue) onConfig(opt.id, o.value)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UsageBar(used: Long, size: Long, fraction: Float) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        LinearProgressIndicator(
            progress = { fraction },
            color = percentColor(fraction),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "${used / 1000}k / ${size / 1000}k",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CommandHints(commands: List<SlashCommand>, draft: String, onPick: (String) -> Unit) {
    val matches = commands.filter { ("/" + it.name).startsWith(draft.lowercase()) }.take(8)
    if (matches.isEmpty()) return
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        for (c in matches) {
            AssistChip(onClick = { onPick(c.name) }, label = { Text("/" + c.name, style = MaterialTheme.typography.labelSmall) })
        }
    }
}

@Composable
private fun Composer(
    draft: String,
    onDraft: (String) -> Unit,
    running: Boolean,
    sendable: Boolean,
    onSend: () -> Unit,
    onStop: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        OutlinedTextField(
            value = draft,
            onValueChange = onDraft,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Message ZCode…") },
            maxLines = 6,
            shape = RoundedCornerShape(18.dp),
        )
        Spacer(Modifier.width(8.dp))
        FilledIconButton(
            onClick = { if (running) onStop() else if (draft.isNotBlank()) onSend() },
            enabled = running || (sendable && draft.isNotBlank()),
        ) {
            if (running) Icon(Icons.Filled.Close, contentDescription = "Stop")
            else Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PermissionSheet(p: Permission, onAnswer: (String) -> Unit) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden }, // answer it; don't swipe it away
    )
    ModalBottomSheet(onDismissRequest = { }, sheetState = sheetState) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text("Permission required", style = MaterialTheme.typography.titleMedium)
            Text(
                p.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp),
            )
            for (opt in p.options) {
                Button(
                    onClick = { onAnswer(opt.optionId) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                ) { Text(opt.name) }
            }
            Spacer(Modifier.width(1.dp))
        }
    }
}
