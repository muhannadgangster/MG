package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MgCyan
import com.example.ui.theme.MgPurple

/**
 * Geometric, minimal, memorable MG Monogram Logo.
 * The left strokes form the 'M', which seamlessly merges its right stem into the curved loop and crossbar of the 'G'.
 */
@Composable
fun MgMonogramLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    animatedGlow: Boolean = false,
    gradientColors: List<Color> = listOf(MgCyan, MgPurple),
    strokeWidthFactor: Float = 0.12f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mg_logo_anim")
    val glowAlpha by if (animatedGlow) {
        infiniteTransition.animateFloat(
            initialValue = 0.6f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "mg_glow"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(1f) }
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val strokePx = w * strokeWidthFactor

            val brush = Brush.linearGradient(
                colors = gradientColors.map { it.copy(alpha = glowAlpha) },
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )

            val stroke = Stroke(
                width = strokePx,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )

            // Geometric path for connected 'M' and 'G':
            // 'M': (0.1w, 0.85h) -> (0.1w, 0.15h) -> (0.35w, 0.55h) -> (0.55w, 0.15h)
            // 'G': connects from (0.55w, 0.15h) down curving across (0.9w, 0.15h) -> (0.9w, 0.85h) -> (0.55w, 0.85h) -> (0.55w, 0.55h) -> (0.78w, 0.55h)

            val mPath = Path().apply {
                // Left leg of M
                moveTo(w * 0.12f, h * 0.85f)
                lineTo(w * 0.12f, h * 0.18f)
                // Center dip of M
                lineTo(w * 0.32f, h * 0.55f)
                // Rise to junction where M meets G
                lineTo(w * 0.50f, h * 0.18f)
            }

            val gPath = Path().apply {
                // Outer G loop connecting from top right
                moveTo(w * 0.86f, h * 0.35f)
                // Arc top
                cubicTo(
                    w * 0.86f, h * 0.15f,
                    w * 0.50f, h * 0.15f,
                    w * 0.50f, h * 0.18f
                )
                // Down along junction
                lineTo(w * 0.50f, h * 0.70f)
                // Bottom curve of G
                cubicTo(
                    w * 0.50f, h * 0.88f,
                    w * 0.88f, h * 0.88f,
                    w * 0.88f, h * 0.65f
                )
                // Inward crossbar of G
                lineTo(w * 0.88f, h * 0.55f)
                lineTo(w * 0.68f, h * 0.55f)
            }

            drawPath(path = mPath, brush = brush, style = stroke)
            drawPath(path = gPath, brush = brush, style = stroke)
        }
    }
}

@Composable
fun MgBrandHeader(
    modifier: Modifier = Modifier,
    logoSize: Dp = 36.dp,
    showTagline: Boolean = false
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        MgMonogramLogo(size = logoSize)
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = "MG",
                color = Color.White,
                fontSize = (logoSize.value * 0.65f).sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.SansSerif
            )
            if (showTagline) {
                Text(
                    text = "CREATE. DISCOVER. CONNECT.",
                    color = MgCyan,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
            }
        }
    }
}
