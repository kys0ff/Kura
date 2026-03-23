package off.kys.kura.core.registry

import android.content.Context
import androidx.core.content.edit
import off.kys.kura.core.common.constants.ANDROID_SETTINGS_PACKAGE

class AppLockRegistry(context: Context) {
    private val prefs = context.getSharedPreferences("locker_settings", Context.MODE_PRIVATE)

    init {
        if (ANDROID_SETTINGS_PACKAGE !in getLockedPackages())
            setAppLocked(ANDROID_SETTINGS_PACKAGE, true)
    }

    fun getLockedPackages(): Set<String> =
        prefs.getStringSet("locked_packages", emptySet()) ?: emptySet()

    fun isAppLocked(packageName: String): Boolean {
        val lockedSet = prefs.getStringSet("locked_packages", emptySet()) ?: emptySet()
        if (!lockedSet.contains(packageName)) return false

        // Check 5-minute timeout
        val lastUnlock = prefs.getLong("unlock_$packageName", 0L)
        val fiveMinutes = 15 * 60 * 1000
        return (System.currentTimeMillis() - lastUnlock) > fiveMinutes
    }

    fun setAppLocked(packageName: String, locked: Boolean) {
        val set =
            prefs.getStringSet("locked_packages", emptySet())?.toMutableSet() ?: mutableSetOf()
        if (locked) set.add(packageName) else set.remove(packageName)
        prefs.edit { putStringSet("locked_packages", set) }
    }

    fun saveUnlockTimestamp(packageName: String) {
        prefs.edit { putLong("unlock_$packageName", System.currentTimeMillis()) }
    }

    fun isSessionValid(packageName: String): Boolean {
        val lastActive = prefs.getLong("unlock_$packageName", 0L)
        val fiveMinutes = 5 * 60 * 1000L

        // If the difference between NOW and the LAST time we saw the app
        // is less than 5 minutes, it's still "unlocked".
        return (System.currentTimeMillis() - lastActive) < fiveMinutes
    }
}