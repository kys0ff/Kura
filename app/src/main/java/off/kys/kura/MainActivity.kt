package off.kys.kura

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import off.kys.kura.ui.theme.KuraTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KuraTheme {
                AppLockerMainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLockerMainScreen() {
    val context = LocalContext.current
    val prefs = remember { LockerPrefs(context) }

    // State for permissions
    var isAccessibilityEnabled by remember { mutableStateOf(isAccessibilityServiceEnabled(context)) }
    var canDrawOverlays by remember { mutableStateOf(Settings.canDrawOverlays(context)) }

    // State for the app list
    val installedApps = remember { getInstalledApps(context) }
    var lockedApps by remember { mutableStateOf(prefs.getLockedPackages()) }

    // Refresh permissions when user returns to the app
    LaunchedEffect(Unit) {
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
            Text("Select Apps to Lock", style = MaterialTheme.typography.titleMedium)
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

@Composable
fun PermissionCard(
    isAccessibilityEnabled: Boolean,
    canDrawOverlays: Boolean,
    onGrantAccessibility: () -> Unit,
    onGrantOverlay: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.round_warning_24),
                    contentDescription = null,
                    tint = Color.Red
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    stringResource(R.string.action_required),
                    style = MaterialTheme.typography.titleSmall
                )
            }

            if (!isAccessibilityEnabled) {
                Text(
                    stringResource(R.string._1_enable_accessibility_service_to_detect_app_launches),
                    style = MaterialTheme.typography.bodySmall
                )
                Button(onClick = onGrantAccessibility, modifier = Modifier.padding(top = 4.dp)) {
                    Text(stringResource(R.string.enable_accessibility))
                }
            }

            if (!canDrawOverlays) {
                Text(
                    stringResource(R.string._2_allow_display_over_other_apps_to_show_the_lock_screen),
                    style = MaterialTheme.typography.bodySmall
                )
                Button(onClick = onGrantOverlay, modifier = Modifier.padding(top = 4.dp)) {
                    Text(stringResource(R.string.allow_overlay))
                }
            }
        }
    }
}

@Composable
fun AppItemRow(app: AppInfo, isLocked: Boolean, onToggle: (Boolean) -> Unit) {
    val isSystemSettings = app.packageName == "com.android.settings"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(app.name, style = MaterialTheme.typography.bodyLarge)
            Text(app.packageName, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }

        // If it's the Settings app, we force it to be "On" and disable the switch
        Switch(
            checked = if (isSystemSettings) true else isLocked,
            onCheckedChange = { if (!isSystemSettings) onToggle(it) },
            enabled = !isSystemSettings
        )
    }
}

// --- HELPER FUNCTIONS ---

fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    val enabledServices =
        am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
    return enabledServices.any { it.resolveInfo.serviceInfo.packageName == context.packageName }
}

fun getInstalledApps(context: Context): List<AppInfo> {
    val pm = context.packageManager
    return pm.getInstalledApplications(PackageManager.GET_META_DATA)
        .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 || it.packageName == "com.android.settings" }
        .map { AppInfo(it.loadLabel(pm).toString(), it.packageName) }
        .sortedBy { it.name }
}

data class AppInfo(val name: String, val packageName: String)