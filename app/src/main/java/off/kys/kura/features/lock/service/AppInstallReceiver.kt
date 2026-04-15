package off.kys.kura.features.lock.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import off.kys.kura.R
import off.kys.kura.core.common.PackageResolver
import off.kys.kura.core.common.constants.KURA_PACKAGE
import off.kys.kura.core.common.extensions.getNotificationService
import off.kys.kura.core.prefs.KuraPreferences
import off.kys.kura.core.registry.LockSessionManager
import org.koin.java.KoinJavaComponent.inject

class AppInstallReceiver : BroadcastReceiver() {

    private val registry: LockSessionManager by inject(LockSessionManager::class.java)
    private val packageResolver: PackageResolver by inject(PackageResolver::class.java)
    private val prefs: KuraPreferences by inject(KuraPreferences::class.java)

    override fun onReceive(context: Context?, intent: Intent?) {
        val ctx = context ?: return
        val action = intent?.action ?: return

        if (action == "off.kys.kura.ACTION_LOCK_APP") {
            val target = intent.getStringExtra("EXTRA_PACKAGE") ?: return
            registry.updatePackageLock(target, true)
            ctx.getNotificationService().cancel(target.hashCode())
            return
        }

        if (action == Intent.ACTION_PACKAGE_ADDED) {
            if (!prefs.newAppAlertsEnabled) return

            val pkgName = intent.data?.schemeSpecificPart ?: return

            if (registry.isPackageLocked(pkgName)) return

            val isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)
            if (isReplacing || pkgName == KURA_PACKAGE) return

            showInstallNotification(ctx, pkgName)
        }
    }

    private fun showInstallNotification(context: Context, pkgName: String) {
        val channelId = "app_install_alerts"
        val nm = context.getNotificationService()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                context.getString(R.string.new_app_alerts),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notifications_for_newly_installed_apps)
            }
            nm.createNotificationChannel(channel)
        }

        val lockIntent = Intent(context, AppInstallReceiver::class.java).apply {
            action = "off.kys.kura.ACTION_LOCK_APP"
            putExtra("EXTRA_PACKAGE", pkgName)
        }
        val lockPendingIntent = PendingIntent.getBroadcast(
            context, pkgName.hashCode(), lockIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle(context.getString(R.string.new_app_detected))
            .setContentText(
                context.getString(
                    R.string.should_be_locked,
                    packageResolver.getAppName(pkgName) ?: pkgName
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM) // Helps bypass some DnD settings
            .setAutoCancel(true)
            .addAction(
                android.R.drawable.ic_lock_lock,
                context.getString(R.string.lock_app),
                lockPendingIntent
            )

        nm.notify(pkgName.hashCode(), builder.build())
    }

}