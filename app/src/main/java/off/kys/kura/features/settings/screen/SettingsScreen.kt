@file:Suppress("SameParameterValue")

package off.kys.kura.features.settings.screen

import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import off.kys.kura.BuildConfig
import off.kys.kura.R
import off.kys.kura.core.common.constants.KURA_GITHUB_REPO_URL
import off.kys.kura.core.common.constants.KURA_PRIVACY_POLICY_URL
import off.kys.kura.core.designsystem.theme.KuraTheme
import off.kys.kura.core.prefs.KuraPreferences
import off.kys.kura.features.main.presentation.activity.MainActivity
import off.kys.kura.features.main.presentation.screen.components.ProtectionToggleRow
import org.koin.compose.koinInject

class SettingsScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val prefs: KuraPreferences = koinInject()
        val uriHandler = LocalUriHandler.current
        val mainActivity = LocalActivity.current as MainActivity
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.settings)) },
                    scrollBehavior = scrollBehavior,
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // --- SECTION: APPEARANCE ---
                item { SettingHeader(stringResource(R.string.appearance)) }
                // Only show Dynamic Color option if API >= 31
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    item {
                        var dynamicColor by remember { mutableStateOf(prefs.dynamicColorEnabled) }
                        ProtectionToggleRow(
                            title = stringResource(R.string.dynamic_color_title),
                            description = stringResource(R.string.dynamic_color_desc),
                            checked = dynamicColor,
                            onCheckedChange = {
                                dynamicColor = it
                                prefs.dynamicColorEnabled = it
                                mainActivity.recreate()
                            }
                        )
                    }
                }
                item {
                    var themeMode by remember { mutableStateOf(prefs.themeMode) }
                    ThemeSelector(themeMode) { selectedTheme ->
                        themeMode = selectedTheme
                        prefs.themeMode = selectedTheme
                        mainActivity.recreate()
                    }
                }
                item {
                    var lockAnimation by remember { mutableStateOf(prefs.lockAnimationEnabled) }
                    ProtectionToggleRow(
                        title = stringResource(R.string.lock_animation_title),
                        description = stringResource(R.string.lock_animation_desc),
                        checked = lockAnimation,
                        onCheckedChange = {
                            lockAnimation = it
                            prefs.lockAnimationEnabled = it
                        }
                    )
                }

                // --- SECTION: SECURITY BEHAVIOR ---
                item { SettingHeader(stringResource(R.string.security_behavior)) }
                item {
                    var lockTimeout by remember { mutableLongStateOf(prefs.lockTimeout) }
                    TimeoutSelector(lockTimeout) {
                        lockTimeout = it
                        prefs.lockTimeout = it
                    }
                }
                item {
                    var resetOnScreenOff by remember { mutableStateOf(prefs.resetOnScreenOff) }
                    ProtectionToggleRow(
                        title = stringResource(R.string.reset_on_screen_off_title),
                        description = stringResource(R.string.reset_on_screen_off_desc),
                        checked = resetOnScreenOff,
                        onCheckedChange = {
                            resetOnScreenOff = it
                            prefs.resetOnScreenOff = it
                        }
                    )
                }

                // --- SECTION: FEEDBACK ---
                item { SettingHeader(stringResource(R.string.feedback)) }
                item {
                    var vibration by remember { mutableStateOf(prefs.vibrationEnabled) }
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
                        onClick = { uriHandler.openUri(KURA_GITHUB_REPO_URL) }
                    )
                }
                item {
                    SettingActionRow(
                        title = stringResource(R.string.privacy_policy),
                        description = stringResource(R.string.privacy_policy_desc),
                        icon = painterResource(R.drawable.round_policy_24),
                        onClick = {
                            uriHandler.openUri(KURA_PRIVACY_POLICY_URL)
                        }
                    )
                }
                item {
                    SettingInfoRow(
                        title = stringResource(R.string.app_version),
                        value = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                        icon = painterResource(R.drawable.round_info_24)
                    )
                }
                item {
                    SettingInfoRow(
                        title = stringResource(R.string.build_time),
                        value = BuildConfig.BUILD_TIME,
                        icon = painterResource(R.drawable.round_calendar_today_24)
                    )
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }

    @Composable
    private fun ThemeSelector(currentTheme: String, onThemeSelected: (String) -> Unit) {
        val options = listOf(
            "SYSTEM" to stringResource(R.string.theme_system),
            "LIGHT" to stringResource(R.string.theme_light),
            "DARK" to stringResource(R.string.theme_dark)
        )

        var expanded by remember { mutableStateOf(false) }
        val currentLabel = options.find { it.first == currentTheme }?.second ?: options[0].second

        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(R.string.app_theme))
                    Text(text = currentLabel, color = MaterialTheme.colorScheme.primary)
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.9f) // Keep it almost full width
            ) {
                options.forEach { (mode, label) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            onThemeSelected(mode)
                            expanded = false
                        },
                        leadingIcon = {
                            val icon = when (mode) {
                                "LIGHT" -> R.drawable.round_light_mode_24
                                "DARK" -> R.drawable.round_mode_night_24
                                else -> R.drawable.round_settings_brightness_24
                            }
                            Icon(painterResource(icon), contentDescription = null)
                        }
                    )
                }
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
            900_000L to stringResource(R.string._15_minutes),
            -1L to stringResource(R.string.never)
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
            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f)
            ) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                Text(
                    value,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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