@file:SuppressLint("AccessibilityPolicy")

package off.kys.kura.features.lock.services

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import off.kys.kura.core.registry.AppLockRegistry
import off.kys.kura.features.lock.activity.LockActivity
import org.koin.android.ext.android.inject

class LockerAccessibilityService : AccessibilityService() {
    private val registry: AppLockRegistry by inject()
    private var lastPackageName: String? = null

    // Track the package we just unlocked to avoid splash screen loops
    companion object {
        var lastUnlockedPackage: String? = null
        var lastUnlockTime: Long = 0
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return

            // 1. If this app is ALREADY unlocked and the user is still using it,
            // refresh the timestamp so the 5-minute timer "restarts" now.
            if (packageName == lastUnlockedPackage) {
                lastUnlockTime = System.currentTimeMillis()
                registry.saveUnlockTimestamp(packageName) // Keeps the session alive
                return
            }

            // 2. Standard Lock Logic
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
            // Use NEW_TASK but avoid CLEAR_TASK to keep the Biometric prompt stable
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("TARGET_PACKAGE", packageName)
        }
        startActivity(intent)
    }
}