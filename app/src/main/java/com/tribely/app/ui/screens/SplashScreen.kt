package com.tribely.app.ui.screens

import android.app.Activity
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.tribely.app.core.data.SessionManager
import com.tribely.app.core.ui.theme.TribelyTheme
import com.tribely.app.navigation.TribelyDestinations
import com.tribely.app.ui.components.BliplyLogo
import com.tribely.app.ui.theme.BliplyColors
import com.tribely.app.ui.theme.BliplyLogoTitleStyle
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: (destination: String) -> Unit
) {
    ConfigureTransparentStatusBar()

    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    var logoVisible by remember { mutableStateOf(false) }
    var titleVisible by remember { mutableStateOf(false) }

    val logoScale by animateFloatAsState(
        targetValue = if (logoVisible) 1f else 0.8f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "splash_logo_scale"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (logoVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "splash_logo_alpha"
    )
    val titleAlpha by animateFloatAsState(
        targetValue = if (titleVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "splash_title_alpha"
    )

    LaunchedEffect(Unit) {
        logoVisible = true
        delay(200)
        titleVisible = true
    }

    LaunchedEffect(Unit) {
        val destination = when {
            sessionManager.getUserId() == null -> TribelyDestinations.WELCOME
            sessionManager.getGroupId() == null -> TribelyDestinations.GROUP_SELECT
            else -> TribelyDestinations.MAIN
        }

        delay(10_000)
        // TODO: Route to onboarding or the main screen after the final auth flow is confirmed.
        onSplashFinished(destination)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BliplyColors.Background)
            .drawBehind {
                val glowRadius = size.minDimension * 0.7f
                drawCircle(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.0f to Color(0xFFFF3DCC).copy(alpha = 0.20f),
                            0.3f to Color(0xFFFF3DCC).copy(alpha = 0.12f),
                            0.6f to Color(0xFFFF3DCC).copy(alpha = 0.05f),
                            1.0f to Color.Transparent
                        ),
                        center = center,
                        radius = glowRadius
                    ),
                    center = center,
                    radius = glowRadius
                )
            }
    ) {
        BliplyLogo(
            modifier = Modifier
                .align(Alignment.Center)
                .scale(logoScale)
                .alpha(logoAlpha)
        )

        Text(
            text = "Bliply",
            style = BliplyLogoTitleStyle,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
                .alpha(titleAlpha)
        )

        LoadingDots(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )
    }
}

@Composable
private fun ConfigureTransparentStatusBar() {
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.setDecorFitsSystemWindows(window, false)
            @Suppress("DEPRECATION")
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }
}

@Composable
private fun LoadingDots(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading_dots")
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val delayMillis = index * 200
            val intensity by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 1_400
                        0f at 0
                        0f at delayMillis
                        1f at delayMillis + 140
                        1f at delayMillis + 560
                        0f at delayMillis + 700
                        0f at 1_400
                    }
                ),
                label = "loading_dot_$index"
            )

            LoadingDot(intensity = intensity)
        }
    }
}

@Composable
private fun LoadingDot(
    intensity: Float,
    modifier: Modifier = Modifier
) {
    val isActive = intensity > 0.5f
    val dotSize = if (isActive) 8.dp else 6.dp
    val dotColor = if (isActive) Color(0xFFFF3DCC) else Color.White.copy(alpha = 0.3f)

    Box(
        modifier = modifier
            .size(dotSize)
            .then(
                if (isActive) {
                    Modifier.shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        spotColor = Color(0xFFFF3DCC),
                        ambientColor = Color(0xFFFF3DCC)
                    )
                } else {
                    Modifier
                }
            )
            .background(color = dotColor, shape = CircleShape)
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF08070C)
@Composable
private fun SplashScreenPreview() {
    TribelyTheme {
        SplashScreen(onSplashFinished = {})
    }
}
