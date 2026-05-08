package com.tribely.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tribely.app.ui.theme.BliplyColors
import com.tribely.app.ui.theme.manropeFamily

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(100.dp)
    val label = text.removeSuffix("→").trimEnd()

    Row(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = shape,
                ambientColor = BliplyColors.NeonPink,
                spotColor = BliplyColors.NeonPink
            )
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        BliplyColors.NeonPink,
                        BliplyColors.NeonPurple,
                        BliplyColors.NeonBlue
                    )
                ),
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 28.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.White,
            fontFamily = manropeFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        )
        Text(
            text = "→",
            color = Color.White,
            fontFamily = manropeFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        )
    }
}
