package off.kys.kura.features.main.state

import off.kys.kura.core.common.constants.ANDROID_SETTINGS_PACKAGE
import off.kys.kura.core.common.constants.ANDROID_UNINSTALLER_PACKAGES
import off.kys.kura.core.common.constants.KURA_PACKAGE
import off.kys.kura.core.data.model.AppInfo

data class MainViewState(
    val isAccessibilityEnabled: Boolean = false,
    val canDrawOverlays: Boolean = false,
    val isAdminActive: Boolean = false,
    val lockedApps: Set<String> = emptySet(),
    val installedApps: List<AppInfo> = emptyList() // Replace AppInfo with your actual model
) {
    val isSettingsLocked: Boolean = lockedApps.contains(ANDROID_SETTINGS_PACKAGE)
    val isSelfLockEnabled: Boolean = lockedApps.contains(KURA_PACKAGE)
    val isUninstallLocked: Boolean = ANDROID_UNINSTALLER_PACKAGES.all { lockedApps.contains(it) }
}