package app.zcode.pocket.ui

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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import app.zcode.pocket.data.workspaceName
import app.zcode.pocket.vm.HubUiState
import org.json.JSONArray
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MachineScreen(hub: HubUiState, onRefresh: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Machine") },
                actions = { IconButton(onClick = onRefresh) { Icon(Icons.Filled.Refresh, contentDescription = "Refresh") } },
            )
        },
    ) { pad ->
        LazyColumn(
            Modifier.padding(pad).fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Card("Hub") {
                    Text(
                        if (hub.error != null) "Unreachable: ${hub.error}" else "Connected · refreshed ${formatEpoch(hub.lastRefresh)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (hub.error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                    )
                }
            }

            val quota = hub.quota
            if (quota != null) {
                item { SectionHeader("Plan usage") }
                for (name in listOf("glm", "opencode", "ollama")) {
                    val section = quota.optJSONObject(name) ?: continue
                    item { QuotaSection(name, section) }
                }
            }

            val wf = hub.workflows
            if (wf != null) {
                item { SectionHeader("Workflows (swarm runs)") }
                val active = wf.optJSONArray("activeRuns") ?: JSONArray()
                val recent = wf.optJSONArray("recentRuns") ?: JSONArray()
                val groups = wf.optJSONArray("groups") ?: JSONArray()
                item {
                    Card("Runs") {
                        Text(
                            "active ${active.length()} · recent ${recent.length()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        for (i in 0 until active.length()) {
                            val r = active.optJSONObject(i) ?: continue
                            Text(
                                "▸ ${r.optString("name", "run")} — ${r.optString("status", "?")}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                        for (i in 0 until groups.length()) {
                            val g = groups.optJSONObject(i) ?: continue
                            val err = g.optString("error")
                            Text(
                                "· ${workspaceName(g.optString("workspace"))}: " +
                                    if (err.isNotEmpty()) err else "${(g.optJSONArray("workflows")?.length() ?: 0)} workflow(s)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }
            }

            if (hub.quota == null && hub.workflows == null) {
                item {
                    Text(
                        if (hub.loading) "Loading…" else "No machine data yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(20.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun Card(title: String, content: @Composable () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(title.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(1.dp))
            content()
        }
    }
}

@Composable
private fun QuotaSection(name: String, section: JSONObject) {
    val kind = section.optString("kind", "unknown")
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(name.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            if (kind != "success") {
                Text(
                    when (kind) {
                        "not_configured" -> "not configured"
                        "auth_error" -> "auth expired"
                        "rate_limited" -> "rate limited"
                        else -> kind
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
                return@Column
            }
            val items = section.optJSONArray("items")
            if (items != null) for (i in 0 until items.length()) {
                val it0 = items.optJSONObject(i) ?: continue
                QuotaRow(it0.optString("label", it0.optString("key")), it0.optDouble("usedPercent", 0.0))
            }
            val windows = section.optJSONArray("windows")
            if (windows != null) for (i in 0 until windows.length()) {
                val w = windows.optJSONObject(i) ?: continue
                QuotaRow(w.optString("label", w.optString("key")), w.optDouble("usagePercent", 0.0))
            }
        }
    }
}

@Composable
private fun QuotaRow(label: String, percent: Double) {
    val f = (percent / 100.0).toFloat().coerceIn(0f, 1f)
    Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(64.dp))
        LinearProgressIndicator(
            progress = { f },
            color = percentColor(f),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "${percent.toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
