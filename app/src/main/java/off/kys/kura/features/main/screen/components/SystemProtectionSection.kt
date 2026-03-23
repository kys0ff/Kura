package off.kys.kura.features.main.screen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import off.kys.kura.R

@Composable
fun SystemProtectionSection(
    isUninstallLocked: Boolean,
    onLockChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Section Header
        Text(
            text = stringResource(R.string.system_protection),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        // Protection Setting Card
        ProtectionToggleCard(
            title = stringResource(R.string.lock_uninstallers),
            description = stringResource(R.string.lock_uninstallers_desc),
            checked = isUninstallLocked,
            onCheckedChange = onLockChanged
        )

    }
}