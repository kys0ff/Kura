package off.kys.kura.core.registry

import android.content.Context
import androidx.core.content.edit
import off.kys.kura.core.common.constants.ANDROID_SETTINGS_PACKAGE

class AppLockRegistry(context: Context) {
    private val prefs = context.getSharedPreferences("locker_settings", Context.MODE_PRIVATE)

    companion object {
        // Use one constant so they don't get out of sync
        private const val SESSION_TIMEOUT_MS = 5 * 60 * 1000L
    }

    init {
        // Ensure Settings is always in the locked set
        if (!getLockedPackages().contains(ANDROID_SETTINGS_PACKAGE)) {
            setAppLocked(ANDROID_SETTINGS_PACKAGE, true)
        }
    }

    fun getLockedPackages(): Set<String> =
        prefs.getStringSet("locked_packages", emptySet()) ?: emptySet()

    // Simplified: Check if package is in the "to-lock" list AND if session is dead
    fun isAppLocked(packageName: String): Boolean {
        if (!getLockedPackages().contains(packageName)) return false
        return !isSessionValid(packageName)
    }

    fun setAppLocked(packageName: String, locked: Boolean) {
        val set = getLockedPackages().toMutableSet()
        if (locked) set.add(packageName) else set.remove(packageName)
        prefs.edit { putStringSet("locked_packages", set) }
    }

    fun saveUnlockTimestamp(packageName: String) {
        prefs.edit { putLong("unlock_$packageName", System.currentTimeMillis()) }
    }

    fun isSessionValid(packageName: String): Boolean {
        val lastActive = prefs.getLong("unlock_$packageName", 0L)
        return (System.currentTimeMillis() - lastActive) < SESSION_TIMEOUT_MS
    }

    fun clearAllSessions() {
        prefs.edit {
            prefs.all.keys.filter { it.startsWith("unlock_") }.forEach { key ->
                remove(key)
            }
        }
    }
}