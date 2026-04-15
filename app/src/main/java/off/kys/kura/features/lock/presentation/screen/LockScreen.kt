package off.kys.kura.features.lock.presentation.screen

import android.content.res.Configuration
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseInOutQuad
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import off.kys.kura.core.common.LockAppearance
import off.kys.kura.core.designsystem.theme.KuraTheme

@Composable
fun LockScreen(lockAppearance: LockAppearance) {
    if (lockAppearance == LockAppearance.WALLPAPER) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        )
        return
    }

    val isDark = isSystemInDarkTheme()
    val minAlpha = if (isDark) 0.1f else 0.2f
    val maxAlpha = if (isDark) 0.25f else 0.5f

    val isPulseActive = lockAppearance == LockAppearance.PULSE
    // Only run transitions if animation is enabled
    val infiniteTransition =
        if (isPulseActive) rememberInfiniteTransition(label = "pulse") else null

    val scale by infiniteTransition?.animateFloat(
        initialValue = 0.85f, targetValue = 1.3f,
        animationSpec = infiniteRepeatable(tween(2500, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "scale"
    )
        ?: remember { mutableFloatStateOf(1.0f) }

    val alpha by infiniteTransition?.animateFloat(
        initialValue = minAlpha, targetValue = maxAlpha,
        animationSpec = infiniteRepeatable(tween(2200, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "alpha"
    )
        ?: remember { mutableFloatStateOf(maxAlpha) }

    val offsetX by infiniteTransition?.animateFloat(
        initialValue = -60f, targetValue = 60f,
        animationSpec = infiniteRepeatable(
            tween(4000, easing = EaseInOutCubic),
            RepeatMode.Reverse
        ),
        label = "offsetX"
    )
        ?: remember { mutableFloatStateOf(0f) }

    val offsetY by infiniteTransition?.animateFloat(
        initialValue = -40f, targetValue = 40f,
        animationSpec = infiniteRepeatable(tween(3300, easing = EaseInOutQuad), RepeatMode.Reverse),
        label = "offsetY"
    )
        ?: remember { mutableFloatStateOf(0f) }

    val baseColor = MaterialTheme.colorScheme.primary

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val glowColor = baseColor.copy(alpha = alpha)
            val floatingCenter = Offset(x = center.x + offsetX, y = center.y + offsetY)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor, Color.Transparent),
                    center = floatingCenter,
                    radius = (size.minDimension / 1.5f) * scale
                ),
                radius = (size.minDimension / 1.5f) * scale,
                center = floatingCenter
            )
        }
    }
}

// --- Previews ---
@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun LockScreenPreview() {
    KuraTheme {
        LockScreen(LockAppearance.WALLPAPER)
    }
}