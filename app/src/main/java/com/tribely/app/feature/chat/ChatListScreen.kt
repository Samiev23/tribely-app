package com.tribely.app.feature.chat

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tribely.app.core.ui.theme.TribelyAccent
import com.tribely.app.core.ui.theme.TribelyPink
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Composable
fun ChatListScreen(
    onChatClick: (groupId: String) -> Unit,
    viewModel: ChatListViewModel = viewModel()
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
            .padding(bottom = 100.dp)
    ) {
        Spacer(Modifier.height(36.dp))

        Text(
            text = "Чаты",
            color = Color.White,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.5).sp
        )

        Spacer(Modifier.height(20.dp))

        when {
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TribelyAccent)
                }
            }

            state.error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = state.error ?: "",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                }
            }

            state.chats.isEmpty() -> {
                EmptyChatsState()
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(state.chats, key = { it.groupId }) { chat ->
                        ChatPreviewItem(
                            chat = chat,
                            onClick = { onChatClick(chat.groupId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatPreviewItem(
    chat: ChatPreview,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(TribelyPink, Color(0xFFA855F7))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            val initials = chat.groupName
                .split(" ")
                .filter { it.isNotEmpty() }
                .take(2)
                .map { it.first().uppercaseChar() }
                .joinToString("")
                .ifEmpty { "?" }

            Text(
                text = initials,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chat.groupName,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                chat.lastMessage?.createdAt?.let { ts ->
                    Text(
                        text = formatChatTime(ts),
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Spacer(Modifier.height(3.dp))

            if (chat.lastMessage != null) {
                val author = if (chat.lastMessageIsMe) {
                    "Ты"
                } else {
                    chat.lastMessageAuthor ?: "..."
                }
                Text(
                    text = "$author: ${chat.lastMessage.text}",
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = "${chat.memberCount} участников · напиши первым",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun EmptyChatsState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "💬", fontSize = 48.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Нет чатов",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Чаты появятся когда ты будешь в группе",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 13.sp
            )
        }
    }
}

private fun formatChatTime(createdAt: Instant): String {
    val now = Clock.System.now()
    val diff = now - createdAt
    val seconds = diff.inWholeSeconds

    return when {
        seconds < 60 -> "только что"
        seconds < 3600 -> "${seconds / 60} мин"
        seconds < 86400 -> "${seconds / 3600} ч"
        seconds < 604800 -> "${seconds / 86400} дн"
        else -> "давно"
    }
}
