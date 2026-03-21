package off.kys.kura

import android.content.Context
import android.content.Intent
import android.util.Log

private const val TAG = "BootReceiver"

class BootReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Note: Accessibility Services usually persist across reboots automatically.
            Log.d(TAG, "Boot completed, launching accessibility service")
        }
    }
}