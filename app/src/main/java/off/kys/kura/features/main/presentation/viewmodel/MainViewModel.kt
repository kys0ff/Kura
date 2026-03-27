package off.kys.kura.features.main.presentation.viewmodel

import android.app.Application
import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import off.kys.kura.core.admin.LockerAdminReceiver
import off.kys.kura.core.common.PackageResolver
import off.kys.kura.core.common.constants.ANDROID_SETTINGS_PACKAGE
import off.kys.kura.core.common.constants.ANDROID_UNINSTALLER_PACKAGES
import off.kys.kura.core.common.constants.KURA_PACKAGE
import off.kys.kura.core.common.extensions.isAccessibilityServiceEnabled
import off.kys.kura.core.registry.LockSessionManager
import off.kys.kura.features.main.presentation.event.MainUiEvent
import off.kys.kura.features.main.presentation.state.MainViewState

class MainViewModel(
    private val application: Application,
    private val resolver: PackageResolver,
    private val lockManager: LockSessionManager
) : AndroidViewModel(application) {

    var uiState by mutableStateOf(MainViewState())
        private set

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        uiState = uiState.copy(
            installedApps = resolver.getInstalledApps(),
            lockedApps = lockManager.getLockedPackages()
        )
    }

    fun onEvent(event: MainUiEvent) {
        val context: Context = application
        when (event) {
            is MainUiEvent.RefreshSystemStates -> updateSystemStates(context)
            is MainUiEvent.ToggleAppLock -> {
                lockManager.updatePackageLock(event.packageName, event.shouldLock)
                uiState = uiState.copy(lockedApps = lockManager.getLockedPackages())
            }

            is MainUiEvent.ToggleSettingsLock -> {
                lockManager.updatePackageLock(ANDROID_SETTINGS_PACKAGE, event.shouldLock)
                uiState = uiState.copy(lockedApps = lockManager.getLockedPackages())
            }

            is MainUiEvent.ToggleUninstallLock -> {
                ANDROID_UNINSTALLER_PACKAGES.forEach {
                    lockManager.updatePackageLock(
                        it,
                        event.shouldLock
                    )
                }
                uiState = uiState.copy(lockedApps = lockManager.getLockedPackages())
            }

            is MainUiEvent.ToggleSelfLock -> {
                lockManager.updatePackageLock(KURA_PACKAGE, event.shouldLock)
                uiState = uiState.copy(lockedApps = lockManager.getLockedPackages())
            }

            is MainUiEvent.DeactivateAdmin -> {
                val dpm =
                    context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                val adminComponent = ComponentName(context, LockerAdminReceiver::class.java)
                dpm.removeActiveAdmin(adminComponent)
                uiState = uiState.copy(isAdminActive = false)
            }

            is MainUiEvent.LockAllApps -> {
                val allPackageNames = uiState.installedApps.map { it.packageName }
                allPackageNames.forEach { lockManager.updatePackageLock(it, true) }
                uiState = uiState.copy(lockedApps = lockManager.getLockedPackages())
            }

            is MainUiEvent.UnlockAllApps -> {
                val allPackagesNames = uiState.installedApps.map { it.packageName }
                allPackagesNames.forEach { lockManager.updatePackageLock(it, false) }
                uiState = uiState.copy(lockedApps = lockManager.getLockedPackages())
            }
        }
    }

    private fun updateSystemStates(context: Context) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(context, LockerAdminReceiver::class.java)
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val isSecure = keyguardManager.isDeviceSecure

        uiState = uiState.copy(
            isAccessibilityEnabled = context.isAccessibilityServiceEnabled(),
            canDrawOverlays = Settings.canDrawOverlays(context),
            isAdminActive = dpm.isAdminActive(adminComponent),
            lockedApps = lockManager.getLockedPackages(),
            isDeviceSecure = isSecure
        )
    }
}