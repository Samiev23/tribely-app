package com.tribely.app.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tribely.app.ui.theme.BliplyColors

@Composable
fun OnboardingDots(
    currentPage: Int,
    pageCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val isActive = index == currentPage
            val width by animateDpAsState(
                targetValue = if (isActive) 24.dp else 6.dp,
                animationSpec = tween(durationMillis = 250),
                label = "dot_width_$index"
            )

            Box(
                modifier = Modifier
                    .width(width)
                    .height(6.dp)
                    .then(
                        if (isActive) {
                            Modifier.shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(3.dp),
                                ambientColor = BliplyColors.NeonPink,
                                spotColor = BliplyColors.NeonPink
                            )
                        } else {
                            Modifier
                        }
                    )
                    .background(
                        brush = if (isActive) {
                            Brush.linearGradient(
                                colors = listOf(
                                    BliplyColors.NeonPink,
                                    BliplyColors.NeonBlue
                                )
                            )
                        } else {
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.2f),
                                    Color.White.copy(alpha = 0.2f)
                                )
                            )
                        },
                        shape = if (isActive) RoundedCornerShape(3.dp) else CircleShape
                    )
            )
        }
    }
}
