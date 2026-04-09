package off.kys.kura.core.prefs

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.core.content.edit

class KuraPreferences(context: Context) {
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("kura_settings", Context.MODE_PRIVATE)

    companion object {
        const val KEY_LOCK_TIMEOUT = "lock_timeout"
        const val KEY_VIBRATION = "vibration_enabled"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_DYNAMIC_COLOR = "dynamic_color_enabled"
        const val KEY_RESET_ON_SCREEN_OFF = "reset_on_screen_off"
        const val KEY_LOCK_ANIMATION = "lock_animation_enabled"
    }

    // Timeout in milliseconds (Default: 1 minute)
    var lockTimeout: Long
        get() = prefs.getLong(KEY_LOCK_TIMEOUT, 60_000L)
        set(value) = prefs.edit { putLong(KEY_LOCK_TIMEOUT, value) }

    var vibrationEnabled: Boolean
        get() = prefs.getBoolean(KEY_VIBRATION, true)
        set(value) = prefs.edit { putBoolean(KEY_VIBRATION, value) }

    // Theme Mode (Default: SYSTEM)
    var themeMode: String
        get() = prefs.getString(KEY_THEME_MODE, "SYSTEM") ?: "SYSTEM"
        set(value) = prefs.edit { putString(KEY_THEME_MODE, value) }

    // Default to true only if the OS supports it (API 31+)
    var dynamicColorEnabled: Boolean
        get() = prefs.getBoolean(KEY_DYNAMIC_COLOR, Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        set(value) = prefs.edit { putBoolean(KEY_DYNAMIC_COLOR, value) }

    var resetOnScreenOff: Boolean
        get() = prefs.getBoolean(KEY_RESET_ON_SCREEN_OFF, true)
        set(value) = prefs.edit { putBoolean(KEY_RESET_ON_SCREEN_OFF, value) }

    var lockAnimationEnabled: Boolean
        get() = prefs.getBoolean(KEY_LOCK_ANIMATION, true)
        set(value) = prefs.edit { putBoolean(KEY_LOCK_ANIMATION, value) }
}