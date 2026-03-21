@file:SuppressLint("AccessibilityPolicy")

package off.kys.kura.features.lock.services

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import off.kys.kura.core.registry.AppLockRegistry
import off.kys.kura.features.lock.activity.LockActivity

class LockerAccessibilityService : AccessibilityService() {
    private lateinit var prefs: AppLockRegistry
    private var lastPackageName: String? = null

    // Track the package we just unlocked to avoid splash screen loops
    companion object {
        var lastUnlockedPackage: String? = null
        var lastUnlockTime: Long = 0
    }

    override fun onServiceConnected() {
        prefs = AppLockRegistry(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return

            // 1. Internal transition check (Splash -> Main)
            // If the package is the same as the one we just processed, don't re-check
            if (packageName == lastPackageName) return

            // 2. Grace Period Check
            // If we unlocked this specific app in the last 3 seconds, ignore window changes
            val isInGracePeriod = packageName == lastUnlockedPackage &&
                    (System.currentTimeMillis() - lastUnlockTime) < 3000

            if (isInGracePeriod) {
                lastPackageName = packageName
                return
            }

            // 4. Check if locked (this includes your 5-minute logic)
            if (prefs.isAppLocked(packageName)) {
                launchLockScreen(packageName)
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