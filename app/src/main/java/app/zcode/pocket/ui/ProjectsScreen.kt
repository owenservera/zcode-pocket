package app.zcode.pocket.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.zcode.pocket.data.ProjectSessions
import app.zcode.pocket.data.workspaceName
import app.zcode.pocket.vm.CreateState
import app.zcode.pocket.vm.HubUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    hub: HubUiState,
    sessionsByPath: Map<String, ProjectSessions>,
    create: CreateState,
    onLoad: (String) -> Unit,
    onResume: (instanceId: String, sessionId: String, workspacePath: String) -> Unit,
    onCreate: (String) -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Projects") }) },
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            Text(
                "Every workspace that ran a session. Tap to browse history, resume a conversation, or start a new one.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
            if (create.message != null) ErrorBanner(create.message) {}
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (p in hub.projects) {
                    item {
                        val loaded = sessionsByPath.containsKey(p.workspacePath)
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().clickable { if (!loaded) onLoad(p.workspacePath) },
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(workspaceName(p.workspacePath), style = MaterialTheme.typography.titleSmall)
                                        Text(
                                            "${p.sessions} session(s) · ${formatEpochDate(p.lastActive)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    Icon(Icons.Filled.Add, contentDescription = "New session", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.clickable { onCreate(p.workspacePath) })
                                }
                                val ps = sessionsByPath[p.workspacePath]
                                if (ps != null) {
                                    for (s in ps.sessions.take(8)) {
                                        Row(
                                            Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    val inst = ps.instanceId
                                                    if (inst != null) onResume(inst, s.sessionId, p.workspacePath)
                                                }
                                                .padding(vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                s.title ?: s.sessionId.take(16),
                                                style = MaterialTheme.typography.bodySmall,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f),
                                            )
                                            Text(
                                                s.updatedAt?.take(10) ?: "",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                    if (ps.sessions.isEmpty()) {
                                        Text("No history yet.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
                if (hub.projects.isEmpty()) {
                    item {
                        Text(
                            if (hub.loading) "Loading…" else "No projects — is the hub reachable?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(20.dp),
                        )
                    }
                }
            }
        }
    }
}
