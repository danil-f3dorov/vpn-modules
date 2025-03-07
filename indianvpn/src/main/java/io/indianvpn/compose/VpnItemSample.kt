package io.indianvpn.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.core.R
import io.indianvpn.ui.theme.Gilroy
import io.indianvpn.ui.theme.textColor
import io.indianvpn.ui.theme.textColor2


@Composable
fun VpnItemSample(
    imageId: Int?,
    country: String?,
    ip: String?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {

    val contentHeight = 46.dp
    val roundedCornerShape = RoundedCornerShape(24.dp)

    Row(
        modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = roundedCornerShape
            )
            .background(Color.White)
            .clip(roundedCornerShape)
            .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(id = imageId ?: R.drawable.ic_cross),
                contentDescription = "vpn server country flag",
                modifier = Modifier
                    .size(contentHeight)
                    .clip(shape = CircleShape)
            )
            Column(
                modifier = Modifier.height(contentHeight),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = country ?: "Unknown",
                    fontFamily = Gilroy.medium,
                    fontSize = 20.sp,
                    color = textColor,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
                Text(
                    text = ip ?: "000.000.000.000",
                    fontFamily = Gilroy.medium,
                    fontSize = 14.sp,
                    color = textColor2
                )
            }
        }
        content()
    }
}


@Composable
@Preview(showBackground = true)
private fun VpnItemSamplePreview() {
    VpnItemSample(
        R.drawable.ic_united_states,
        "United States",
        "00.999.43.21"
    ) {
        Text(
            "popa",
            textAlign = TextAlign.Center
        )
    }
}