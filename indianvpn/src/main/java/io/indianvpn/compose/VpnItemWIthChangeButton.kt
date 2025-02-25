package io.indianvpn.compose

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.common.R
import common.util.ui.NoRippleInteractionSource
import io.indianvpn.ui.theme.Gilroy
import io.indianvpn.ui.theme.textColor2


@Composable
fun VpnItemWithChangeButton(
    imageId: Int?,
    country: String?,
    ip: String?,
    onClickChange: () -> Unit
) {

    val roundedCornerShape = RoundedCornerShape(30.dp)

    VpnItemSample(
        imageId,
        country,
        ip
    ) {
        Box(
            modifier = Modifier
                .clip(roundedCornerShape)
                .border(width = 1.dp, color = textColor2, shape = roundedCornerShape)
                .padding(horizontal = 12.dp)
                .padding(vertical = 6.dp)
                .clickable(
                    interactionSource = NoRippleInteractionSource,
                    indication = null,
                    onClick = onClickChange
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Change",
                fontFamily = Gilroy.medium,
                fontSize = 16.sp,
                color = textColor2
            )
        }
    }
}

@Composable
@Preview
private fun VpnItemWithChangeButtonPreview() {
    VpnItemWithChangeButton(
        R.drawable.ic_united_states,
        "moscow",
        "123.421.32.12"
    ) {}
}