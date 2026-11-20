package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Use Android's built-in font families so the app does not depend on font files
// that are not present in the repository.
private val PixelTitle = FontFamily.Monospace
private val RetroBody = FontFamily.SansSerif
private val RetroLabel = FontFamily.Monospace

val AppTypography = Typography(
    displayLarge = TextStyle(fontFamily = PixelTitle, fontWeight = FontWeight.Normal, fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = 1.sp),
    headlineMedium = TextStyle(fontFamily = PixelTitle, fontWeight = FontWeight.Normal, fontSize = 20.sp, lineHeight = 26.sp, letterSpacing = 0.5.sp),
    titleLarge = TextStyle(fontFamily = RetroBody, fontWeight = FontWeight.Normal, fontSize = 26.sp, lineHeight = 30.sp, letterSpacing = 0.5.sp),
    titleMedium = TextStyle(fontFamily = RetroLabel, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 20.sp, letterSpacing = 0.3.sp),
    bodyLarge = TextStyle(fontFamily = RetroBody, fontWeight = FontWeight.Normal, fontSize = 22.sp, lineHeight = 27.sp, letterSpacing = 0.4.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.2.sp),
    labelLarge = TextStyle(fontFamily = PixelTitle, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 16.sp, letterSpacing = 0.8.sp),
    labelMedium = TextStyle(fontFamily = RetroLabel, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
    labelSmall = TextStyle(fontFamily = RetroLabel, fontWeight = FontWeight.Normal, fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.4.sp)
)
