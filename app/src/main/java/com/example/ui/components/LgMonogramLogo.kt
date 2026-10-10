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
 * Geometric, minimal, memorable LG Monogram Logo with Liquid Glass specular aesthetics.
 * The left stem and base form the 'L', which flows seamlessly into the outer curved contour and crossbar of the 'G'.
 */
@Composable
fun LgMonogramLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    animatedGlow: Boolean = false,
    gradientColors: List<Color> = listOf(Color.White, MgCyan, MgPurple),
    strokeWidthFactor: Float = 0.13f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "lg_logo_anim")
    val glowAlpha by if (animatedGlow) {
        infiniteTransition.animateFloat(
            initialValue = 0.65f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "lg_glow"
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

            // Letter 'L': Vertical down on left, then horizontal base
            val lPath = Path().apply {
                moveTo(w * 0.18f, h * 0.16f)
                lineTo(w * 0.18f, h * 0.84f)
                lineTo(w * 0.44f, h * 0.84f)
            }

            // Letter 'G': Curves seamlessly into circle with inward crossbar
            val gPath = Path().apply {
                // Top curve of G
                moveTo(w * 0.84f, h * 0.36f)
                cubicTo(
                    w * 0.84f, h * 0.16f,
                    w * 0.48f, h * 0.16f,
                    w * 0.48f, h * 0.50f
                )
                // Bottom curve of G
                cubicTo(
                    w * 0.48f, h * 0.84f,
                    w * 0.84f, h * 0.84f,
                    w * 0.84f, h * 0.54f
                )
                // Inward crossbar of G
                lineTo(w * 0.66f, h * 0.54f)
            }

            drawPath(path = lPath, brush = brush, style = stroke)
            drawPath(path = gPath, brush = brush, style = stroke)
        }
    }
}

@Composable
fun LgBrandHeader(
    modifier: Modifier = Modifier,
    logoSize: Dp = 36.dp,
    showTagline: Boolean = false
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        LgMonogramLogo(size = logoSize)
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = "LG",
                color = Color.White,
                fontSize = (logoSize.value * 0.68f).sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.SansSerif
            )
            if (showTagline) {
                Text(
                    text = "CREATE • DISCOVER • CONNECT",
                    color = MgCyan,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
            }
        }
    }
}
