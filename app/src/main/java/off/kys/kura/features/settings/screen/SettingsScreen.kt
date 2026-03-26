@file:Suppress("SameParameterValue")

package off.kys.kura.features.settings.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import off.kys.kura.BuildConfig
import off.kys.kura.R
import off.kys.kura.core.designsystem.theme.KuraTheme
import off.kys.kura.core.prefs.KuraPreferences
import off.kys.kura.features.main.screen.components.ProtectionToggleRow
import org.koin.compose.koinInject

class SettingsScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val prefs: KuraPreferences = koinInject()
        val uriHandler = LocalUriHandler.current

        var lockTimeout by remember { mutableLongStateOf(prefs.lockTimeout) }
        var vibration by remember { mutableStateOf(prefs.vibrationEnabled) }

        Scaffold(
            topBar = { TopAppBar(title = { Text(stringResource(R.string.settings)) }) }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // --- SECTION: SECURITY BEHAVIOR ---
                item { SettingHeader(stringResource(R.string.security_behavior)) }
                item {
                    TimeoutSelector(lockTimeout) {
                        lockTimeout = it
                        prefs.lockTimeout = it
                    }
                }

                // --- SECTION: FEEDBACK ---
                item { SettingHeader(stringResource(R.string.feedback)) }
                item {
                    ProtectionToggleRow(
                        title = stringResource(R.string.haptic_feedback_title),
                        description = stringResource(R.string.heptic_feedback_desc),
                        checked = vibration,
                        onCheckedChange = {
                            vibration = it
                            prefs.vibrationEnabled = it
                        }
                    )
                }

                // --- SECTION: ABOUT KURA ---
                item { SettingHeader(stringResource(R.string.about_kura)) }

                item {
                    SettingActionRow(
                        title = stringResource(R.string.github_repo),
                        description = stringResource(R.string.github_repo_desc),
                        icon = painterResource(R.drawable.round_code_24),
                        onClick = { uriHandler.openUri("https://github.com/kys0ff/Kura") }
                    )
                }

                /*item {
                    SettingActionRow(
                        title = stringResource(R.string.privacy_policy),
                        description = stringResource(R.string.privacy_policy_desc),
                        icon = painterResource(R.drawable.round_policy_24),
                        onClick = {
                            // TODO: uriHandler.openUri("https://todo.com/privacy")
                        }
                    )
                }*/

                item {
                    SettingInfoRow(
                        title = stringResource(R.string.app_version),
                        value = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                        icon = painterResource(R.drawable.round_info_24)
                    )
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }

    @Composable
    private fun SettingHeader(title: String) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
        )
    }

    @Composable
    private fun TimeoutSelector(currentTimeout: Long, onTimeoutSelected: (Long) -> Unit) {
        // Map the millisecond values to readable strings
        val options = listOf(
            0L to stringResource(R.string.immediately),
            60_000L to stringResource(R.string._1_minute),
            300_000L to stringResource(R.string._5_minutes),
            900_000L to stringResource(R.string._15_minutes)
        )

        var expanded by remember { mutableStateOf(false) }

        // Find the label for the current value, or fallback to "Custom"
        val currentLabel = options.find { it.first == currentTimeout }?.second
            ?: stringResource(R.string.custom)

        Box(modifier = Modifier.padding(16.dp)) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.lock_timeout, currentLabel))
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { (time, label) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            onTimeoutSelected(time)
                            expanded = false
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun SettingActionRow(
        title: String,
        description: String,
        icon: Painter,
        onClick: () -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Column(modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(
                painter = painterResource(R.drawable.round_open_in_new_24),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }

    @Composable
    private fun SettingInfoRow(title: String, value: String, icon: Painter) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Preview
@Composable
private fun SettingsScreenPreview() {
    KuraTheme {
        SettingsScreen().Content()
    }
}