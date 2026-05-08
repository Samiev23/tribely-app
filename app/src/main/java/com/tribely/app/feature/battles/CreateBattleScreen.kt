package com.tribely.app.feature.battles

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tribely.app.core.ui.theme.TribelyCyan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBattleScreen(
    onBack: () -> Unit,
    onCreated: (battleId: String) -> Unit,
    viewModel: CreateBattleViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.createdBattleId) {
        state.createdBattleId?.let { id ->
            Toast.makeText(context, "🚀 Челлендж запущен!", Toast.LENGTH_SHORT).show()
            viewModel.clearCreatedBattle()
            onCreated(id)
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(horizontal = 18.dp)
    ) {
        Spacer(Modifier.height(12.dp))

        IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Назад",
                tint = Color.White.copy(alpha = 0.7f)
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "СОЗДАТЬ ЧЕЛЛЕНДЖ",
            color = Color.White.copy(alpha = 0.45f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Своя тема",
            color = Color.White,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.5).sp
        )
        Text(
            text = "Все в группе смогут участвовать",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(Modifier.height(28.dp))

        Text(
            text = "ТЕМА ЧЕЛЛЕНДЖА",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = state.theme,
            onValueChange = viewModel::updateTheme,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    "Например: Кто страннее завтрак?",
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 14.sp
                )
            },
            singleLine = false,
            maxLines = 3,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = TribelyCyan,
                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                focusedContainerColor = Color.White.copy(alpha = 0.04f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.04f),
                cursorColor = TribelyCyan
            )
        )

        Text(
            text = "${state.theme.length}/60",
            color = if (state.theme.length > 60) {
                Color.Red
            } else {
                Color.White.copy(alpha = 0.4f)
            },
            fontSize = 11.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            textAlign = TextAlign.End
        )

        Spacer(Modifier.height(20.dp))

        Text(
            text = "ВРЕМЯ НА ИСПОЛНЕНИЕ",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DurationOption("15", "минут", 15, state.durationMinutes, Modifier.weight(1f), viewModel::updateDuration)
            DurationOption("1", "час", 60, state.durationMinutes, Modifier.weight(1f), viewModel::updateDuration)
            DurationOption("24", "часа", 1440, state.durationMinutes, Modifier.weight(1f), viewModel::updateDuration)
        }

        Spacer(Modifier.height(20.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = TribelyCyan.copy(alpha = 0.06f),
            border = BorderStroke(
                1.dp,
                TribelyCyan.copy(alpha = 0.15f)
            )
        ) {
            Row(modifier = Modifier.padding(12.dp)) {
                Text("💡", fontSize = 14.sp)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "После загрузок будет 24ч голосование. Кто наберёт больше 🔥 — Чемпион баттла + 50 XP",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }

        Spacer(Modifier.weight(1f))

        val canSubmit = state.theme.trim().length in 5..60 && !state.isSubmitting

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable(enabled = canSubmit) { viewModel.create() },
            color = if (canSubmit) TribelyCyan else Color.White.copy(alpha = 0.08f)
        ) {
            Box(
                modifier = Modifier.padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "⚡ Запустить челлендж",
                        color = if (canSubmit) Color.Black else Color.White.copy(alpha = 0.4f),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DurationOption(
    num: String,
    unit: String,
    minutes: Int,
    selected: Int,
    modifier: Modifier = Modifier,
    onClick: (Int) -> Unit
) {
    val isSelected = selected == minutes
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick(minutes) },
        color = if (isSelected) {
            TribelyCyan.copy(alpha = 0.15f)
        } else {
            Color.White.copy(alpha = 0.04f)
        },
        border = BorderStroke(
            1.dp,
            if (isSelected) TribelyCyan else Color.White.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = num,
                color = if (isSelected) TribelyCyan else Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = unit,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp,
                letterSpacing = 1.sp
            )
        }
    }
}
