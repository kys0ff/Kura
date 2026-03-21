package off.kys.kura.core.common

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import off.kys.kura.core.common.constants.ANDROID_SETTINGS_PACKAGE
import off.kys.kura.core.data.model.AppInfo

class PackageManagerUtils(context: Context) {
    private val pm = context.packageManager

    fun getAppName(packageName: String): String? = try {
        val applicationInfo = pm.getApplicationInfo(packageName, 0)
        pm.getApplicationLabel(applicationInfo).toString()
    } catch (_: PackageManager.NameNotFoundException) {
        // Returns null if the package isn't installed on the device
        null
    }

    fun getInstalledApps(): List<AppInfo> = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 || it.packageName == ANDROID_SETTINGS_PACKAGE }
        .map { AppInfo(it.loadLabel(pm).toString(), it.packageName) }
        .sortedBy { it.name }

}