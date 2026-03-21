package off.kys.kura.features.lock.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import off.kys.kura.R
import off.kys.kura.core.common.PackageManagerUtils
import off.kys.kura.core.registry.AppLockRegistry
import off.kys.kura.features.lock.services.LockerAccessibilityService
import org.koin.android.ext.android.inject

private const val TAG = "LockActivity"

class LockActivity : FragmentActivity() {
    private val pmUtils by inject<PackageManagerUtils>()
    private val registry by lazy { AppLockRegistry(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

                // 4. Resume the target app
                resumeTargetApp(packageName)
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

    private fun resumeTargetApp(packageName: String) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            // These flags ensure we bring the EXISTING task to the front
            // instead of starting a fresh instance or a new task entry.
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)

            try {
                startActivity(launchIntent)
            } catch (e: Exception) {
                // Fallback if the app was uninstalled or disabled in the last second
                Log.e(TAG, "resumeTargetApp: ", e)
            }
        }

        // 4. Finish the LockActivity
        finish()

        // 5. Remove the "closing" animation so it looks like the LockActivity
        // simply vanished, revealing the app instantly.
        @Suppress("DEPRECATION")
        overridePendingTransition(0, 0)
    }

    private fun goHome() {
        val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_HOME) }
        startActivity(intent)
        finish()
    }
}