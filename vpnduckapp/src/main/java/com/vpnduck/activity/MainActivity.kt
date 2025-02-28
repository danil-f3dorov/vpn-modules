package com.vpnduck.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.vpnduck.activity.ui.theme.VpnDuckTheme
import common.activity.VpnActivity
import common.nav.VpnNavigation

class MainActivity : VpnActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VpnDuckTheme {
                VpnNavigation(
                    mainViewModel = mainViewModel,
                    navController = rememberNavController(),
                    screens = ,
                    startVpn = {
                        startVpn(it)
                    },
                    stopVpn = {
                        stopVpn()
                    }

                )
            }
        }
    }
}