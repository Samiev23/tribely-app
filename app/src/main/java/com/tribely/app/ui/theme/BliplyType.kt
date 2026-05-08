package com.tribely.app.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.tribely.app.R

val unboundedFamily = FontFamily(
    Font(R.font.unbounded_regular, FontWeight.Normal),
    Font(R.font.unbounded_semibold, FontWeight.SemiBold),
    Font(R.font.unbounded_bold, FontWeight.Bold)
)

val manropeFamily = FontFamily(
    Font(R.font.manrope_semibold, FontWeight.SemiBold)
)

val BliplyLogoTitleStyle = TextStyle(
    fontFamily = unboundedFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 22.sp,
    letterSpacing = 1.sp,
    color = BliplyColors.TextPrimary
)
