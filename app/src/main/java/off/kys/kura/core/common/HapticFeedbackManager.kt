@file:Suppress("SameParameterValue")

package off.kys.kura.core.common

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class HapticFeedbackManager(context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager =
            context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    /**
     * A quick buzz for a successful action (e.g., unlocking).
     */
    fun success() {
        vibrate(duration = 50L, amplitude = 180)
    }

    /**
     * A double-buzz for an error (e.g., wrong fingerprint).
     */
    fun error() {
        val pattern = longArrayOf(0, 50, 100, 50) // Wait 0, Vibrate 50, Wait 100, Vibrate 50
        val amplitudes = intArrayOf(0, 255, 0, 255) // Max strength

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, -1)
        }
    }

    /**
     * Generic vibration with fallback for older APIs.
     */
    private fun vibrate(
        duration: Long,
        @SuppressLint("InlinedApi")
        amplitude: Int = VibrationEffect.DEFAULT_AMPLITUDE
    ) {
        if (vibrator?.hasVibrator() == false) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // API 26+ uses VibrationEffect
            val effect = VibrationEffect.createOneShot(duration, amplitude)
            vibrator?.vibrate(effect)
        } else {
            // API 24-25 uses legacy method
            @Suppress("DEPRECATION")
            vibrator?.vibrate(duration)
        }
    }
}