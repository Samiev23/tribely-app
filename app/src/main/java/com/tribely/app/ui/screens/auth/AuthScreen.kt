package com.tribely.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tribely.app.ui.components.BliplyLogo
import com.tribely.app.ui.components.buttons.AppleSignInButton
import com.tribely.app.ui.components.buttons.GoogleSignInButton
import com.tribely.app.ui.components.buttons.PhoneSignInButton
import com.tribely.app.ui.components.dividers.OrDivider
import com.tribely.app.ui.theme.BliplyColors
import com.tribely.app.ui.theme.manropeFamily
import com.tribely.app.ui.theme.unboundedFamily

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        if (state is AuthState.Success) {
            onAuthSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BliplyColors.Background)
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            BliplyColors.NeonPink.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        center = Offset(size.width / 2f, 0f),
                        radius = size.height * 0.5f
                    )
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            BliplyColors.NeonBlue.copy(alpha = 0.10f),
                            Color.Transparent
                        ),
                        center = Offset(size.width / 2f, size.height),
                        radius = size.height * 0.5f
                    )
                )
            }
    ) {
        BackButton(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(16.dp),
            onClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            BliplyLogo(
                size = 64.dp,
                modifier = Modifier.shadow(
                    elevation = 40.dp,
                    shape = CircleShape,
                    ambientColor = BliplyColors.NeonPink,
                    spotColor = BliplyColors.NeonPink
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = authTitle(),
                fontFamily = unboundedFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 26.sp,
                lineHeight = 28.sp,
                letterSpacing = (-0.5).sp,
                textAlign = TextAlign.Center,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Войди, чтобы создать свой круг и делиться моментами с близкими.",
                fontFamily = manropeFamily,
                fontSize = 13.sp,
                color = Color(0xFFA8A1B8),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            AuthButtons(
                state = state,
                onGoogleClick = {
                    // TODO: Replace test user 1 with real Google sign-in before release.
                    viewModel.signInAsTestUser(1)
                },
                onAppleClick = {
                    // TODO: Replace test user 2 with real Apple sign-in before release.
                    viewModel.signInAsTestUser(2)
                },
                onPhoneClick = {
                    // TODO: Navigate to phone auth screen when phone sign-in is ready.
                    viewModel.signInAsTestUser(3)
                }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            LegalText()
        }
    }
}

@Composable
private fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .background(Color.White.copy(alpha = 0.06f), CircleShape)
            .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = null,
            tint = Color(0xFFA8A1B8),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun AuthButtons(
    state: AuthState,
    onGoogleClick: () -> Unit,
    onAppleClick: () -> Unit,
    onPhoneClick: () -> Unit
) {
    val isLoading = state is AuthState.Loading

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center
        ) {
            GoogleSignInButton(onClick = onGoogleClick, enabled = !isLoading)
            Spacer(modifier = Modifier.height(10.dp))
            AppleSignInButton(onClick = onAppleClick, enabled = !isLoading)

            Spacer(modifier = Modifier.height(18.dp))
            OrDivider()
            Spacer(modifier = Modifier.height(18.dp))

            PhoneSignInButton(onClick = onPhoneClick, enabled = !isLoading)

            if (state is AuthState.Error) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = state.message,
                    color = Color(0xFFFF5C7A),
                    fontFamily = manropeFamily,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(BliplyColors.Background.copy(alpha = 0.42f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = BliplyColors.NeonPink,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun LegalText(
    modifier: Modifier = Modifier
) {
    Text(
        text = buildAnnotatedString {
            append("Продолжая, ты принимаешь ")
            withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                append("условия")
            }
            append("\nи ")
            withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                append("политику конфиденциальности")
            }
        },
        color = Color(0xFF6B6480),
        fontFamily = manropeFamily,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

private fun authTitle() = buildAnnotatedString {
    val gradientBrush = Brush.linearGradient(
        listOf(
            BliplyColors.NeonPink,
            BliplyColors.NeonPurple,
            BliplyColors.NeonBlue
        )
    )

    append("Добро пожаловать\nв ")
    withStyle(
        SpanStyle(
            brush = gradientBrush,
            fontFamily = unboundedFamily,
            fontWeight = FontWeight.SemiBold
        )
    ) {
        append("Bliply")
    }
}
