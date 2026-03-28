@file:SuppressLint("AccessibilityPolicy")

package off.kys.kura.features.lock.service

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import off.kys.kura.core.common.constants.KURA_PACKAGE
import off.kys.kura.core.prefs.KuraPreferences
import off.kys.kura.core.registry.LockSessionManager
import off.kys.kura.features.lock.presentation.activity.LockActivity
import org.koin.android.ext.android.inject

private const val TAG = "LockerAccessibilityService"

class LockerAccessibilityService : AccessibilityService() {
    private val registry: LockSessionManager by inject()
    private val appPrefs: KuraPreferences by inject()
    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "onReceive: Resetting all sessions")
            // Clear the grace period when screen turns off
            lastUnlockedPackage = null
            lastUnlockTime = 0L
            // Optionally clear the registry timestamps too
            registry.clearAllSessions()
        }
    }
    private var lastPackageName: String? = null

    companion object {
        private const val DISK_WRITE_THROTTLE: Long = 1_000L
        var lastUnlockedPackage: String? = null
        var lastUnlockTime: Long = 0
    }

    override fun onCreate() {
        super.onCreate()

        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            registerReceiver(screenOffReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(screenOffReceiver, filter)
        }
    }

    override fun onDestroy() {
        // ALWAYS unregister to prevent memory leaks and crashes
        try {
            unregisterReceiver(screenOffReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "onDestroy: Error unregistering receiver", e)
        }
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val currentPackage = event.packageName?.toString() ?: return

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                // Logic for switching apps (The Gatekeeper)
                handleWindowChange(event)
            }

            AccessibilityEvent.TYPE_VIEW_CLICKED,
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                if (currentPackage == lastUnlockedPackage) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastUnlockTime > DISK_WRITE_THROTTLE) {
                        lastUnlockTime = currentTime
                        refreshSession(currentPackage)
                    }
                }
            }

            else -> {}
        }
    }

    private fun handleWindowChange(event: AccessibilityEvent) {
        val currentPackage = event.packageName?.toString() ?: return
        val currentTime = System.currentTimeMillis()

        if (currentPackage == KURA_PACKAGE) {
            if (event.className?.toString()?.contains("LockActivity") == true) return
        }

        if (currentPackage == lastPackageName && currentPackage == lastUnlockedPackage) {
            lastUnlockTime = currentTime
            refreshSession(currentPackage)
            return
        }

        if (registry.isPackageLocked(currentPackage)) {
            // Fast Check: Is this the package we just unlocked, AND is the timer still valid?
            val isMemoryValid = (currentPackage == lastUnlockedPackage) &&
                    (currentTime - lastUnlockTime < appPrefs.lockTimeout)

            if (isMemoryValid) {
                // User is active and within time. Refresh the heartbeat.
                lastUnlockTime = currentTime
                refreshSession(currentPackage)
            } else {
                // Memory check failed (or it's a different app).
                // Fallback to Disk: Check if a valid session exists in SharedPreferences.
                if (registry.isSessionValid(currentPackage)) {
                    // Disk says it's okay! Sync memory so next event is a "Fast Check".
                    lastUnlockedPackage = currentPackage
                    lastUnlockTime = currentTime
                    refreshSession(currentPackage)
                } else {
                    // Both Memory and Disk failed. Trigger Lock.
                    launchLockScreen(currentPackage)
                }
            }
        }

        lastPackageName = currentPackage
    }

    override fun onInterrupt() {}

    private fun refreshSession(packageName: String) {
        registry.saveUnlockTimestamp(packageName)
    }

    private fun launchLockScreen(packageName: String) {
        val intent = Intent(this, LockActivity::class.java).apply {
            // FLAG_ACTIVITY_NEW_TASK is required from a Service.
            // CLEAR_TOP combined with singleInstance ensures we don't stack locks.
            // EXCLUDE_FROM_RECENTS hides the lock screen from the multitasking menu.
            // NO_ANIMATION makes the transition instant, preventing lifecycle lag.
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
                        Intent.FLAG_ACTIVITY_NO_ANIMATION
            )
            putExtra("TARGET_PACKAGE", packageName)
        }
        startActivity(intent)
    }
}