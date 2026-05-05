package com.tribely.app.feature.feed

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tribely.app.core.data.SessionManager
import com.tribely.app.core.ui.theme.TribelyAccent
import com.tribely.app.feature.daily.FullscreenPhotoViewer

@Composable
fun FeedScreen(
    viewModel: FeedViewModel = viewModel()
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val groupId by sessionManager.groupIdFlow.collectAsState(initial = null)
    val groupName by sessionManager.groupNameFlow.collectAsState(initial = null)
    val showSoon = { feature: String ->
        Toast.makeText(context, "$feature — скоро", Toast.LENGTH_SHORT).show()
    }

    val uiState by viewModel.uiState.collectAsState()

    var fullscreenIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(groupId) {
        groupId?.let { viewModel.loadFeed(it) }
    }

    val state = uiState
    val fsIndex = fullscreenIndex
    if (state is FeedUiState.Success && fsIndex != null && state.submissions.isNotEmpty()) {
        FullscreenPhotoViewer(
            submissions = state.submissions,
            initialIndex = fsIndex,
            onClose = { fullscreenIndex = null },
            onReact = { submissionId, type ->
                viewModel.toggleReaction(submissionId, type)
            }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = (groupName ?: "ГРУППА").uppercase(),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Сегодня жгли 🔥",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Все ответы группы за неделю",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(onClick = { showSoon("Поиск") }) {
                Text(
                    text = "🔍",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        when (val s = state) {
            is FeedUiState.Loading -> LoadingFeed()
            is FeedUiState.Error -> ErrorFeed(s.message)
            is FeedUiState.Success -> {
                if (s.submissions.isEmpty()) {
                    EmptyFeed()
                } else {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        LeaderOfDayCard(onClick = { showSoon("Лидер дня") })
                        s.submissions.forEachIndexed { index, submission ->
                            FeedPostCard(
                                submission = submission,
                                challengeText = null,
                                bgColorIndex = index,
                                onPhotoClick = { fullscreenIndex = index },
                                onReact = { type ->
                                    viewModel.toggleReaction(submission.id, type)
                                },
                                onMenuClick = { showSoon("Меню") },
                                onCommentClick = { showSoon("Комментарии") }
                            )
                        }
                        Spacer(Modifier.height(120.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingFeed() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = TribelyAccent)
        Spacer(Modifier.height(12.dp))
        Text(
            "Подгружаем ленту...",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ErrorFeed(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "❌ Ошибка",
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.ExtraBold,
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(8.dp))
        Text(
            message,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun EmptyFeed() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "🌅",
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Тут будут ответы группы",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            "Сделай первый бросок, и лента оживёт",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}
