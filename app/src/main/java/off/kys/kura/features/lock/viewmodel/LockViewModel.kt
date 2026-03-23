package off.kys.kura.features.lock.viewmodel

import androidx.biometric.BiometricPrompt
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import off.kys.kura.core.common.PackageResolver
import off.kys.kura.core.registry.LockSessionManager
import off.kys.kura.features.lock.services.LockerAccessibilityService
import off.kys.kura.features.lock.side_effect.LockSideEffect
import off.kys.kura.features.lock.state.LockViewState

class LockViewModel(
    private val lockManager: LockSessionManager,
    packageResolver: PackageResolver,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Retrieve package from Intent extras via SavedStateHandle
    val targetPackage: String = savedStateHandle["TARGET_PACKAGE"] ?: ""

    private val _uiState = MutableStateFlow(LockViewState())
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<LockSideEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        if (targetPackage.isEmpty()) {
            sendEffect(LockSideEffect.Finish)
        } else {
            val name = packageResolver.getAppName(targetPackage) ?: "this app"
            _uiState.update { it.copy(appName = name) }
        }
    }

    fun onAuthSuccess() {
        // 1. Update service grace period
        LockerAccessibilityService.lastUnlockedPackage = targetPackage
        LockerAccessibilityService.lastUnlockTime = System.currentTimeMillis()

        // 2. Save to registry
        lockManager.saveUnlockTimestamp(targetPackage)

        sendEffect(LockSideEffect.Finish)
    }

    fun onAuthError(errorCode: Int) {
        if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
            errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
            sendEffect(LockSideEffect.GoHome)
        }
    }

    private fun sendEffect(sideEffect: LockSideEffect) {
        viewModelScope.launch { _effect.send(sideEffect) }
    }
}