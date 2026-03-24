@file:SuppressLint("LocalContextGetResourceValueCall")

package off.kys.kura.features.main.screen

import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.LifecycleResumeEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import off.kys.kura.R
import off.kys.kura.core.admin.LockerAdminReceiver
import off.kys.kura.features.main.event.MainUiEvent
import off.kys.kura.features.main.screen.components.AppItemRow
import off.kys.kura.features.main.screen.components.AppSelectionHeader
import off.kys.kura.features.main.screen.components.KeepAndroidOpenNotice
import off.kys.kura.features.main.screen.components.PermissionCard
import off.kys.kura.features.main.screen.components.SystemProtectionSection
import off.kys.kura.features.main.viewmodel.MainViewModel
import off.kys.kura.features.settings.screen.SettingsScreen
import org.koin.compose.koinInject

class MainScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel: MainViewModel = koinInject()
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current
        val state = viewModel.uiState
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        val adminComponent = remember { ComponentName(context, LockerAdminReceiver::class.java) }

        // State to manage one-time visibility of the banner
        var showKeepAndroidOpenNotice by rememberSaveable { mutableStateOf(true) }

        LifecycleResumeEffect(Unit) {
            viewModel.onEvent(MainUiEvent.RefreshSystemStates)
            onPauseOrDispose {}
        }

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name)) },
                    scrollBehavior = scrollBehavior,
                    actions = {
                        IconButton(onClick = { navigator += SettingsScreen() }) {
                            Icon(
                                painter = painterResource(R.drawable.round_settings_24),
                                contentDescription = stringResource(R.string.settings)
                            )
                        }
                    }
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // --- Keep Android Open Notice ---
                item {
                    KeepAndroidOpenNotice(
                        isVisible = showKeepAndroidOpenNotice,
                        onDismiss = { showKeepAndroidOpenNotice = false }
                    )
                }

                if (!state.isAccessibilityEnabled || !state.canDrawOverlays) {
                    item {
                        PermissionCard(
                            state = state,
                            onGrantAccessibility = {
                                viewModel.onEvent(MainUiEvent.RefreshSystemStates)
                                if (!state.isAccessibilityEnabled) {
                                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                                }
                            },
                            onGrantOverlay = {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    "package:${context.packageName}".toUri()
                                )
                                context.startActivity(intent)
                            }
                        )
                    }
                }

                item {
                    SystemProtectionSection(
                        state = state,
                        onSettingsLockToggle = { viewModel.onEvent(MainUiEvent.ToggleSettingsLock(it)) },
                        onUninstallLockChanged = {
                            viewModel.onEvent(MainUiEvent.ToggleUninstallLock(it))
                        },
                        onSelfLockToggle = { viewModel.onEvent(MainUiEvent.ToggleSelfLock(it)) },
                        onAdminToggle = { activate ->
                            if (activate) {
                                val intent =
                                    Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                                        putExtra(
                                            DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                                            adminComponent
                                        )
                                        putExtra(
                                            DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                            context.getString(R.string.device_admin_description)
                                        )
                                    }
                                context.startActivity(intent)
                            } else viewModel.onEvent(MainUiEvent.DeactivateAdmin)
                        }
                    )
                }

                item {
                    AppSelectionHeader(
                        areAllLocked = state.areAllLocked,
                        onToggleAll = {
                            viewModel.onEvent(if (state.areAllLocked) MainUiEvent.UnlockAllApps else MainUiEvent.LockAllApps)
                        }
                    )
                }

                items(state.installedApps, key = { it.packageName }) { app ->
                    AppItemRow(
                        app = app,
                        isLocked = state.lockedApps.contains(app.packageName),
                        onToggle = {
                            viewModel.onEvent(MainUiEvent.ToggleAppLock(app.packageName, it))
                        }
                    )
                }
            }
        }
    }
}