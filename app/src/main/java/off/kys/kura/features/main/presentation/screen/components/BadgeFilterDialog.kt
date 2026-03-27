package off.kys.kura.features.main.presentation.screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import off.kys.kura.R
import off.kys.kura.features.main.data.Badge
import java.util.Locale

@Composable
fun BadgeFilterDialog(
    initialSelected: Set<Badge>,
    onDismiss: () -> Unit,
    onApply: (Set<Badge>) -> Unit
) {
    // Temporary state to hold changes before clicking "Apply"
    var tempSelected by remember { mutableStateOf(initialSelected) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.filter_by_badge)) },
        text = {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Badge.entries.forEach { badge ->
                    val isSelected = badge in tempSelected
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            tempSelected =
                                if (isSelected) tempSelected - badge else tempSelected + badge
                        },
                        label = {
                            Text(
                                text = badge.name.replace("_", " ").lowercase()
                                    .replaceFirstChar {
                                        if (it.isLowerCase()) it.titlecase(Locale.ROOT)
                                        else it.toString()
                                    }
                            )
                        },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    painter = painterResource(R.drawable.round_check_24),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else null
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onApply(tempSelected)
                onDismiss()
            }) {
                Text(stringResource(R.string.apply))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}