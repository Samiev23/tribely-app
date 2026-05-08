package com.tribely.app.feature.battles
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tribely.app.core.data.model.Battle
import com.tribely.app.core.ui.theme.TribelyAccent
import com.tribely.app.core.ui.theme.TribelyCyan
import com.tribely.app.core.ui.theme.TribelyOrange
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Composable
fun BattlesScreen(
    onCreateClick: () -> Unit = {},
    onBattleClick: (String) -> Unit = {},
    viewModel: BattlesViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 18.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        Text(
            text = "СЕМЬЯ",
            color = Color.White.copy(alpha = 0.45f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Челленджи",
            color = Color.White,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.5).sp
        )
        Text(
            text = "Соревнуйтесь в креативе",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(Modifier.height(20.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { onCreateClick() },
            color = TribelyCyan
        ) {
            Box(
                modifier = Modifier.padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⚡ Создать челлендж",
                    color = Color.Black,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = TribelyAccent)
                }
            }

            state.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.error ?: "",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                }
            }

            state.activeBattles.isEmpty() && state.finishedBattles.isEmpty() -> {
                EmptyBattlesState()
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (state.activeBattles.isNotEmpty()) {
                        item {
                            SectionLabel("АКТИВНЫЕ · ${state.activeBattles.size}")
                        }
                        items(state.activeBattles, key = { it.id }) { battle ->
                            BattleCard(battle = battle) {
                                onBattleClick(battle.id)
                            }
                        }
                    }

                    if (state.finishedBattles.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(8.dp))
                            SectionLabel("ЗАВЕРШЁННЫЕ · ${state.finishedBattles.size}")
                        }
                        items(state.finishedBattles, key = { it.id }) { battle ->
                            BattleCard(battle = battle) {
                                onBattleClick(battle.id)
                            }
                        }
                    }

                    item { Spacer(Modifier.height(120.dp)) }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = Color.White.copy(alpha = 0.45f),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(vertical = 6.dp)
    )
}

@Composable
private fun BattleCard(
    battle: Battle,
    onClick: () -> Unit
) {
    val (statusText, statusColor) = when (battle.status) {
        "live" -> "ИДЁТ БАТТЛ" to TribelyCyan
        "voting" -> "ГОЛОСОВАНИЕ" to TribelyOrange
        "finished" -> "ЗАВЕРШЁН" to TribelyAccent
        else -> "" to Color.Gray
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = Color.White.copy(alpha = 0.04f),
        border = BorderStroke(
            1.dp,
            statusColor.copy(alpha = 0.25f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = statusText,
                    color = statusColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = formatTime(battle.createdAt),
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 11.sp
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = battle.theme,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 21.sp
            )
        }
    }
}

@Composable
private fun EmptyBattlesState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "⚔️", fontSize = 48.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Пока нет челленджей",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Создай первый и брось вызов группе",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 13.sp
            )
        }
    }
}

private fun formatTime(createdAt: Instant): String {
    val now = Clock.System.now()
    val diff = now - createdAt
    val seconds = diff.inWholeSeconds

    return when {
        seconds < 60 -> "только что"
        seconds < 3600 -> "${seconds / 60} мин"
        seconds < 86400 -> "${seconds / 3600} ч"
        else -> "${seconds / 86400} дн"
    }
}
