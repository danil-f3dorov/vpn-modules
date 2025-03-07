package io.indianvpn.compose

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.core.R


@Composable
fun VpnItemWithPing(
    imageId: Int,
    country: String,
    ip: String,
    modifier: Modifier = Modifier
) {
    VpnItemSample(
        imageId = imageId,
        country = country,
        ip = ip,
        modifier = modifier
    ) {
        Image(
            painter = painterResource(com.indianvpn.R.drawable.ic_ping),
            contentDescription = "ping"
        )
    }
}

@Composable
@Preview
private fun VpnItemWithPingPreview() {
    VpnItemWithPing(
        R.drawable.ic_united_states,
        "moscow",
        "123.421.32.12"
    )
}