@file:SuppressLint("LocalContextGetResourceValueCall")

package off.kys.kura.features.main.screen

import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.LifecycleResumeEffect
import off.kys.kura.R
import off.kys.kura.core.admin.LockerAdminReceiver
import off.kys.kura.features.main.event.MainUiEvent
import off.kys.kura.features.main.screen.components.AppItemRow
import off.kys.kura.features.main.screen.components.PermissionCard
import off.kys.kura.features.main.screen.components.SystemProtectionSection
import off.kys.kura.features.main.viewmodel.MainViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = koinInject()
) {
    val context = LocalContext.current
    val state = viewModel.uiState
    val areAllLocked = state.areAllLocked
    val adminComponent = remember { ComponentName(context, LockerAdminReceiver::class.java) }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    // Sync state when returning to app
    LifecycleResumeEffect(key1 = Unit) {
        viewModel.onEvent(MainUiEvent.RefreshSystemStates)
        onPauseOrDispose {}
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                scrollBehavior = scrollBehavior
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(horizontal = 8.dp)
        ) {
            if (!state.isAccessibilityEnabled || !state.canDrawOverlays) {
                PermissionCard(
                    isAccessibilityEnabled = state.isAccessibilityEnabled,
                    canDrawOverlays = state.canDrawOverlays,
                    onGrantAccessibility = {
                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    },
                    onGrantOverlay = {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            "package:${context.packageName}".toUri()
                        )
                        context.startActivity(intent)
                    }
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                item {
                    SystemProtectionSection(
                        isSettingsLocked = state.isSettingsLocked,
                        isUninstallLocked = state.isUninstallLocked,
                        isAdminActive = state.isAdminActive,
                        isSelfLockEnabled = state.isSelfLockEnabled,
                        onSettingsLockToggle = { viewModel.onEvent(MainUiEvent.ToggleSettingsLock(it)) },
                        onUninstallLockChanged = { shouldLock ->
                            viewModel.onEvent(MainUiEvent.ToggleUninstallLock(shouldLock))
                        },
                        onAdminToggle = { shouldActivate ->
                            if (shouldActivate) {
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
                            } else {
                                viewModel.onEvent(MainUiEvent.DeactivateAdmin)
                            }
                        },
                        onSelfLockToggle = { viewModel.onEvent(MainUiEvent.ToggleSelfLock(it)) }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.select_apps_to_lock),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )

                        TextButton(
                            onClick = {
                                if (areAllLocked) {
                                    // Logic for unlocking all (needs to be handled in ViewModel)
                                    viewModel.onEvent(MainUiEvent.UnlockAllApps)
                                } else {
                                    viewModel.onEvent(MainUiEvent.LockAllApps)
                                }
                            }
                        ) {
                            Text(
                                if (areAllLocked) stringResource(R.string.unlock_all)
                                else stringResource(R.string.lock_all)
                            )
                        }
                    }
                }

                items(state.installedApps) { app ->
                    AppItemRow(
                        app = app,
                        isLocked = state.lockedApps.contains(app.packageName),
                        onToggle = { isChecked ->
                            viewModel.onEvent(MainUiEvent.ToggleAppLock(app.packageName, isChecked))
                        }
                    )
                }
            }
        }
    }
}