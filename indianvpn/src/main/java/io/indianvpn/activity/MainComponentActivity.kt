package io.indianvpn.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import core.activity.VpnComponentActivity
import io.indianvpn.nav.ComposableNavigation
import io.indianvpn.ui.theme.IndianVpnTheme

class MainComponentActivity : VpnComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IndianVpnTheme {
                ComposableNavigation(
                    mainViewModel = mainViewModel,
                    navController = rememberNavController(),
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
