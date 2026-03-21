package off.kys.kura.features.main.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import off.kys.kura.core.common.constants.ANDROID_SETTINGS_PACKAGE
import off.kys.kura.core.data.model.AppInfo

@Composable
fun AppItemRow(
    app: AppInfo,
    isLocked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val isSystemSettings = app.packageName == ANDROID_SETTINGS_PACKAGE

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