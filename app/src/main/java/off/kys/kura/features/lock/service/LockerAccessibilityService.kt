@file:SuppressLint("AccessibilityPolicy", "UnspecifiedRegisterReceiverFlag")
@file:Suppress("NOTHING_TO_INLINE")

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
    private val installReceiver: AppInstallReceiver by inject()
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

        val installFilter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addDataScheme("package")
        }
        val buttonFilter = IntentFilter("off.kys.kura.ACTION_LOCK_APP")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(installReceiver, installFilter, RECEIVER_NOT_EXPORTED)
            registerReceiver(installReceiver, buttonFilter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(installReceiver, installFilter)
            registerReceiver(installReceiver, buttonFilter)
        }
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(screenOffReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "onDestroy: Error unregistering screenOffReceiver", e)
        }
        try {
            unregisterReceiver(installReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "onDestroy: Error unregistering installReceiver", e)
        }
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val currentPackage = event.packageName?.toString() ?: return

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                if (registry.isPackageLocked(currentPackage)) {
                    handleWindowChange(event)
                } else {
                    if (!isKuraLock(event)) {
                        if (isSystemOrLauncher(currentPackage)) {
                            lastUnlockedPackage = null
                            lastUnlockTime = 0L
                        }
                        lastSeenPackage = currentPackage
                    }
                }
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

    private inline fun isKuraLock(event: AccessibilityEvent): Boolean {
        val pkg = event.packageName?.toString().orEmpty()
        val cls = event.className?.toString().orEmpty()
        return pkg == KURA_PACKAGE && cls.contains("LockActivity")
    }

    private inline fun isSystemOrLauncher(packageName: String): Boolean =
        packageName == "com.android.systemui"

    private fun handleWindowChange(event: AccessibilityEvent) {
        val currentPackage = event.packageName?.toString() ?: return
        val currentTime = System.currentTimeMillis()

        if (registry.isPackageLocked(currentPackage)) {
            val isContinuation = currentPackage == lastSeenPackage
            val isSameAsLastUnlocked = currentPackage == lastUnlockedPackage
            val timeSinceLastInteraction = currentTime - lastUnlockTime
            val isWithinGracePeriod = isSameAsLastUnlocked && lastUnlockTime != 0L &&
                    (appPrefs.lockTimeout == -1L || timeSinceLastInteraction < maxOf(
                        appPrefs.lockTimeout,
                        2000L
                    ))

            when {
                isContinuation && lastUnlockTime != 0L -> refreshSession(
                    currentPackage,
                    currentTime
                )

                isWithinGracePeriod -> refreshSession(currentPackage, currentTime)

                registry.isSessionValid(currentPackage) -> refreshSession(
                    currentPackage,
                    currentTime
                )

                else -> launchLockScreen(currentPackage)
            }
        }
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