package off.kys.kura.core.common

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import off.kys.kura.core.common.constants.ANDROID_SETTINGS_PACKAGE
import off.kys.kura.core.common.constants.KURA_PACKAGE
import off.kys.kura.core.data.model.AppInfo

class PackageResolver(context: Context) {
    private val pm = context.packageManager

    fun getAppName(packageName: String): String? = try {
        val applicationInfo = pm.getApplicationInfo(packageName, 0)
        pm.getApplicationLabel(applicationInfo).toString()
    } catch (_: PackageManager.NameNotFoundException) {
        null
    }

    fun getInstalledApps(): List<AppInfo> {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        // Fetch all activities that can be launched by the user
        val resolveInfos = pm.queryIntentActivities(intent, 0)

        return resolveInfos
            .asSequence()
            .map { it.activityInfo.applicationInfo }
            .distinctBy { it.packageName }
            .filter { it.packageName != KURA_PACKAGE && it.packageName != ANDROID_SETTINGS_PACKAGE }
            .map { AppInfo(it.loadLabel(pm).toString(), it.packageName) }
            .sortedBy { it.name }
            .toList()
    }
}