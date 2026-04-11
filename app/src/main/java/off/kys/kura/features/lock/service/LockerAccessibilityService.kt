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
        private var lastSeenPackage: String? = null
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
                handleWindowChange(event)
                // Always update what we are currently looking at
                lastSeenPackage = currentPackage
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

        if (isIgnoredPackage(event)) return

        if (registry.isPackageLocked(currentPackage)) {
            val isContinuation = currentPackage == lastSeenPackage
            val timeSinceLastInteraction = currentTime - lastUnlockTime

            // The grace period only matters if we are actually SWITCHING apps.
            val isWithinGracePeriod = lastUnlockTime != 0L &&
                    (appPrefs.lockTimeout == -1L || timeSinceLastInteraction < maxOf(
                        appPrefs.lockTimeout,
                        2000L
                    ))

            when {
                // 1. Same app continuation (e.g., Fragment change, Toast, or just reading)
                // We don't lock as long as the user hasn't left the app (and no screen-off reset).
                isContinuation && lastUnlockTime != 0L -> {
                    refreshSession(currentPackage, currentTime)
                }

                // 2. Persistent session check (Registry)
                registry.isSessionValid(currentPackage) -> {
                    refreshSession(currentPackage, currentTime)
                }

                // 3. User switched apps, but did it within the timeout
                !isContinuation && isWithinGracePeriod -> {
                    refreshSession(currentPackage, currentTime)
                }

                // 4. Timeout expired or brand-new app entry
                else -> {
                    launchLockScreen(currentPackage)
                }
            }
        }
    }

    private fun isIgnoredPackage(event: AccessibilityEvent): Boolean {
        val packageName = event.packageName?.toString() ?: return false
        val className = event.className?.toString() ?: return false

        val isSystemLauncher = packageName == "com.android.systemui"

        if (packageName == KURA_PACKAGE)
            return className.contains("LockActivity")

        return isSystemLauncher
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
            // FLAG_ACTIVITY_NO_USER_ACTION tell's the system it's an automated event, not a user tap
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
                        Intent.FLAG_ACTIVITY_NO_ANIMATION or
                        Intent.FLAG_ACTIVITY_NO_USER_ACTION
            )
            putExtra("TARGET_PACKAGE", packageName)
        }
        startActivity(intent)
    }
}