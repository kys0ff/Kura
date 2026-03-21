package off.kys.kura

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class LockActivity : FragmentActivity() { // Must be FragmentActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val targetPackage = intent.getStringExtra("TARGET_PACKAGE") ?: ""
        showBiometricPrompt(targetPackage)
    }

    private fun showBiometricPrompt(packageName: String) {
        val executor = ContextCompat.getMainExecutor(this)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                LockerPrefs(this@LockActivity).saveUnlockTimestamp(packageName)
                finish() // Success: Let them into the app
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                goHome() // Failed/Canceled: Kick them out
            }
        }

        val prompt = BiometricPrompt(this, executor, callback)
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.access_restricted))
            .setSubtitle(
                getString(
                    R.string.use_biometrics_to_access,
                    getAppNameFromPackage(packageName) ?: getString(R.string.this_app)
                )
            )
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        prompt.authenticate(info)
    }

    private fun getAppNameFromPackage(packageName: String): String? = try {
        val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
        packageManager.getApplicationLabel(applicationInfo).toString()
    } catch (_: PackageManager.NameNotFoundException) {
        // Returns null if the package isn't installed on the device
        null
    }

    private fun goHome() {
        val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_HOME) }
        startActivity(intent)
        finish()
    }
}