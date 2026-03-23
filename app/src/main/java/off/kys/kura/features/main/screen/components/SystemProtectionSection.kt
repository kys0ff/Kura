package off.kys.kura.features.main.screen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import off.kys.kura.R

@Composable
fun SystemProtectionSection(
    isUninstallLocked: Boolean,
    isAdminActive: Boolean,
    isSelfLockEnabled: Boolean,
    isSettingsLocked: Boolean, // Add this parameter
    onUninstallLockChanged: (Boolean) -> Unit,
    onAdminToggle: (Boolean) -> Unit,
    onSelfLockToggle: (Boolean) -> Unit,
    onSettingsLockToggle: (Boolean) -> Unit, // Add this callback
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.system_protection),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
        ) {
            Column {
                // 1. Lock System Settings
                ProtectionToggleRow(
                    title = stringResource(R.string.lock_settings),
                    description = stringResource(R.string.lock_settings_desc),
                    checked = isSettingsLocked,
                    onCheckedChange = onSettingsLockToggle
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // 2. Anti-Uninstall (Package Installers)
                ProtectionToggleRow(
                    title = stringResource(R.string.lock_uninstallers),
                    description = stringResource(R.string.lock_uninstallers_desc),
                    checked = isUninstallLocked,
                    onCheckedChange = onUninstallLockChanged
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // 3. Device Admin
                ProtectionToggleRow(
                    title = stringResource(R.string.advanced_protection),
                    description = stringResource(R.string.advanced_protection_desc),
                    checked = isAdminActive,
                    onCheckedChange = onAdminToggle
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // 4. Self-Lock
                ProtectionToggleRow(
                    title = stringResource(R.string.self_lock),
                    description = stringResource(R.string.self_lock_desc),
                    checked = isSelfLockEnabled,
                    onCheckedChange = onSelfLockToggle
                )
            }
        }
    }
}