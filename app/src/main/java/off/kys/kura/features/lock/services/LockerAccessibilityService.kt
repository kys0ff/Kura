@file:SuppressLint("AccessibilityPolicy")

package off.kys.kura.features.lock.services

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
import off.kys.kura.core.registry.LockSessionManager
import off.kys.kura.features.lock.activity.LockActivity
import org.koin.android.ext.android.inject

private const val TAG = "LockerAccessibilityService"

class LockerAccessibilityService : AccessibilityService() {
    private val registry: LockSessionManager by inject()
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
    private var lastPackageName: String? = null // Restore this!
    private var lastDiskWriteTime = 0L

    companion object {
        private const val DISK_WRITE_THROTTLE = 10_000L
        var lastUnlockedPackage: String? = null
        var lastUnlockTime: Long = 0
    }

    override fun onCreate() {
        super.onCreate()

        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)

        // For Android 14+ (API 34) support:
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
        // Safe-extract package name. Some system events have null packages.
        val currentPackage = event.packageName?.toString() ?: return

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                // 1. Safe "Exit" Save Logic
                // We use safe calls (?.) and avoid !! (double-bang)
                val last = lastPackageName
                val unlocked = lastUnlockedPackage

                if (last != null && unlocked != null && last == unlocked && currentPackage != unlocked) {
                    // User is leaving the protected app, save the timestamp now
                    registry.saveUnlockTimestamp(last)
                }

                handleWindowChange(event, currentPackage)
                lastPackageName = currentPackage
            }

            AccessibilityEvent.TYPE_VIEW_CLICKED,
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                // 2. Heartbeat logic
                if (currentPackage == lastUnlockedPackage) {
                    refreshSession(currentPackage)
                }
            }

            else -> Log.d(TAG, "onAccessibilityEvent: Unhandled event type: ${event.eventType}")
        }
    }

    private fun handleWindowChange(event: AccessibilityEvent, packageName: String) {
        // 1. Exit early if it's our own app's lock screen
        if (packageName == KURA_PACKAGE) {
            val className = event.className?.toString() ?: return
            if (className.contains("LockActivity")) return
        }

        // 2. If it's the app we just unlocked, keep the timer alive
        if (packageName == lastUnlockedPackage) {
            refreshSession(packageName)
            return
        }

        // 3. Check if the app is locked and if the session is still valid
        if (registry.isPackageLocked(packageName)) {
            if (!registry.isSessionValid(packageName)) {
                launchLockScreen(packageName)
            } else {
                // Valid session exists, just update the service's memory
                lastUnlockedPackage = packageName
                refreshSession(packageName)
            }
        }
    }

    override fun onInterrupt() {}

    private fun refreshSession(packageName: String) {
        val currentTime = System.currentTimeMillis()
        lastUnlockTime = currentTime // Update the fast local variable

        // Only write to SharedPreferences if it's been more than 30 seconds
        // since the last write to save battery and CPU.
        if (currentTime - lastDiskWriteTime > DISK_WRITE_THROTTLE) {
            registry.saveUnlockTimestamp(packageName)
            lastDiskWriteTime = currentTime
        }
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