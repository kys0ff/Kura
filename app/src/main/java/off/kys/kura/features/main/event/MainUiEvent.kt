package off.kys.kura.features.main.event

sealed class MainUiEvent {
    data class ToggleAppLock(val packageName: String, val shouldLock: Boolean) : MainUiEvent()
    data class ToggleSettingsLock(val shouldLock: Boolean) : MainUiEvent()
    data class ToggleUninstallLock(val shouldLock: Boolean) : MainUiEvent()
    data class ToggleSelfLock(val shouldLock: Boolean) : MainUiEvent()
    object RefreshSystemStates : MainUiEvent()
    object DeactivateAdmin : MainUiEvent()
}