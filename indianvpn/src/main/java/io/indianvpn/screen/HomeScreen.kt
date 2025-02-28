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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indianvpn.R
import common.util.enum.HomeScreenState
import common.util.timer.StopwatchManager
import io.indianvpn.compose.ConnectButton
import io.indianvpn.compose.Stopwatch
import io.indianvpn.compose.VpnItemWithChangeButton
import io.indianvpn.ui.theme.Gilroy
import io.indianvpn.ui.theme.labelLarge
import io.indianvpn.ui.theme.textColor2

@Composable
fun HomeScreen(
    imageId: Int?,
    countryName: String?,
    ip: String?,
    screenState: State<HomeScreenState>,
    onClickConnect: () -> Unit,
    onClickChange: () -> Unit,
    onClickStopVpn: () -> Unit
) {


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
                HomeScreenState.Disconnected -> {
                    Text(
                        text = "DISCONNECTED",
                        fontFamily = Gilroy.medium,
                        fontWeight = FontWeight.Medium,
                        fontSize = 28.sp,
                        color = textColor2
                    )
                }
                HomeScreenState.Connected -> {
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
                HomeScreenState.Connecting -> {
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
                imageId, countryName, ip, onClickChange
            )
            Spacer(Modifier.height(16.dp))
            ConnectButton(
                screenState = screenState.value,
                onClickConnect = {
                    onClickConnect()
                },
                onClickStopVpn = {
                    onClickStopVpn()
                }
            )

        }
    }
    when(screenState.value) {
        HomeScreenState.Disconnected -> {
            StopwatchManager.stop()
        }
        HomeScreenState.Connected ->  {
            StopwatchManager.start()

        }
        HomeScreenState.Connecting -> {

        }
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
@Preview
private fun HomeScreenPreview() {
    HomeScreen(
        com.common.R.drawable.ic_russia,
        "Novosibirsk",
        "123.456.789.000",
        mutableStateOf(HomeScreenState.Disconnected),
        {},
        {},
        {}
    )
}