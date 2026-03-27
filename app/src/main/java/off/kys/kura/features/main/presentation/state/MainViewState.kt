package off.kys.kura.features.main.presentation.state

import off.kys.kura.core.common.constants.ANDROID_SETTINGS_PACKAGE
import off.kys.kura.core.common.constants.ANDROID_UNINSTALLER_PACKAGES
import off.kys.kura.core.common.constants.KURA_PACKAGE
import off.kys.kura.core.data.model.AppInfo
import off.kys.kura.features.main.data.Badge

data class MainViewState(
    val isAccessibilityEnabled: Boolean = true,
    val canDrawOverlays: Boolean = true,
    val isAdminActive: Boolean = false,
    val lockedApps: Set<String> = emptySet(),
    val filteredApps: List<AppInfo> = emptyList(),
    val installedApps: List<AppInfo> = emptyList(),
    val isDeviceSecure: Boolean = true,
    val activeFilters: Set<Badge> = emptySet(),
) {
    val isSettingsLocked: Boolean = lockedApps.contains(ANDROID_SETTINGS_PACKAGE)
    val isSelfLockEnabled: Boolean = lockedApps.contains(KURA_PACKAGE)
    val isUninstallLocked: Boolean = ANDROID_UNINSTALLER_PACKAGES.all { lockedApps.contains(it) }
    val areAllLocked: Boolean =
        installedApps.isNotEmpty() && installedApps.all { lockedApps.contains(it.packageName) }
}