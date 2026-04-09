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
            // Check the preference here
            if (appPrefs.resetOnScreenOff) {
                Log.d(TAG, "Screen Off: Resetting sessions per user preference")
                lastUnlockedPackage = null
                lastUnlockTime = 0L
                registry.clearAllSessions()
            } else {
                Log.d(TAG, "Screen Off: Keeping sessions active (reset disabled)")
            }
        }
    }

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
                        refreshSession(currentPackage, currentTime)
                    }
                }
            }

            else -> {}
        }
    }

    private fun handleWindowChange(event: AccessibilityEvent) {
        val currentPackage = event.packageName?.toString() ?: return
        val currentTime = System.currentTimeMillis()

        // 1. Ignore if it's our own lock screen or the system launcher
        if (isIgnoredPackage(event)) return

        // 2. The "User was just here" Check
        val timeSinceLastInteraction = currentTime - lastUnlockTime
        val isWithinGracePeriod = timeSinceLastInteraction < appPrefs.lockTimeout

        if (registry.isPackageLocked(currentPackage)) {
            if (currentPackage == lastUnlockedPackage && isWithinGracePeriod) {
                // User just came back or is still here.
                refreshSession(currentPackage, currentTime)
            } else if (registry.isSessionValid(currentPackage)) {
                // Disk session is still valid.
                refreshSession(currentPackage, currentTime)
            } else {
                // Truly expired or a brand-new app.
                launchLockScreen(currentPackage)
            }
        }
    }

    private fun isIgnoredPackage(event: AccessibilityEvent): Boolean {
        if (event.packageName == KURA_PACKAGE) {
            return event.className?.toString()?.contains("LockActivity") == true
        }
        return false
    }

    override fun onInterrupt() {}

    private fun refreshSession(
        packageName: String,
        currentTime: Long = System.currentTimeMillis()
    ) {
        lastUnlockTime = currentTime
        lastUnlockedPackage = packageName
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