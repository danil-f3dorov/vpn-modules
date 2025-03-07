package com.vpnduck.activity

import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.vpnduck.activity.ui.theme.VpnDuckTheme

class MainActivity : VpnActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VpnDuckTheme {
                VpnNavigation(
                    mainViewModel = mainViewModel,
                    navController = rememberNavController(),
                    screens = NavigationScreens(
                        fetchServerScreen = { params -> {
                            FetchServerScreen(params)
                        }

                        },

                    ),
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

