package off.kys.kura.core.registry

import android.content.Context
import androidx.core.content.edit
import off.kys.kura.core.prefs.KuraPreferences

class LockSessionManager(
    context: Context,
    private val appPrefs: KuraPreferences
) {
    private val prefs = context.getSharedPreferences("locker_settings", Context.MODE_PRIVATE)

    fun getLockedPackages(): Set<String> =
        prefs.getStringSet("locked_packages", emptySet()) ?: emptySet()

    fun isPackageLocked(packageName: String): Boolean = getLockedPackages().contains(packageName)

    fun updatePackageLock(packageName: String, locked: Boolean) {
        val set = getLockedPackages().toMutableSet()
        if (locked) set.add(packageName) else set.remove(packageName)
        prefs.edit { putStringSet("locked_packages", set) }
    }

    fun saveUnlockTimestamp(packageName: String) {
        prefs.edit { putLong("unlock_$packageName", System.currentTimeMillis()) }
    }

    fun isSessionValid(packageName: String): Boolean {
        val lastActive = prefs.getLong("unlock_$packageName", 0L)

        // 1. If it was never unlocked or was cleared, it's definitely not valid.
        if (lastActive == 0L) return false

        // 2. If timeout is "Never" (-1), it's always valid if lastActive > 0.
        // Otherwise, check the actual duration.
        return appPrefs.lockTimeout == -1L || (System.currentTimeMillis() - lastActive) < appPrefs.lockTimeout
    }

    fun clearAllSessions() {
        prefs.edit {
            prefs.all.keys.filter { it.startsWith("unlock_") }.forEach { key ->
                remove(key)
            }
        }
    }
}