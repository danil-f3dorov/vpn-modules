package io.indianvpn.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indianvpn.R
import common.domain.model.Server
import common.util.parse.ParseFlag
import common.util.ui.NoRippleInteractionSource
import io.indianvpn.compose.GradientButton
import io.indianvpn.compose.VpnItemWithPing
import io.indianvpn.ui.theme.Gilroy
import io.indianvpn.ui.theme.labelLarge
import io.indianvpn.ui.theme.textColor2
import io.indianvpn.ui.theme.textColor3
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun SelectServerScreen(
    srvListFlow: StateFlow<List<Server>?>,
    updateSrvList: () -> Unit,
    navHome:() -> Unit,
    backStack:() -> Unit,
    setCurrentServer:(srv: Server) -> Unit,
    startVpn:() -> Unit
) {

    val srvList = remember { srvListFlow }.collectAsState()

    LaunchedEffect(Unit) {
        updateSrvList()
    }

    Column(
        modifier = Modifier
            .padding(vertical = 36.dp)
            .padding(horizontal = 16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.Start,

        ) {
        Row(
            modifier = Modifier
                .padding()
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back),
                contentDescription = "back",
                tint = textColor2,
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = NoRippleInteractionSource,
                    onClick = backStack
                )
            )
            Text(
                text = "VPN",
                style = labelLarge
            )
        }

        Spacer(Modifier.height(40.dp))

        GradientButton(onClick = {
            srvList.value?.let { setCurrentServer(it.random()) }
            navHome()
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
                    text = "Auto selection",
                    fontFamily = Gilroy.medium,
                    fontSize = 24.sp,
                    color = textColor3
                )
            }
        }

        Spacer(Modifier.height(40.dp))

        Text(
            text = "LOCATION",
            fontFamily = Gilroy.medium,
            fontSize = 24.sp,
            color = textColor2
        )

        Spacer(Modifier.height(24.dp))

        LazyColumn {
            items(srvList.value ?: emptyList()) { server ->
                VpnItemWithPing(
                    imageId = ParseFlag.findFlagForServer(server),
                    country = server.country,
                    ip = server.ip,
                    rowModifier = Modifier.clickable(
                        interactionSource = NoRippleInteractionSource,
                        indication = null,
                        onClick = {
                            setCurrentServer(server)
                            navHome()
                            startVpn()
                        }
                    )
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}


@Composable
@Preview(showBackground = true)
private fun SelectServerScreenPreview() {
    SelectServerScreen(
        srvListFlow  = MutableStateFlow<List<Server>?>(null),
        {},
        {},
        {},
        {},
        {},

    )
}