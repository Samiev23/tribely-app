package com.tribely.app.ui.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tribely.app.ui.theme.BliplyColors
import com.tribely.app.ui.theme.manropeFamily
import kotlinx.coroutines.delay

@Composable
fun OnboardingSlide3(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(width = 240.dp, height = 280.dp),
        contentAlignment = Alignment.Center
    ) {
        ChatAnimation()
    }
}

@Composable
private fun ChatAnimation(
    modifier: Modifier = Modifier
) {
    var msg1Visible by remember { mutableStateOf(false) }
    var msg2Visible by remember { mutableStateOf(false) }
    var msg3Visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            msg1Visible = false
            msg2Visible = false
            msg3Visible = false

            delay(300)
            msg1Visible = true
            delay(700)
            msg2Visible = true
            delay(700)
            msg3Visible = true
            delay(3_000)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterStart
        ) {
            AnimatedChatMessage(
                visible = msg1Visible,
                isMine = false,
                text = "Алёш, ты обедал? 🥺"
            )
        }
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            AnimatedChatMessage(
                visible = msg2Visible,
                isMine = true,
                text = "Только что борщ доел 😄"
            )
        }
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterStart
        ) {
            AnimatedChatMessage(
                visible = msg3Visible,
                isMine = false,
                text = "Ну ты молодец 💜"
            )
        }
    }
}

@Composable
private fun AnimatedChatMessage(
    visible: Boolean,
    isMine: Boolean,
    text: String,
    modifier: Modifier = Modifier
) {
    val shape = if (isMine) {
        RoundedCornerShape(18.dp, 18.dp, 6.dp, 18.dp)
    } else {
        RoundedCornerShape(18.dp, 18.dp, 18.dp, 6.dp)
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(durationMillis = 400)
        ) + slideInVertically(
            initialOffsetY = { it / 4 },
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
        ),
        exit = fadeOut(animationSpec = tween(durationMillis = 200))
    ) {
        Box(
            modifier = modifier
                .widthIn(max = 180.dp)
                .then(
                    if (isMine) {
                        Modifier.shadow(
                            elevation = 4.dp,
                            shape = shape,
                            ambientColor = BliplyColors.NeonPink,
                            spotColor = BliplyColors.NeonPink
                        )
                    } else {
                        Modifier
                    }
                )
                .clip(shape)
                .then(
                    if (isMine) {
                        Modifier.background(
                            Brush.linearGradient(
                                colors = listOf(
                                    BliplyColors.NeonPink,
                                    BliplyColors.NeonPurple,
                                    BliplyColors.NeonBlue
                                )
                            )
                        )
                    } else {
                        Modifier
                            .background(Color.White.copy(alpha = 0.06f))
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.08f),
                                shape = shape
                            )
                    }
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = text,
                fontFamily = manropeFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = Color.White,
                lineHeight = 18.sp
            )
        }
    }
}
