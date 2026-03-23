package off.kys.kura.features.lock.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import off.kys.kura.R
import off.kys.kura.core.designsystem.theme.KuraTheme
import off.kys.kura.features.lock.side_effect.LockSideEffect
import off.kys.kura.features.lock.viewmodel.LockViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class LockActivity : FragmentActivity() {

    private val viewModel: LockViewModel by viewModel()

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
            KuraTheme {
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                LockScreenContent()

                // Trigger prompt once when the view is ready
                LaunchedEffect(key1 = Unit) {
                    if (viewModel.targetPackage.isNotEmpty()) {
                        showBiometricPrompt(state.appName)
                    }
                }
            }
        }
    }

    @Composable
    private fun LockScreenContent() {
        val infiniteTransition = rememberInfiniteTransition(label = "backgroundPulse")
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ), label = "scale"
        )

        Box(modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)) {
            val accentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(accentColor, Color.Transparent),
                        center = center,
                        radius = (size.minDimension / 1.5f) * scale
                    ),
                    radius = (size.minDimension / 1.5f) * scale,
                    center = center
                )
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