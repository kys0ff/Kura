package off.kys.kura.features.main.presentation.event

import off.kys.kura.features.main.data.Badge

sealed class MainUiEvent {
    data class ToggleAppLock(val packageName: String, val shouldLock: Boolean) : MainUiEvent()
    data class ToggleSettingsLock(val shouldLock: Boolean) : MainUiEvent()
    data class ToggleUninstallLock(val shouldLock: Boolean) : MainUiEvent()
    data class ToggleSelfLock(val shouldLock: Boolean) : MainUiEvent()
    data class FilterApps(val badges: Set<Badge>) : MainUiEvent()
    object RefreshSystemStates : MainUiEvent()
    object DeactivateAdmin : MainUiEvent()
    object LockAllApps : MainUiEvent()
    object UnlockAllApps : MainUiEvent()
}