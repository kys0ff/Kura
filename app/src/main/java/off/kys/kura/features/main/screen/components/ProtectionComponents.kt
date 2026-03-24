package off.kys.kura.features.main.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import off.kys.kura.R
import off.kys.kura.features.main.state.MainViewState

@Composable
fun SystemProtectionSection(
    state: MainViewState,
    onSettingsLockToggle: (Boolean) -> Unit,
    onUninstallLockChanged: (Boolean) -> Unit,
    onAdminToggle: (Boolean) -> Unit,
    onSelfLockToggle: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            stringResource(R.string.system_protection),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = 0.4f
                )
            )
        ) {
            Column {
                ProtectionToggleRow(
                    stringResource(R.string.lock_settings),
                    stringResource(R.string.lock_settings_desc),
                    state.isSettingsLocked,
                    onSettingsLockToggle
                )
                HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                ProtectionToggleRow(
                    stringResource(R.string.lock_uninstallers),
                    stringResource(R.string.lock_uninstallers_desc),
                    state.isUninstallLocked,
                    onUninstallLockChanged
                )
                HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                ProtectionToggleRow(
                    stringResource(R.string.advanced_protection),
                    stringResource(R.string.advanced_protection_desc),
                    state.isAdminActive,
                    onAdminToggle
                )
                HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                ProtectionToggleRow(
                    stringResource(R.string.self_lock),
                    stringResource(R.string.self_lock_desc),
                    state.isSelfLockEnabled,
                    onSelfLockToggle
                )
            }
        }
    }
}

@Composable
fun ProtectionToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}