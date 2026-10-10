package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MgCyan
import com.example.ui.theme.MgPurple

/**
 * Liquid Glass Material tiers defining depth, opacity, and specular reflection levels.
 */
enum class GlassTier {
    ULTRA_THIN, // Overlays, badges, video floating controls (18-28% opacity)
    MEDIUM,     // Navigation dock, cards, feed controls, action bars (50-70% opacity)
    DEEP        // Modals, bottom sheets, full panels, settings cards (80-92% opacity)
}

/**
 * Premium Liquid Glass Modifier:
 * Implements modern spatial glassmorphism with:
 * - Realistic translucent frosted depth
 * - Dual-layer specular border with chromatic refraction (cyan/purple edge diffraction)
 * - Inner luminous highlight rim at top edge
 * - Soft diffuse outer shadow
 * - High text readability contrast
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(16.dp),
    tier: GlassTier = GlassTier.MEDIUM,
    borderWidth: Dp = 1.dp,
    hasChromaticDiffraction: Boolean = true,
    hasInnerSpecularRim: Boolean = true
): Modifier = this
    .clip(shape)
    .drawBehind {
        val w = size.width
        val h = size.height

        // 1. Translucent frosted liquid glass base
        val (topColor, bottomColor) = when (tier) {
            GlassTier.ULTRA_THIN -> Pair(Color(0x35263147), Color(0x18131B2A))
            GlassTier.MEDIUM -> Pair(Color(0x901E2638), Color(0x65101524))
            GlassTier.DEEP -> Pair(Color(0xE0131929), Color(0xCC0D111E))
        }

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(topColor, bottomColor),
                startY = 0f,
                endY = h
            )
        )

        // 2. Subtle light diffraction tint across surface
        if (hasChromaticDiffraction) {
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MgCyan.copy(alpha = if (tier == GlassTier.ULTRA_THIN) 0.08f else 0.04f),
                        Color.Transparent,
                        MgPurple.copy(alpha = if (tier == GlassTier.ULTRA_THIN) 0.07f else 0.03f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(w, h)
                )
            )
        }

        // 3. Inner specular refraction highlight line along top rim
        if (hasInnerSpecularRim && h > 20f) {
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0x66FFFFFF),
                        Color(0x99FFFFFF),
                        Color(0x66FFFFFF),
                        Color.Transparent
                    )
                ),
                start = Offset(w * 0.15f, 1.5f),
                end = Offset(w * 0.85f, 1.5f),
                strokeWidth = 1.5f
            )
        }
    }
    .border(
        width = borderWidth,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.55f),
                if (hasChromaticDiffraction) MgCyan.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.25f),
                if (hasChromaticDiffraction) MgPurple.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.12f),
                Color.White.copy(alpha = 0.08f)
            ),
            start = Offset(0f, 0f),
            end = Offset(600f, 800f)
        ),
        shape = shape
    )

/**
 * Reusable Liquid Glass Container Box
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    tier: GlassTier = GlassTier.MEDIUM,
    borderWidth: Dp = 1.dp,
    hasChromaticDiffraction: Boolean = true,
    animatedGleam: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val gleamOffset by if (animatedGleam) {
        val infiniteTransition = rememberInfiniteTransition(label = "gleam_anim")
        infiniteTransition.animateFloat(
            initialValue = -0.3f,
            targetValue = 1.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(3500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "gleam_offset"
        )
    } else {
        remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    }

    Box(
        modifier = modifier
            .liquidGlass(
                shape = shape,
                tier = tier,
                borderWidth = borderWidth,
                hasChromaticDiffraction = hasChromaticDiffraction
            )
            .drawBehind {
                if (animatedGleam) {
                    val w = size.width
                    val h = size.height
                    val startX = w * gleamOffset
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.12f),
                                Color.White.copy(alpha = 0.20f),
                                Color.White.copy(alpha = 0.12f),
                                Color.Transparent
                            ),
                            start = Offset(startX - 60f, 0f),
                            end = Offset(startX + 60f, h)
                        )
                    )
                }
            }
    ) {
        content()
    }
}

/**
 * Floating Liquid Glass Dock for Navigation Bars and Toolbars
 */
@Composable
fun LiquidGlassDock(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    content: @Composable RowScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 20.dp,
                shape = shape,
                spotColor = MgCyan.copy(alpha = 0.2f),
                ambientColor = Color.Black
            )
            .liquidGlass(
                shape = shape,
                tier = GlassTier.MEDIUM,
                borderWidth = 1.dp,
                hasChromaticDiffraction = true,
                hasInnerSpecularRim = true
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}

/**
 * Liquid Glass Pill badge for status indicators, categories, and tags
 */
@Composable
fun LiquidGlassPill(
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    val shape = CircleShape
    val clickModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier

    Box(
        modifier = modifier
            .then(clickModifier)
            .clip(shape)
            .then(
                if (isSelected) {
                    Modifier
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MgCyan.copy(alpha = 0.95f),
                                    MgPurple.copy(alpha = 0.90f)
                                )
                            )
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.7f),
                            shape = shape
                        )
                } else {
                    Modifier.liquidGlass(
                        shape = shape,
                        tier = GlassTier.ULTRA_THIN,
                        borderWidth = 1.dp,
                        hasChromaticDiffraction = true
                    )
                }
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}

/**
 * Liquid Glass Button with interactive ripple and specular refraction
 */
@Composable
fun LiquidGlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    tier: GlassTier = GlassTier.MEDIUM,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .liquidGlass(shape = shape, tier = tier, borderWidth = 1.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = MgCyan),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
