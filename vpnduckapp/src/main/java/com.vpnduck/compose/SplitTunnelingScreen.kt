package com.vpnduck.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vpnduck.theme.backgroundPrimary
import com.vpnduck.theme.plusJakartaSans
import com.vpnduck.theme.textColor2
import common.viewmodel.SplitTunnelingVIewModel

@Composable
fun SplitTunnelingScreen(
    vIewModel: SplitTunnelingVIewModel,
    onBackPressed: () -> Unit
) {
    val packageManager = LocalContext.current.packageManager
    val appList = vIewModel.getAppListWithInternetPermission(packageManager)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundPrimary),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopBar("Split Tunneling") {
            onBackPressed()
        }
        Spacer(Modifier.height(40.dp))
        Text(
            modifier = Modifier.padding(horizontal = 48.dp),
            text = "All applications work with an encrypted VPN connection. If you want to exclude any apps, click the button on the right.",
            fontSize = 12.sp,
            fontFamily = plusJakartaSans,
            color = textColor2,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        LazyColumn {
            items(appList.size) {
                AppTunnelingItem(appList[it])
            }
        }
    }
}