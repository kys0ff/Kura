@file:Suppress("NOTHING_TO_INLINE")

package off.kys.kura.core.common.extensions

import android.app.NotificationManager
import android.content.Context

inline fun Context.getNotificationService(): NotificationManager =
    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager