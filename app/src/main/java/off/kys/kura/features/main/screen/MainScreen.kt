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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import off.kys.kura.features.main.screen.components.AppSelectionHeader
import off.kys.kura.features.main.screen.components.PermissionCard
import off.kys.kura.features.main.screen.components.SystemProtectionSection
import off.kys.kura.features.main.viewmodel.MainViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel = koinInject()) {
    val context = LocalContext.current
    val state = viewModel.uiState
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val adminComponent = remember { ComponentName(context, LockerAdminReceiver::class.java) }

    LifecycleResumeEffect(Unit) {
        viewModel.onEvent(MainUiEvent.RefreshSystemStates)
        onPauseOrDispose {}
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                scrollBehavior = scrollBehavior
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
                            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, "package:${context.packageName}".toUri())
                            context.startActivity(intent)
                        }
                    )
                }
            }

            item {
                SystemProtectionSection(
                    state = state,
                    onSettingsLockToggle = { viewModel.onEvent(MainUiEvent.ToggleSettingsLock(it)) },
                    onUninstallLockChanged = { viewModel.onEvent(MainUiEvent.ToggleUninstallLock(it)) },
                    onSelfLockToggle = { viewModel.onEvent(MainUiEvent.ToggleSelfLock(it)) },
                    onAdminToggle = { activate ->
                        if (activate) {
                            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                                putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, context.getString(R.string.device_admin_description))
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
                    onToggle = { viewModel.onEvent(MainUiEvent.ToggleAppLock(app.packageName, it)) }
                )
            }
        }
    }
}