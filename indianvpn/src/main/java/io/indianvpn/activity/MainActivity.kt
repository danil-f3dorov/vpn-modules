package io.indianvpn.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import common.activity.VpnActivity
import io.indianvpn.nav.ComposableNavigation
import io.indianvpn.ui.theme.IndianVpnTheme

class MainActivity : VpnActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navHostController = rememberNavController()

            IndianVpnTheme {
                ComposableNavigation(
                    mainViewModel,
                    navHostController,
                    startVpn = {
                        startVpn(it, requestPermissionLauncher)
                    },
                    stopVpn = {
                        stopVpn()
                    }
                )
            }
        }
    }
}
