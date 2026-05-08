package com.tribely.app.ui.screens.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tribely.app.core.ui.theme.TribelyTheme
import com.tribely.app.ui.components.GradientButton
import com.tribely.app.ui.components.OnboardingDots
import com.tribely.app.ui.theme.BliplyColors
import com.tribely.app.ui.theme.unboundedFamily

private data class OnboardingSlide(
    val titlePrefix: String,
    val titleAccent: String,
    val subtitle: String,
    val buttonText: String
)

private val onboardingSlides = listOf(
    OnboardingSlide(
        titlePrefix = "Только ",
        titleAccent = "свой круг",
        subtitle = "До 12 близких людей. Без подписчиков, без друзей друзей. Только те, кому ты пишешь сам.",
        buttonText = "Дальше →"
    ),
    OnboardingSlide(
        titlePrefix = "Один ",
        titleAccent = "момент в день",
        subtitle = "Каждый день — задание: «найди что-то, что сделало тебя счастливым». Не контент, не лайки — просто маленькая искренняя заметка для своих.",
        buttonText = "Дальше →"
    ),
    OnboardingSlide(
        titlePrefix = "Чаты и ",
        titleAccent = "звонки",
        subtitle = "Голосовые, фото, видеозвонки. Всё что нужно — без рекламы, групп на 200 человек и сторис незнакомцев.",
        buttonText = "Поехали →"
    )
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    onSkip: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentPage by remember { mutableIntStateOf(0) }
    val lastPage = onboardingSlides.lastIndex

    BackHandler {
        if (currentPage == 0) {
            onClose()
        } else {
            currentPage -= 1
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BliplyColors.Background)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            BliplyColors.NeonPink.copy(alpha = 0.20f),
                            Color.Transparent
                        ),
                        center = center.copy(y = 0f),
                        radius = size.minDimension * 0.75f
                    ),
                    center = center.copy(y = 0f),
                    radius = size.minDimension * 0.75f
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            BliplyColors.NeonBlue.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        center = center.copy(y = size.height),
                        radius = size.minDimension * 0.75f
                    ),
                    center = center.copy(y = size.height),
                    radius = size.minDimension * 0.75f
                )
            }
            .padding(horizontal = 16.dp, vertical = 28.dp)
    ) {
        if (currentPage < lastPage) {
            Text(
                text = "пропустить",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(WindowInsets.statusBars.asPaddingValues())
                    .padding(top = 24.dp)
                    .clickable(onClick = onSkip)
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            AnimatedContent(
                targetState = currentPage,
                transitionSpec = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(durationMillis = 400)
                    ) + fadeIn(animationSpec = tween(durationMillis = 400)) togetherWith
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(durationMillis = 400)
                        ) + fadeOut(animationSpec = tween(durationMillis = 400))
                },
                label = "onboarding_slide"
            ) { page ->
                OnboardingSlideContent(
                    page = page,
                    slide = onboardingSlides[page],
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(WindowInsets.navigationBars.asPaddingValues())
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OnboardingDots(
                    currentPage = currentPage,
                    pageCount = onboardingSlides.size
                )
                GradientButton(
                    text = onboardingSlides[currentPage].buttonText,
                    onClick = {
                        if (currentPage == lastPage) {
                            // TODO: Navigate to auth/login screen.
                            onFinished()
                        } else {
                            currentPage += 1
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun OnboardingSlideContent(
    page: Int,
    slide: OnboardingSlide,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (page) {
            0 -> OnboardingSlide1()
            1 -> OnboardingSlide2()
            else -> OnboardingSlide3()
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = buildOnboardingTitle(slide.titlePrefix, slide.titleAccent),
            color = Color.White,
            fontFamily = unboundedFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp,
            lineHeight = 36.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = slide.subtitle,
            color = Color.White.copy(alpha = 0.62f),
            fontSize = 14.sp,
            lineHeight = 21.sp
        )
    }
}

@Composable
private fun buildOnboardingTitle(
    prefix: String,
    accent: String
) = buildAnnotatedString {
    val gradientBrush = Brush.linearGradient(
        listOf(
            BliplyColors.NeonPink,
            BliplyColors.NeonPurple,
            BliplyColors.NeonBlue
        )
    )

    append(prefix)
    withStyle(SpanStyle(brush = gradientBrush)) {
        append(accent)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF08070C)
@Composable
private fun OnboardingScreenPreview() {
    TribelyTheme {
        OnboardingScreen(
            onFinished = {},
            onSkip = {},
            onClose = {}
        )
    }
}
