package com.vpnduck.activity.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp


private val colorScheme = darkColorScheme(
    background = background,
    onBackground = onBackground,
    secondary = white,
    tertiary = dim
)

private val typography = Typography(
    titleLarge = TextStyle(
        fontFamily = FontFamily(plus_jakarta_sans_bold),
        fontSize = 35.sp,
        color = Color.White
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily(plus_jakarta_sans_medium),
        fontSize = 14.sp,
        color = Color.White
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily(poppins_regular),
        fontSize = 12.sp,
        color = dim
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily(plus_jakarta_sans_regular),
        fontSize = 12.sp,
        color = dim
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily(plus_jakarta_sans_regular),
        fontSize = 14.sp,
        color = dim
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily(plus_jakarta_sans_extra_bold),
        fontSize = 18.sp,
        color = Color.White
    )


)

@Composable
fun VpnDuckTheme(
    content : @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
        typography = typography
    )
}
