package com.tribely.app.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tribely.app.core.data.SessionManager
import com.tribely.app.core.data.repository.AuthRepository
import com.tribely.app.core.ui.theme.TribelyPink
import com.tribely.app.core.ui.theme.TribelyTheme
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authRepo = remember { AuthRepository(SessionManager(context)) }

    var isLoading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 24.dp)
    ) {
        // Hero-блок
        Spacer(Modifier.height(32.dp))
        Text(
            text = "привет",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.onBackground)) {
                    append("Готов\n")
                }
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append("бросать?")
                }
            },
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 48.sp
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Это временный режим разработки. Войди как тестовый пользователь — всё работает реально, через Supabase.",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(Modifier.weight(1f))

        // Dev-баннер
        DevModeBanner()

        Spacer(Modifier.height(16.dp))

        // Кнопки тестовых пользователей
        Button(
            onClick = {
                if (isLoading) return@Button
                isLoading = true
                errorText = null
                scope.launch {
                    authRepo.signInAsDevUser("Тест-юзер 1")
                        .onSuccess { onLoggedIn() }
                        .onFailure {
                            errorText = it.message ?: "Неизвестная ошибка"
                            isLoading = false
                        }
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.Black
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.Black,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Войти как Тест-юзер 1", fontWeight = FontWeight.ExtraBold)
            }
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                if (isLoading) return@OutlinedButton
                isLoading = true
                errorText = null
                scope.launch {
                    authRepo.signInAsDevUser("Тест-юзер 2")
                        .onSuccess { onLoggedIn() }
                        .onFailure {
                            errorText = it.message ?: "Неизвестная ошибка"
                            isLoading = false
                        }
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                "Войти как Тест-юзер 2",
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
        }

        if (errorText != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "❌ $errorText",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Google вход добавим перед публикацией",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun DevModeBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TribelyPink.copy(alpha = 0.12f))
            .border(
                width = 1.dp,
                color = TribelyPink.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Construction,
            contentDescription = null,
            tint = TribelyPink,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                text = "DEV MODE",
                color = TribelyPink,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Временный режим разработки",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF050505, widthDp = 360, heightDp = 760)
@Composable
private fun LoginScreenPreview() {
    TribelyTheme {
        LoginScreen()
    }
}
