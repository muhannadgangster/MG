package com.example.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Liquid Glass Color Tokens
val GlassFrostedDark = Color(0x4D101622) // Translucent deep frosted tint
val GlassFrostedMedium = Color(0x66161E2E)
val GlassFrostedLight = Color(0x80222B3D)
val GlassSurfaceHighlight = Color(0x2EFFFFFF) // Soft specular reflection

// Luminous Specular Border Gradients
val GlassBorderSpecular = Brush.linearGradient(
    colors = listOf(
        Color(0x99FFFFFF), // Top-left specular gleam
        Color(0x4000E5FF), // Subtle cyan refraction
        Color(0x1AFFFFFF), // Mid transmission
        Color(0x338B5CF6), // Subtle violet diffraction
        Color(0x66FFFFFF)  // Bottom-right edge reflection
    ),
    start = Offset(0f, 0f),
    end = Offset(400f, 600f)
)

val GlassBorderActive = Brush.linearGradient(
    colors = listOf(
        Color(0xFF00E5FF),
        Color(0x808B5CF6),
        Color(0xCCFFFFFF)
    )
)

val GlassBorderSubtle = Brush.linearGradient(
    colors = listOf(
        Color(0x4DFFFFFF),
        Color(0x1AFFFFFF),
        Color(0x33FFFFFF)
    )
)

/**
 * Modifier that applies the signature Liquid Glass spatial effect:
 * Translucent frosted tint, layered specular gradient border, soft shadow depth, and inner reflection sheen.
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color = GlassFrostedDark,
    borderBrush: Brush = GlassBorderSpecular,
    borderWidth: Dp = 1.dp,
    elevation: Dp = 8.dp
): Modifier = this
    .shadow(
        elevation = elevation,
        shape = shape,
        ambientColor = Color.Black.copy(alpha = 0.5f),
        spotColor = Color(0x4000E5FF)
    )
    .clip(shape)
    .background(backgroundColor)
    .drawBehind {
        // Specular diagonal highlight gleam across top-left corner
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.08f), Color.Transparent),
                center = Offset(size.width * 0.2f, size.height * 0.15f),
                radius = size.width * 0.6f
            ),
            center = Offset(size.width * 0.2f, size.height * 0.15f),
            radius = size.width * 0.6f
        )
    }
    .border(width = borderWidth, brush = borderBrush, shape = shape)

/**
 * Interactive Liquid Glass button modifier with tactile spring compression on touch.
 */
@Composable
fun Modifier.liquidGlassClickable(
    shape: Shape = RoundedCornerShape(14.dp),
    backgroundColor: Color = GlassFrostedMedium,
    borderBrush: Brush = GlassBorderSpecular,
    elevation: Dp = 6.dp,
    onClick: () -> Unit
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "glass_press_scale"
    )

    return this
        .scale(scale)
        .liquidGlass(
            shape = shape,
            backgroundColor = if (isPressed) backgroundColor.copy(alpha = 0.85f) else backgroundColor,
            borderBrush = if (isPressed) GlassBorderActive else borderBrush,
            borderWidth = if (isPressed) 1.5.dp else 1.dp,
            elevation = if (isPressed) 2.dp else elevation
        )
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
}

/**
 * Floating glass capsule container for video player controls and status badges.
 */
@Composable
fun LiquidGlassCapsule(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Black.copy(alpha = 0.45f),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .border(1.dp, GlassBorderSpecular, CircleShape)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        content()
    }
}
