package off.kys.kura.features.main.screen

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.LifecycleResumeEffect
import off.kys.kura.R
import off.kys.kura.core.common.PackageManagerUtils
import off.kys.kura.core.common.extensions.isAccessibilityServiceEnabled
import off.kys.kura.core.registry.AppLockRegistry
import off.kys.kura.features.main.screen.components.AppItemRow
import off.kys.kura.features.main.screen.components.PermissionCard
import off.kys.kura.features.main.screen.components.SystemProtectionSection
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLockerMainScreen(
    context: Context = LocalContext.current,
    pmUtils: PackageManagerUtils = koinInject(),
    prefs: AppLockRegistry = koinInject()
) {
    // 1. Define Critical Packages
    val myPackage = context.packageName
    val settingsPackage = "com.android.settings"
    val uninstallerPackages = remember {
        listOf("com.google.android.packageinstaller", "com.android.packageinstaller")
    }

    // State for permissions
    var isAccessibilityEnabled by remember { mutableStateOf(context.isAccessibilityServiceEnabled()) }
    var canDrawOverlays by remember { mutableStateOf(Settings.canDrawOverlays(context)) }

    // State for the app list
    val installedApps = remember { pmUtils.getInstalledApps() }
    var lockedApps by remember { mutableStateOf(prefs.getLockedPackages()) }

    // Check if protection is active (if both are locked)
    val isUninstallLocked = remember(lockedApps) {
        uninstallerPackages.all { lockedApps.contains(it) }
    }

    fun updatePermissionsState() {
        isAccessibilityEnabled = context.isAccessibilityServiceEnabled()
        canDrawOverlays = Settings.canDrawOverlays(context)
    }

    // Enable Defaults on First Launch
    LaunchedEffect(key1 = Unit) {
        val currentLocked = prefs.getLockedPackages()
        // If the app is opened for the first time (or criticals are missing), lock them
        if (!currentLocked.contains(myPackage) || !currentLocked.contains(settingsPackage)) {
            prefs.setAppLocked(myPackage, true)
            prefs.setAppLocked(settingsPackage, true)
            uninstallerPackages.forEach { prefs.setAppLocked(it, true) }

            // Refresh local state
            lockedApps = prefs.getLockedPackages()
        }
    }

    // Refresh permissions when user returns to the app
    LifecycleResumeEffect(key1 = Unit) {
        updatePermissionsState()
        onPauseOrDispose { updatePermissionsState() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(8.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // --- PERMISSION SECTION ---
            if (!isAccessibilityEnabled || !canDrawOverlays) {
                PermissionCard(
                    isAccessibilityEnabled = isAccessibilityEnabled,
                    canDrawOverlays = canDrawOverlays,
                    onGrantAccessibility = {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        context.startActivity(intent)
                    },
                    onGrantOverlay = {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            "package:${context.packageName}".toUri()
                        )
                        context.startActivity(intent)
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))
            }


            LazyColumn(modifier = Modifier.weight(1f)) {
                item {
                    SystemProtectionSection(
                        isUninstallLocked = isUninstallLocked,
                        onLockChanged = { shouldLock ->
                            uninstallerPackages.forEach { pkg ->
                                prefs.setAppLocked(pkg, shouldLock)
                            }
                            lockedApps = prefs.getLockedPackages()
                        }
                    )
                }

                item {
                    // --- APP LIST SECTION ---
                    Text(
                        text = stringResource(R.string.select_apps_to_lock),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                items(installedApps) { app ->
                    // Logic: Settings and our own App should be "Locked" by default and hard to disable
                    val isCritical = app.packageName == settingsPackage || app.packageName == myPackage

                    AppItemRow(
                        app = app,
                        isLocked = if (isCritical) true else lockedApps.contains(app.packageName),
                        onToggle = { isChecked ->
                            // Prevent unlocking critical apps from the list
                            if (!isCritical) {
                                prefs.setAppLocked(app.packageName, isChecked)
                                lockedApps = prefs.getLockedPackages()
                            }
                        }
                    )
                }
            }
        }
    }
}