package io.indianvpn.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext


private val LightColorScheme = lightColorScheme(
    background = Color.White,
    primary = peach
)

@Composable
fun IndianVpnTheme(

    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
    val context = LocalContext.current
    val window = (context as Activity).window
    window.statusBarColor = colorScheme.background.toArgb()
}