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
import off.kys.kura.R
import off.kys.kura.core.registry.AppLockRegistry
import off.kys.kura.features.main.screen.components.AppItemRow
import off.kys.kura.features.main.screen.components.PermissionCard
import off.kys.kura.core.common.PackageManagerUtils
import off.kys.kura.core.common.extensions.isAccessibilityServiceEnabled
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLockerMainScreen(
    context: Context = LocalContext.current,
    pmUtils: PackageManagerUtils = koinInject(),
    prefs: AppLockRegistry = koinInject()
) {
    // State for permissions
    var isAccessibilityEnabled by remember { mutableStateOf(context.isAccessibilityServiceEnabled()) }
    var canDrawOverlays by remember { mutableStateOf(Settings.canDrawOverlays(context)) }

    // State for the app list
    val installedApps = remember { pmUtils.getInstalledApps() }
    var lockedApps by remember { mutableStateOf(prefs.getLockedPackages()) }

    // Refresh permissions when user returns to the app
    LaunchedEffect(key1 = Unit) {
        // This would ideally be triggered on lifecycle resume
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
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- APP LIST SECTION ---
            Text(
                text = stringResource(R.string.select_apps_to_lock),
                style = MaterialTheme.typography.titleMedium
            )
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(installedApps) { app ->
                    AppItemRow(
                        app = app,
                        isLocked = lockedApps.contains(app.packageName),
                        onToggle = { isChecked ->
                            prefs.setAppLocked(app.packageName, isChecked)
                            lockedApps = prefs.getLockedPackages()
                        }
                    )
                }
            }
        }
    }
}