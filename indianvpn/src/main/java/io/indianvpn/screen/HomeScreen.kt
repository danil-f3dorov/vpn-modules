package io.indianvpn.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indianvpn.R
import core.util.enum.ScreenState
import core.util.timer.StopwatchManager
import io.indianvpn.compose.ConnectButton
import io.indianvpn.compose.Stopwatch
import io.indianvpn.compose.VpnItemWithChangeButton
import io.indianvpn.compose.wrapNoNetwork
import io.indianvpn.ui.theme.Gilroy
import io.indianvpn.ui.theme.labelLarge
import io.indianvpn.ui.theme.textColor2

@Composable
fun HomeScreen(
    imageId: Int?,
    countryName: String?,
    ip: String?,
    screenState: State<ScreenState>,
    onClickConnect: () -> Unit,
    onClickChange: () -> Unit,
    onClickStopVpn: () -> Unit,
    navNoInternet: () -> Unit
) {

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color.White)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .padding(top = 40.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.ic_globesimple),
                contentDescription = null
            )
            Text(
                text = "VPN",
                style = labelLarge
            )
        }
        Spacer(Modifier.weight(1f))
        Image(
            painter = painterResource(R.drawable.ic_lightning_fill),
            contentDescription = null
        )
        Spacer(Modifier.weight(1f))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (screenState.value) {
                ScreenState.Disconnected -> {
                    Text(
                        text = "DISCONNECTED",
                        fontFamily = Gilroy.medium,
                        fontWeight = FontWeight.Medium,
                        fontSize = 28.sp,
                        color = textColor2
                    )
                }

                ScreenState.Connected -> {
                    StopwatchManager.start()
                    Text(
                        text = "CONNECTED",
                        fontFamily = Gilroy.medium,
                        fontWeight = FontWeight.Medium,
                        fontSize = 28.sp,
                        color = textColor2
                    )
                    Spacer(Modifier.height(8.dp))
                    Stopwatch()
                }

                ScreenState.Connecting -> {
                }
            }
        }
        Spacer(Modifier.weight(1f))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            VpnItemWithChangeButton(
                imageId, countryName, ip, onClickChange = {
                    wrapNoNetwork(
                        context = context,
                        action = onClickChange,
                        navNoInternet = navNoInternet
                    )
                }
            )
            Spacer(Modifier.height(16.dp))
            ConnectButton(
                screenState = screenState.value,
                onClickConnect = {
                    wrapNoNetwork(
                        context = context,
                        action = onClickConnect,
                        navNoInternet = navNoInternet
                    )
                },
                onClickStopVpn = {
                    onClickStopVpn()
                }
            )

        }
    }
    when (screenState.value) {
        ScreenState.Disconnected -> {
            StopwatchManager.stop()
        }

        ScreenState.Connected -> {
            StopwatchManager.start()

        }

        ScreenState.Connecting -> {

        }
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
@Preview
private fun HomeScreenPreview() {
    HomeScreen(
        com.core.R.drawable.ic_russia,
        "Novosibirsk",
        "123.456.789.000",
        mutableStateOf(ScreenState.Disconnected),
        {},
        {},
        {},
        {}
    )
}