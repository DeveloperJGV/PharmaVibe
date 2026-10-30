package com.aviva.controlfarmacia.ui.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.aviva.controlfarmacia.domain.model.ExpiryStatus

data class StatusColors(val container: Color, val content: Color)

/** Container/content colour pair used for expiry badges and stat cards. */
@Composable
fun ExpiryStatus.colors(): StatusColors = when (this) {
    ExpiryStatus.EXPIRED -> StatusColors(
        MaterialTheme.colorScheme.errorContainer,
        MaterialTheme.colorScheme.onErrorContainer
    )
    ExpiryStatus.EXPIRING_THIS_MONTH -> StatusColors(
        MaterialTheme.colorScheme.tertiaryContainer,
        MaterialTheme.colorScheme.onTertiaryContainer
    )
    ExpiryStatus.VALID -> StatusColors(
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.onSecondaryContainer
    )
}
