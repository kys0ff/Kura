package off.kys.kura.features.lock.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import off.kys.kura.R
import off.kys.kura.core.common.PackageResolver
import off.kys.kura.core.designsystem.theme.KuraTheme
import off.kys.kura.core.registry.LockSessionManager
import off.kys.kura.features.lock.services.LockerAccessibilityService
import org.koin.android.ext.android.inject

private const val TAG = "LockActivity"

class LockActivity : FragmentActivity() {
    private val pmUtils by inject<PackageResolver>()
    private val registry by inject<LockSessionManager>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            KuraTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                )
            }
        }

        // Ensure the activity doesn't show in Recents
        val targetPackage = intent.getStringExtra("TARGET_PACKAGE") ?: run {
            finish()
            return
        }
        showBiometricPrompt(targetPackage)
    }

    private fun showBiometricPrompt(packageName: String) {
        val executor = ContextCompat.getMainExecutor(this)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                // 1. Update the grace period in the service
                LockerAccessibilityService.lastUnlockedPackage = packageName
                LockerAccessibilityService.lastUnlockTime = System.currentTimeMillis()

                // 2. Save to the 5-minute registry
                registry.saveUnlockTimestamp(packageName)

                // 3. Close this activity
                finish()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                // Only kick home if it's a USER cancellation or a HARD error.
                // Ignore system-induced cancellations during transitions (code 5 or 10 usually)
                if (errorCode != BiometricPrompt.ERROR_CANCELED &&
                    errorCode != BiometricPrompt.ERROR_USER_CANCELED
                ) {
                    Log.e(TAG, "Authentication error: $errorCode, $errString")
                }

                if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                    errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON
                ) {
                    goHome()
                }
            }
        }

        val prompt = BiometricPrompt(this, executor, callback)
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.access_restricted))
            .setSubtitle(
                getString(
                    R.string.use_biometrics_to_access,
                    pmUtils.getAppName(packageName) ?: getString(R.string.this_app)
                )
            )
            // This allows Pattern/PIN fallback if Biometrics fail
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