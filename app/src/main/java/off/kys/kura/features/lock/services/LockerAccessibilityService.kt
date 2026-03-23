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
import off.kys.kura.core.registry.AppLockRegistry
import off.kys.kura.features.lock.activity.LockActivity
import org.koin.android.ext.android.inject

private const val TAG = "LockerAccessibilityService"

class LockerAccessibilityService : AccessibilityService() {
    private val registry: AppLockRegistry by inject()
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

    // Track the package we just unlocked to avoid splash screen loops
    companion object {
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
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return

            if (packageName == "off.kys.kura.debug")
                Log.d(TAG, "onAccessibilityEvent: Self app detected")

            // 1. If this app is ALREADY unlocked and the user is still using it,
            // refresh the timestamp so the 5-minute timer "restarts" now.
            if (packageName == lastUnlockedPackage) {
                lastUnlockTime = System.currentTimeMillis()
                registry.saveUnlockTimestamp(packageName) // Keeps the session alive
                return
            }

            // 2. Standard Lock Logic
            Log.d(TAG, "onAccessibilityEvent: $packageName lock status: ${registry.isAppLocked(packageName)}")
            Log.d(TAG, "onAccessibilityEvent: $packageName has valid session: ${registry.isSessionValid(packageName)}")
            Log.d(TAG, "onAccessibilityEvent: $packageName lastUnlockedPackage: $lastUnlockedPackage")
            Log.d(TAG, "onAccessibilityEvent: $packageName lastUnlockTime: $lastUnlockTime")

            if (registry.isAppLocked(packageName)) {
                // Check if the PREVIOUS session actually expired
                if (!registry.isSessionValid(packageName)) {
                    launchLockScreen(packageName)
                } else {
                    // Session is still valid, update it because they just switched back to it
                    lastUnlockedPackage = packageName
                    lastUnlockTime = System.currentTimeMillis()
                }
            }

            lastPackageName = packageName
        }
    }

    override fun onInterrupt() {}

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