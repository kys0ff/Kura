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
    private var currentPackageName: String = this.packageName

    override fun onServiceConnected() {
        prefs = AppLockRegistry(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return

            // 1. Ignore if the package hasn't changed (prevents redundant checks)
            if (packageName == currentPackageName) return

            // 2. Ignore our own app (The Locker)
            if (packageName == this.packageName) {
                currentPackageName = packageName
                return
            }

            // 3. Check if the app is locked in settings
            if (prefs.isAppLocked(packageName)) {
                launchLockScreen(packageName)
            }

            currentPackageName = packageName
        }
    }

    private fun launchLockScreen(packageName: String) {
        val intent = Intent(this, LockActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            putExtra("TARGET_PACKAGE", packageName)
        }
        startActivity(intent)
    }

    override fun onInterrupt() = Unit
}