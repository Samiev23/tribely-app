package com.tribely.app.ui.screens.onboarding

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun OnboardingSlide2(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(width = 240.dp, height = 280.dp),
        contentAlignment = Alignment.Center
    ) {
        FloatingCard(
            rotationDegrees = -6f,
            offsetX = (-30).dp,
            offsetY = 20.dp,
            floatRange = 8.dp,
            durationMs = 5_000,
            gradient = Brush.linearGradient(
                colors = listOf(Color(0xFF3DD9FF), Color(0xFF8B5CFF))
            )
        )
        FloatingCard(
            rotationDegrees = 2f,
            offsetX = 0.dp,
            offsetY = 30.dp,
            floatRange = 6.dp,
            durationMs = 6_000,
            delayMs = 200,
            gradient = Brush.linearGradient(
                colors = listOf(Color(0xFFFF3DCC), Color(0xFF8B5CFF))
            )
        )
        FloatingCard(
            rotationDegrees = -2f,
            offsetX = 30.dp,
            offsetY = 10.dp,
            floatRange = 10.dp,
            durationMs = 4_500,
            delayMs = 400,
            gradient = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFF4D8D),
                    Color(0xFFFF3DCC),
                    Color(0xFF4D7BFF)
                )
            )
        )
    }
}

@Composable
private fun FloatingCard(
    cardModifier: Modifier = Modifier,
    rotationDegrees: Float,
    offsetX: Dp,
    offsetY: Dp,
    floatRange: Dp,
    durationMs: Int,
    delayMs: Int = 0,
    gradient: Brush
) {
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val floatY by infiniteTransition.animateFloat(
        initialValue = -floatRange.value,
        targetValue = floatRange.value,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMs,
                delayMillis = delayMs,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_y"
    )
    val shape = RoundedCornerShape(18.dp)

    Box(
        modifier = cardModifier
            .offset(x = offsetX, y = offsetY + floatY.dp)
            .rotate(rotationDegrees)
            .size(width = 160.dp, height = 200.dp)
            .shadow(
                elevation = 12.dp,
                shape = shape,
                spotColor = Color.Black.copy(alpha = 0.5f)
            )
            .clip(shape)
            .background(gradient)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawBehind {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.25f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.3f, size.height * 0.3f),
                            radius = size.width * 0.5f
                        )
                    )
                }
        )
    }
}
