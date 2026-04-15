package off.kys.kura.features.lock.presentation.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import off.kys.kura.R
import off.kys.kura.core.designsystem.theme.KuraTheme
import off.kys.kura.core.prefs.KuraPreferences
import off.kys.kura.features.lock.presentation.screen.LockScreen
import off.kys.kura.features.lock.presentation.side_effect.LockSideEffect
import off.kys.kura.features.lock.presentation.viewmodel.LockViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class LockActivity : FragmentActivity() {

    private val viewModel: LockViewModel by viewModel()
    private val kuraPreferences: KuraPreferences by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle Side Effects (Navigation)
        lifecycleScope.launch {
            viewModel.effect.collect { effect ->
                when (effect) {
                    is LockSideEffect.Finish -> finish()
                    is LockSideEffect.GoHome -> goHome()
                }
            }
        }

        setContent {
            val themeMode = kuraPreferences.themeMode
            val dynamicColor = kuraPreferences.dynamicColorEnabled

            KuraTheme(
                darkTheme = when (themeMode) {
                    "LIGHT" -> false
                    "DARK" -> true
                    else -> isSystemInDarkTheme()
                },
                dynamicColor = dynamicColor
            ) {
                val state by viewModel.uiState.collectAsStateWithLifecycle()

                LockScreen(lockAppearance = kuraPreferences.lockAppearance)
                LaunchedEffect(key1 = Unit) {
                    if (viewModel.targetPackage.isNotEmpty()) {
                        showBiometricPrompt(state.appName)
                    } else {
                        viewModel.onAuthError(BiometricPrompt.ERROR_USER_CANCELED)
                    }
                }
            }
        }
    }

    private fun showBiometricPrompt(appName: String) {
        val executor = ContextCompat.getMainExecutor(this)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                viewModel.onAuthSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                viewModel.onAuthError(errorCode)
            }
        }

        val prompt = BiometricPrompt(this, executor, callback)
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.access_restricted))
            .setSubtitle(getString(R.string.use_biometrics_to_access, appName))
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        prompt.authenticate(info)
    }

    private fun goHome() {
        val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_HOME) }
        startActivity(intent)
        finish()
    }
}