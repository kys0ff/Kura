package off.kys.kura.features.main.screen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import off.kys.kura.R

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
                    text = stringResource(R.string.action_required),
                    style = MaterialTheme.typography.titleSmall
                )
            }

            if (!isAccessibilityEnabled) {
                Text(
                    text = stringResource(R.string._1_enable_accessibility_service_to_detect_app_launches),
                    style = MaterialTheme.typography.bodySmall
                )
                Button(onClick = onGrantAccessibility, modifier = Modifier.padding(top = 4.dp)) {
                    Text(stringResource(R.string.enable_accessibility))
                }
            }

            if (!canDrawOverlays) {
                Text(
                    text = stringResource(R.string._2_allow_display_over_other_apps_to_show_the_lock_screen),
                    style = MaterialTheme.typography.bodySmall
                )
                Button(onClick = onGrantOverlay, modifier = Modifier.padding(top = 4.dp)) {
                    Text(stringResource(R.string.allow_overlay))
                }
            }
        }
    }
}