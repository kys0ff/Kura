package off.kys.kura.features.main.presentation.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import off.kys.kura.R
import off.kys.kura.core.data.model.AppInfo
import off.kys.kura.features.main.data.Badge

@Composable
fun AppSelectionHeader(
    areAllLocked: Boolean,
    onToggleAll: () -> Unit,
    activeFilters: Set<Badge>,
    onFilterChanged: (Set<Badge>) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.select_apps_to_lock),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )

        TextButton(onClick = onToggleAll) {
            Text(stringResource(if (areAllLocked) R.string.unlock_all else R.string.lock_all))
        }

        // The Filter Icon
        IconButton(onClick = { showDialog = true }) {
            val iconPainter = if (activeFilters.isNotEmpty())
                painterResource(R.drawable.baseline_filter_alt_24)
            else
                painterResource(R.drawable.outline_filter_alt_24)

            val iconTint = if (activeFilters.isNotEmpty())
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant


            Icon(
                painter = iconPainter,
                contentDescription = stringResource(R.string.filter),
                tint = iconTint
            )
        }
    }

    if (showDialog) {
        BadgeFilterDialog(
            initialSelected = activeFilters,
            onDismiss = {
                @Suppress("AssignedValueIsNeverRead")
                showDialog = false
            },
            onApply = onFilterChanged
        )
    }
}

@Composable
fun AppItemRow(
    app: AppInfo,
    badges: List<Badge>,
    isLocked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.name,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = app.packageName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (badges.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    badges.sortedBy { it.index }.forEach { badge ->
                        BadgeChip(badge)
                    }
                }
            }
        }

        Spacer(Modifier.size(8.dp))

        Switch(
            checked = isLocked,
            onCheckedChange = { onToggle(it) },
        )
    }
}

@Composable
private fun BadgeChip(badge: Badge) {
    val (labelRes, containerColor, contentColor) = when (badge) {
        Badge.CRUCIAL -> Triple(
            R.string.badge_crucial,
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )

        Badge.RECOMMENDED -> Triple(
            R.string.badge_recommended,
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )

        Badge.HAS_IN_APP_LOCK -> Triple(
            R.string.badge_has_lock,
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )

        Badge.NOT_RECOMMENDED -> Triple(
            R.string.badge_not_recommended,
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        )
    }

    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = containerColor,
        contentColor = contentColor
    ) {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontWeight = FontWeight.Bold
        )
    }
}