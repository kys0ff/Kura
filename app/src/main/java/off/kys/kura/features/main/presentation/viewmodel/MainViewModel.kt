package off.kys.kura.features.main.presentation.viewmodel

import android.app.Application
import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import off.kys.kura.core.admin.LockerAdminReceiver
import off.kys.kura.core.common.PackageResolver
import off.kys.kura.core.common.constants.ANDROID_SETTINGS_PACKAGE
import off.kys.kura.core.common.constants.ANDROID_UNINSTALLER_PACKAGES
import off.kys.kura.core.common.constants.KURA_PACKAGE
import off.kys.kura.core.common.extensions.isAccessibilityServiceEnabled
import off.kys.kura.core.registry.LockSessionManager
import off.kys.kura.features.main.data.Badge
import off.kys.kura.features.main.domain.BadgeLoader
import off.kys.kura.features.main.presentation.event.MainUiEvent
import off.kys.kura.features.main.presentation.state.MainViewState

class MainViewModel(
    private val application: Application,
    private val resolver: PackageResolver,
    private val lockManager: LockSessionManager,
    private val badgeLoader: BadgeLoader
) : AndroidViewModel(application) {

    var uiState by mutableStateOf(MainViewState())
        private set

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        val allApps = resolver.getInstalledApps()
        uiState = uiState.copy(
            installedApps = allApps,
            filteredApps = allApps,
            lockedApps = lockManager.getLockedPackages()
        )
        badgeLoader.loadJson()
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

            is MainUiEvent.FilterApps -> {
                val newFilters = event.badges
                val filtered = if (newFilters.isEmpty()) {
                    uiState.installedApps
                } else {
                    uiState.installedApps.filter { app ->
                        badgeLoader.getBadges(app.packageName).any { it in newFilters }
                    }
                }

                uiState = uiState.copy(
                    activeFilters = newFilters,
                    filteredApps = filtered
                )
                syncLockState()
            }

            is MainUiEvent.DeactivateAdmin -> {
                val dpm =
                    context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                val adminComponent = ComponentName(context, LockerAdminReceiver::class.java)
                dpm.removeActiveAdmin(adminComponent)
                uiState = uiState.copy(isAdminActive = false)
            }

            is MainUiEvent.LockAllApps -> {
                if (uiState.filteredApps.isNotEmpty()) {
                    uiState.filteredApps.forEach { app ->
                        lockManager.updatePackageLock(app.packageName, true)
                    }
                } else {
                    uiState.installedApps.forEach { app ->
                        lockManager.updatePackageLock(app.packageName, true)
                    }
                }
                syncLockState()
            }

            is MainUiEvent.UnlockAllApps -> {
                if (uiState.filteredApps.isNotEmpty()) {
                    uiState.filteredApps.forEach { app ->
                        lockManager.updatePackageLock(app.packageName, false)
                    }
                } else {
                    uiState.installedApps.forEach { app ->
                        lockManager.updatePackageLock(app.packageName, false)
                    }
                }
                syncLockState()
            }
        }
    }

    fun getBadges(packageName: String): List<Badge> = badgeLoader.getBadges(packageName)

    private fun syncLockState() {
        val currentLocked = lockManager.getLockedPackages()

        uiState = uiState.copy(lockedApps = currentLocked)
    }

    private fun updateSystemStates(context: Context) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(context, LockerAdminReceiver::class.java)
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val isSecure = keyguardManager.isDeviceSecure
        val isNotificationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true

        uiState = uiState.copy(
            isAccessibilityEnabled = context.isAccessibilityServiceEnabled(),
            canDrawOverlays = Settings.canDrawOverlays(context),
            isAdminActive = dpm.isAdminActive(adminComponent),
            lockedApps = lockManager.getLockedPackages(),
            isDeviceSecure = isSecure,
            isNotificationPermissionGranted = isNotificationGranted
        )
    }
}