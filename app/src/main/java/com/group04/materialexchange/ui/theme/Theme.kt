package com.group04.materialexchange.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val ReuseGreen = Color(0xFF387C3E)
val DustyRose = Color(0xFFDE9C9D)
val Blush = Color(0xFFF9EFEE)
val MutedRose = Color(0xFFE2D4D3)
val RejectRed = Color(0xFFB62F3A)
val CardShape = RoundedCornerShape(12.dp)

@Composable
fun ExchangeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = ReuseGreen,
            onPrimary = Color.White,
            secondary = DustyRose,
            background = Color.White,
            surface = Color.White,
            onSurface = Color.Black,
            onBackground = Color.Black,
            error = RejectRed
        ),
        content = content
    )
}
