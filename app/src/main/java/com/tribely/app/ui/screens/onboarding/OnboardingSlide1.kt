package com.tribely.app.ui.screens.onboarding

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tribely.app.ui.theme.BliplyColors
import com.tribely.app.ui.theme.unboundedFamily
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun OnboardingSlide1(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "friends_orbit")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 30_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "friends_orbit_rotation"
    )

    Box(
        modifier = modifier.size(280.dp),
        contentAlignment = Alignment.Center
    ) {
        OrbitCanvas()

        OrbitingFriend(
            angle = rotation,
            baseAngle = 270f,
            label = "М",
            gradient = Brush.linearGradient(
                colors = listOf(Color(0xFFFF4D8D), BliplyColors.NeonPink)
            )
        )
        OrbitingFriend(
            angle = rotation,
            baseAngle = 0f,
            label = "С",
            gradient = Brush.linearGradient(
                colors = listOf(Color(0xFF3DD9FF), BliplyColors.NeonPurple)
            )
        )
        OrbitingFriend(
            angle = rotation,
            baseAngle = 90f,
            label = "П",
            gradient = Brush.linearGradient(
                colors = listOf(BliplyColors.NeonPurple, BliplyColors.NeonBlue)
            )
        )
        OrbitingFriend(
            angle = rotation,
            baseAngle = 180f,
            label = "Л",
            gradient = Brush.linearGradient(
                colors = listOf(Color(0xFFE91FFF), Color(0xFFFF4D8D))
            )
        )

        CenterAvatar()
    }
}

@Composable
private fun OrbitCanvas(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(280.dp)) {
        drawCircle(
            color = Color.White.copy(alpha = 0.1f),
            radius = 120.dp.toPx(),
            style = Stroke(
                width = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(
                    intervals = floatArrayOf(8f, 8f),
                    phase = 0f
                )
            )
        )
    }
}

@Composable
private fun OrbitingFriend(
    angle: Float,
    baseAngle: Float,
    label: String,
    gradient: Brush,
    radius: Dp = 120.dp
) {
    val totalAngle = (angle + baseAngle) % 360f
    val radians = Math.toRadians(totalAngle.toDouble())
    val xOffset = (cos(radians) * radius.value).toFloat().dp
    val yOffset = (sin(radians) * radius.value).toFloat().dp

    Box(
        modifier = Modifier
            .offset(x = xOffset, y = yOffset)
            .size(46.dp)
            .shadow(
                elevation = 4.dp,
                shape = CircleShape,
                ambientColor = BliplyColors.NeonPink,
                spotColor = BliplyColors.NeonPink
            )
            .clip(CircleShape)
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = unboundedFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = Color.White
        )
    }
}

@Composable
private fun CenterAvatar(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(70.dp)
            .shadow(
                elevation = 32.dp,
                shape = CircleShape,
                ambientColor = BliplyColors.NeonPink,
                spotColor = BliplyColors.NeonPink
            )
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                ambientColor = BliplyColors.NeonPink,
                spotColor = BliplyColors.NeonPink
            )
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        BliplyColors.NeonPink,
                        BliplyColors.NeonPurple,
                        BliplyColors.NeonBlue
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "А",
            fontFamily = unboundedFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            color = Color.White
        )
    }
}
