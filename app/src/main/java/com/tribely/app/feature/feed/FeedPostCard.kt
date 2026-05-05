package com.tribely.app.feature.feed

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tribely.app.core.data.model.ReactionType
import com.tribely.app.core.data.model.SubmissionWithAuthor
import com.tribely.app.core.ui.theme.TribelyAccent
import com.tribely.app.core.util.TimeFormat

private val cardBgPalette = listOf(
    listOf(Color(0xFF6B2C8E), Color(0xFF4A1C68)),
    listOf(Color(0xFF1B5A6B), Color(0xFF0F3D4A)),
    listOf(Color(0xFF8E2C5E), Color(0xFF5A1838)),
    listOf(Color(0xFF4A5A28), Color(0xFF2C3818))
)

@Composable
fun FeedPostCard(
    submission: SubmissionWithAuthor,
    challengeText: String?,
    bgColorIndex: Int,
    onPhotoClick: () -> Unit,
    onReact: (ReactionType) -> Unit,
    onMenuClick: () -> Unit,
    onCommentClick: () -> Unit
) {
    val avatarColor = remember(submission.authorName) {
        val palette = listOf(
            0xFFFF3EA5L,
            0xFFD4FF00L,
            0xFF00E5FFL,
            0xFFFFAA00L,
            0xFFA855F7L
        )
        val hash = kotlin.math.abs(submission.authorName.hashCode())
        Color(palette[hash % palette.size])
    }
    val bgColors = cardBgPalette[bgColorIndex % cardBgPalette.size]

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF111111),
        border = BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.04f)
        )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(avatarColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = submission.authorName.take(1).uppercase(),
                        color = Color.Black,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = submission.authorName,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                        if (submission.isMine) {
                            Text(
                                text = " (ты)",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 13.sp
                            )
                        }
                    }
                    if (!challengeText.isNullOrBlank()) {
                        Text(
                            text = challengeText,
                            color = Color.White.copy(alpha = 0.55f),
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    }
                }
                Text(
                    text = TimeFormat.timeAgo(submission.createdAt),
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onMenuClick),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⋮",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 20.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 11f)
                    .background(Brush.linearGradient(colors = bgColors))
                    .clickable(onClick = onPhotoClick)
            ) {
                AsyncImage(
                    model = submission.mediaUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                if (submission.isMine) {
                    Surface(
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.TopStart),
                        shape = RoundedCornerShape(8.dp),
                        color = TribelyAccent
                    ) {
                        Text(
                            text = "ТЫ",
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (!submission.caption.isNullOrBlank()) {
                Text(
                    text = submission.caption,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 4.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InlineReactionPill(
                    emoji = "🔥",
                    count = submission.reactions.fireCount,
                    isActive = submission.reactions.myFire,
                    isReadOnly = submission.isMine,
                    onClick = { onReact(ReactionType.FIRE) }
                )
                InlineReactionPill(
                    emoji = "😂",
                    count = submission.reactions.laughCount,
                    isActive = submission.reactions.myLaugh,
                    isReadOnly = submission.isMine,
                    onClick = { onReact(ReactionType.LAUGH) }
                )
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onCommentClick),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "💬",
                        fontSize = 18.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun LeaderOfDayCard(
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFFFAA00).copy(alpha = 0.06f),
        border = BorderStroke(
            width = 1.dp,
            color = Color(0xFFFFAA00).copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "👑 Лидер дня",
                    color = Color(0xFFFFAA00),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    letterSpacing = 1.5.sp
                )
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "СКОРО",
                        color = Color.White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Появится\nпосле дуэлей",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 26.sp,
                lineHeight = 30.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Победитель чата КНБ получает корону на 24ч",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun InlineReactionPill(
    emoji: String,
    count: Int,
    isActive: Boolean,
    isReadOnly: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isActive) {
        TribelyAccent.copy(alpha = 0.15f)
    } else {
        Color.White.copy(alpha = 0.06f)
    }
    val borderColor = if (isActive) {
        TribelyAccent.copy(alpha = 0.5f)
    } else {
        Color.White.copy(alpha = 0.1f)
    }
    val textColor = if (isActive) {
        TribelyAccent
    } else {
        Color.White.copy(alpha = 0.8f)
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(bgColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(18.dp)
            )
            .let { modifier ->
                if (isReadOnly) modifier else modifier.clickable(onClick = onClick)
            }
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = emoji, fontSize = 15.sp)
        if (count > 0) {
            Text(
                text = count.toString(),
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}
