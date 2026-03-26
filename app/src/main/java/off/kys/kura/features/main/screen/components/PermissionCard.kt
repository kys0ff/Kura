package off.kys.kura.features.main.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import off.kys.kura.R
import off.kys.kura.features.main.state.MainViewState

@Composable
fun PermissionCard(
    state: MainViewState,
    onGrantAccessibility: () -> Unit,
    onGrantOverlay: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(painterResource(R.drawable.round_warning_24), null, tint = MaterialTheme.colorScheme.error)
                Text(stringResource(R.string.action_required), style = MaterialTheme.typography.titleSmall)
            }
            if (!state.isAccessibilityEnabled) {
                PermissionItem(
                    text = stringResource(R.string._1_enable_accessibility_service_to_detect_app_launches),
                    buttonText = stringResource(R.string.enable_accessibility),
                    onClick = onGrantAccessibility
                )
            }
            if (!state.canDrawOverlays) {
                PermissionItem(
                    text = stringResource(R.string._2_allow_display_over_other_apps_to_show_the_lock_screen),
                    buttonText = stringResource(R.string.allow_overlay),
                    onClick = onGrantOverlay
                )
            }
        }
    }
}

@Composable
private fun PermissionItem(text: String, buttonText:String, onClick: () -> Unit) {
    Column {
        Text(text, style = MaterialTheme.typography.bodySmall)
        Button(onClick = onClick, modifier = Modifier.padding(top = 4.dp)) {
            Text(buttonText)
        }
    }
}