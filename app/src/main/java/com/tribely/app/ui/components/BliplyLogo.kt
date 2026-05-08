package com.tribely.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp

@Composable
fun BliplyLogo(
    modifier: Modifier = Modifier
) {
    BliplyLogoMark(modifier = modifier)
}

@Composable
fun BliplyLogoMark(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            rotate(degrees = rotation, pivot = center) {
                drawRoundRect(
                    brush = Brush.sweepGradient(
                        colorStops = arrayOf(
                            0.0f to Color(0xFFFF3DCC),
                            0.25f to Color(0xFF8B5CFF),
                            0.5f to Color(0xFF4D7BFF),
                            0.75f to Color(0xFF8B5CFF),
                            1.0f to Color(0xFFFF3DCC)
                        ),
                        center = center
                    ),
                    cornerRadius = CornerRadius(35.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        Box(
            modifier = Modifier
                .size(114.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlowingEye(color = Color(0xFFFF3DCC))
                GlowingEye(color = Color(0xFF8B5CFF))
            }
        }
    }
}

@Composable
fun GlowingEye(
    color: Color
) {
    Box(
        modifier = Modifier.size(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            color.copy(alpha = 0.7f),
                            color.copy(alpha = 0f)
                        ),
                        radius = 60f
                    ),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color, CircleShape)
        )
    }
}
