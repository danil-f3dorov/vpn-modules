package io.indianvpn.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import common.util.ui.NoRippleInteractionSource
import io.indianvpn.compose.GradientButton
import io.indianvpn.compose.Stopwatch
import io.indianvpn.compose.VpnItemWithChangeButton
import io.indianvpn.ui.theme.Gilroy
import io.indianvpn.ui.theme.darkPeach
import io.indianvpn.ui.theme.labelLarge
import io.indianvpn.ui.theme.textColor2
import io.indianvpn.ui.theme.textColor3
import kotlinx.coroutines.delay

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

    var connectingString by remember { mutableStateOf("Connecting")}
    val coroutineScope = rememberCoroutineScope()

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
                .padding(vertical = 40.dp)
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
        Image(
            painter = painterResource(R.drawable.ic_lightning_fill),
            contentDescription = null
        )
        Spacer(Modifier.height(120.dp))
        when(screenState.value) {
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
                Text(
                    text = "CONNECTED",
                    fontFamily = Gilroy.medium,
                    fontWeight = FontWeight.Medium,
                    fontSize = 28.sp,
                    color = textColor2
                )
                Stopwatch()
            }
            HomeScreenState.Connecting -> {

            }
        }
        Spacer(Modifier.height(60.dp))
        VpnItemWithChangeButton(
            imageId, countryName, ip, onClickChange
        )
        Spacer(Modifier.height(16.dp))

        when (screenState.value) {
            HomeScreenState.Disconnected -> {
                GradientButton(
                    onClick = onClickConnect
                ) {
                    Text(
                        text = "Connect",
                        fontFamily = Gilroy.medium,
                        fontSize = 24.sp,
                        color = textColor3
                    )
                }
            }

            HomeScreenState.Connected -> {
                Box(
                    modifier = Modifier
                        .height(90.dp)
                        .fillMaxWidth()
                        .background(darkPeach, shape = RoundedCornerShape(30.dp))
                        .clickable(
                            indication = null,
                            interactionSource = NoRippleInteractionSource,
                            onClick = { onClickStopVpn() }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Disconnect"
                    )
                }
            }
            HomeScreenState.Connecting -> {
                LaunchedEffect(Unit) {
                    var dots = 0
                    while (screenState.value == HomeScreenState.Connecting) {
                        delay(450L)
                        dots = (dots + 1) % 4
                        connectingString = "Connecting${".".repeat(dots)}"
                    }
                }

                Box(
                    modifier = Modifier
                        .height(90.dp)
                        .fillMaxWidth()
                        .clickable(
                            indication = null,
                            interactionSource = NoRippleInteractionSource,
                            onClick = {}
                        )
                        .border(width = 1.dp,
                            color = textColor2,
                            shape = RoundedCornerShape(30.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = connectingString,

                    )
                }
            }
        }
    }
}

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