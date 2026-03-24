package off.kys.kura.core.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class KuraPreferences(context: Context) {
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("kura_settings", Context.MODE_PRIVATE)

    companion object {
        const val KEY_LOCK_TIMEOUT = "lock_timeout"
        const val KEY_LOCK_ON_EXIT = "lock_on_exit"
        const val KEY_VIBRATION = "vibration_enabled"
        const val KEY_HIDE_ICON = "hide_app_icon"
    }

    // Timeout in milliseconds (Default: 1 minute)
    var lockTimeout: Long
        get() = prefs.getLong(KEY_LOCK_TIMEOUT, 60_000L)
        set(value) = prefs.edit { putLong(KEY_LOCK_TIMEOUT, value) }

    // Whether to lock immediately when app is swiped from Recents
    var lockOnExit: Boolean
        get() = prefs.getBoolean(KEY_LOCK_ON_EXIT, false)
        set(value) = prefs.edit { putBoolean(KEY_LOCK_ON_EXIT, value) }

    var vibrationEnabled: Boolean
        get() = prefs.getBoolean(KEY_VIBRATION, true)
        set(value) = prefs.edit { putBoolean(KEY_VIBRATION, value) }
        
    // Method to get a Flow-like update if you want to observe changes
    fun observeSettings(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }
}