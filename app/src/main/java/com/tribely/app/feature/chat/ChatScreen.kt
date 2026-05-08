package com.tribely.app.feature.chat

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tribely.app.core.data.model.MessageWithAuthor
import com.tribely.app.core.ui.theme.TribelyAccent
import com.tribely.app.core.ui.theme.TribelyPink
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun ChatScreen(
    groupId: String,
    onBack: () -> Unit,
    viewModel: ChatViewModel = viewModel(
        factory = ChatViewModelFactory(groupId)
    )
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val listState = rememberLazyListState()

    LaunchedEffect(state.error) {
        state.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

        ChatHeader(
            groupName = state.groupName.ifBlank { "Чат" },
            memberCount = state.memberCount,
            onBack = onBack
        )

        HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 1.dp)

        Box(modifier = Modifier.weight(1f)) {
            when {
                state.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = TribelyAccent)
                    }
                }

                state.messages.isEmpty() -> {
                    EmptyMessagesState()
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(state.messages, key = { it.id }) { msg ->
                            MessageBubble(message = msg)
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 1.dp)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
        ) {
            MessageInput(
                draft = state.draft,
                isSending = state.isSending,
                onDraftChange = viewModel::updateDraft,
                onSend = viewModel::send
            )
        }
    }
}

@Composable
private fun ChatHeader(
    groupName: String,
    memberCount: Int,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 12.dp, top = 4.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Назад",
                tint = Color.White.copy(alpha = 0.85f)
            )
        }

        val initials = groupName
            .split(" ")
            .filter { it.isNotEmpty() }
            .take(2)
            .map { it.first().uppercaseChar() }
            .joinToString("")
            .ifEmpty { "?" }

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(TribelyPink, Color(0xFFA855F7))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp
            )
        }

        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = groupName,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                maxLines = 1
            )
            Text(
                text = "$memberCount участников",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun EmptyMessagesState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("👋", fontSize = 48.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                "Начни разговор",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Напиши первое сообщение",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun MessageInput(
    draft: String,
    isSending: Boolean,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp),
            color = Color.White.copy(alpha = 0.05f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = draft,
                    onValueChange = onDraftChange,
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 19.sp
                    ),
                    cursorBrush = SolidColor(TribelyPink),
                    maxLines = 5,
                    decorationBox = { inner ->
                        if (draft.isEmpty()) {
                            Text(
                                "Напиши сообщение...",
                                color = Color.White.copy(alpha = 0.3f),
                                fontSize = 14.sp
                            )
                        }
                        inner()
                    }
                )

                if (draft.length > 450) {
                    Text(
                        text = "${500 - draft.length}",
                        color = if (draft.length > 480) {
                            Color(0xFFFF6B6B)
                        } else {
                            Color(0xFFFFAA00)
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }
        }

        val canSend = draft.trim().isNotEmpty() && !isSending

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (canSend) {
                        Brush.linearGradient(
                            colors = listOf(TribelyPink, Color(0xFFC8338C))
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.08f),
                                Color.White.copy(alpha = 0.08f)
                            )
                        )
                    }
                )
                .clickable(enabled = canSend) { onSend() },
            contentAlignment = Alignment.Center
        ) {
            if (isSending) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Отправить",
                    tint = if (canSend) Color.White else Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(message: MessageWithAuthor) {
    val isMe = message.authorIsMe

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.78f),
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
        ) {
            if (!isMe) {
                val color = runCatching {
                    Color(android.graphics.Color.parseColor(message.authorAvatarColor))
                }.getOrDefault(Color(0xFF00E5FF))

                Text(
                    text = message.authorName,
                    color = color,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 12.dp, bottom = 2.dp)
                )
            }

            Surface(
                shape = if (isMe) {
                    RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
                } else {
                    RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
                },
                color = if (isMe) Color.Transparent else Color.White.copy(alpha = 0.06f),
                modifier = if (isMe) {
                    Modifier.background(
                        Brush.linearGradient(
                            colors = listOf(TribelyPink, Color(0xFFC8338C))
                        ),
                        shape = RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
                    )
                } else {
                    Modifier
                }
            ) {
                Text(
                    text = message.text,
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp)
                )
            }

            Text(
                text = formatMessageTime(message.createdAt),
                color = Color.White.copy(alpha = 0.35f),
                fontSize = 10.sp,
                modifier = Modifier.padding(
                    start = if (isMe) 0.dp else 6.dp,
                    end = if (isMe) 6.dp else 0.dp,
                    top = 2.dp
                )
            )
        }
    }
}

private fun formatMessageTime(createdAt: Instant): String {
    val tz = TimeZone.currentSystemDefault()
    val local = createdAt.toLocalDateTime(tz)
    val h = local.hour.toString().padStart(2, '0')
    val m = local.minute.toString().padStart(2, '0')
    return "$h:$m"
}
