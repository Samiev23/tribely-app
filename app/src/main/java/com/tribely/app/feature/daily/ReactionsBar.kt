package com.tribely.app.feature.daily

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tribely.app.core.data.model.ReactionType
import com.tribely.app.core.data.model.ReactionsState
import com.tribely.app.core.ui.theme.TribelyAccent

@Composable
fun ReactionsBar(
    reactions: ReactionsState,
    isMyPhoto: Boolean,
    onReact: (ReactionType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ReactionButton(
            emoji = ReactionType.FIRE.emoji,
            count = reactions.fireCount,
            isActive = reactions.myFire,
            isReadOnly = isMyPhoto,
            onClick = { onReact(ReactionType.FIRE) }
        )
        ReactionButton(
            emoji = ReactionType.LAUGH.emoji,
            count = reactions.laughCount,
            isActive = reactions.myLaugh,
            isReadOnly = isMyPhoto,
            onClick = { onReact(ReactionType.LAUGH) }
        )
    }
}

@Composable
private fun ReactionButton(
    emoji: String,
    count: Int,
    isActive: Boolean,
    isReadOnly: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.05f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "reaction-scale"
    )

    val bgColor = when {
        isActive -> TribelyAccent.copy(alpha = 0.2f)
        else -> Color.White.copy(alpha = 0.08f)
    }
    val borderColor = when {
        isActive -> TribelyAccent
        else -> Color.White.copy(alpha = 0.15f)
    }
    val textColor = when {
        isActive -> TribelyAccent
        else -> Color.White
    }

    Row(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(
                width = if (isActive) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .let { modifier ->
                if (isReadOnly) modifier else modifier.clickable(onClick = onClick)
            }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = emoji,
            fontSize = 18.sp
        )
        if (count > 0) {
            Text(
                text = count.toString(),
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}
