package io.indianvpn.compose

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import common.util.enum.HomeScreenState
import common.util.ui.NoRippleInteractionSource
import io.indianvpn.ui.theme.Gilroy
import io.indianvpn.ui.theme.darkPeach
import io.indianvpn.ui.theme.textColor2
import io.indianvpn.ui.theme.textColor3
import kotlinx.coroutines.delay

@Composable
fun ConnectButton(
    screenState: HomeScreenState,
    onClickConnect: () -> Unit,
    onClickStopVpn: () -> Unit
) {

    var connectingString by remember { mutableStateOf("Connecting") }

    when (screenState) {
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
                    text = "Disconnect",
                    fontFamily = Gilroy.medium,
                    fontWeight = FontWeight.Thin,
                    fontSize = 20.sp,
                    color = textColor2
                )
            }
        }
        HomeScreenState.Connecting -> {
            LaunchedEffect(Unit) {
                var dots = 0
                while (true) {
                    delay(450L)
                    Log.i("dots", "$dots")
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
                    fontFamily = Gilroy.medium,
                    fontWeight = FontWeight.Medium,
                    fontSize = 24.sp,
                    color = textColor2
                )
            }
        }
    }
}