package io.indianvpn.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indianvpn.R
import core.util.extensions.isNetworkAvailable
import io.indianvpn.compose.GradientButton
import io.indianvpn.ui.theme.Gilroy
import io.indianvpn.ui.theme.labelLarge
import io.indianvpn.ui.theme.textColor2
import io.indianvpn.ui.theme.textColor3

@Composable
fun NoInternetScreen(
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(vertical = 36.dp)
            .padding(horizontal = 16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .padding()
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier)
            Text(
                text = "VPN",
                style = labelLarge
            )
        }
        Text(
            text = "The network is unavailable. \nPlease connect to the network.",
            fontFamily = Gilroy.heavy,
            fontSize = 22.sp,
            color = textColor2,
            textAlign = TextAlign.Center
        )
        GradientButton(onClick = {
            if (isNetworkAvailable()) onClick()
        }) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrows_clockwise),
                    contentDescription = "try to reconnect",
                    tint = textColor3
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Refresh",
                    fontFamily = Gilroy.medium,
                    fontSize = 24.sp,
                    color = textColor3
                )
            }
        }
    }
}


@Composable
@Preview(showBackground = true)
private fun NoInternetPreview() {
    NoInternetScreen {

    }
}