package app.zcode.pocket.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.zcode.pocket.data.HubApi
import app.zcode.pocket.vm.PocketViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: PocketViewModel, onBack: () -> Unit) {
    var url by remember { mutableStateOf(vm.hubUrl) }
    var token by remember { mutableStateOf(vm.token) }
    var result by remember { mutableStateOf<String?>(null) }
    var testing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
            )
        },
    ) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("Hub URL") },
                placeholder = { Text("http://100.x.y.z:8377") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = token,
                onValueChange = { token = it },
                label = { Text("Token") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        vm.saveConfig(url, token)
                        result = "Saved."
                    },
                ) { Text("Save") }
                Button(
                    onClick = {
                        testing = true
                        result = null
                        scope.launch {
                            val api = HubApi(url.trim(), token.trim())
                            result = try {
                                val health = api.health()
                                if (!health) "Hub not reachable at $url (health failed)."
                                else {
                                    val inst = api.instances()
                                    "Connected. ${inst.size} bridge(s): " +
                                        inst.joinToString(", ") { it.id + " @" + it.workspace.substringAfterLast('\\') }
                                }
                            } catch (e: Exception) {
                                "Failed: ${e.message}"
                            }
                            testing = false
                        }
                    },
                    enabled = !testing,
                ) { Text(if (testing) "Testing…" else "Test") }
            }
            result?.let {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(it, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(10.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("About", style = MaterialTheme.typography.titleSmall)
            Text(
                "ZCode Pocket 1.0.0 — a personal client for the zcode-acp hub on the machine.\n\n" +
                    "Machine side: playbook/toolkit/zcode-pocket/launcher.ps1 start\n" +
                    "Acceptance: node playbook/toolkit/zcode-pocket/acceptance.mjs\n\n" +
                    "The token grants full control of every agent session on the machine. " +
                    "Reach the hub over Tailscale or a trusted LAN, never the open internet without TLS.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
