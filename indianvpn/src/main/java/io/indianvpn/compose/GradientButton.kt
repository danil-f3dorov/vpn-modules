package io.indianvpn.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import common.util.ui.NoRippleInteractionSource
import io.indianvpn.ui.theme.darkPeach
import io.indianvpn.ui.theme.yellow


@Composable
fun GradientButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .height(90.dp)
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = NoRippleInteractionSource,
                onClick = onClick
            )
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(darkPeach, yellow),
                ),
                shape = RoundedCornerShape(30.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}


@Composable
@Preview
private fun GradientButtonPreview() {
    GradientButton(onClick = {}, content = {
        Text("popa")
    })
}
