package io.indianvpn.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import common.activity.VpnActivity
import common.nav.NavigationScreens
import common.nav.VpnNavigation
import io.indianvpn.screen.FetchServerScreen
import io.indianvpn.screen.HomeScreen
import io.indianvpn.screen.NoInternetScreen
import io.indianvpn.screen.SelectServerScreen
import io.indianvpn.ui.theme.IndianVpnTheme

class MainActivity : VpnActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IndianVpnTheme {
                VpnNavigation(
                    mainViewModel = mainViewModel,
                    navController = rememberNavController(),
                    screens = NavigationScreens(
                        fetchServerScreen = { params -> FetchServerScreen(
                            fetchServerList = params.fetchServerList
                        ) },
                        homeScreen = { params ->
                            HomeScreen(
                                imageId = params.imageId,
                                countryName = params.countryName,
                                ip = params.ip,
                                screenState = params.screenState,
                                onClickConnect = params.onClickConnect,
                                onClickChange = params.onClickChange,
                                onClickStopVpn = params.onClickStopVpn
                            )
                        },
                        selectServerScreen = { params ->
                            SelectServerScreen(
                                srvListFlow = params.srvListFlow,
                                updateSrvList = params.updateSrvList,
                                navHome = params.navHome,
                                backStack = params.backStack,
                                setCurrentServer = params.setCurrentServer,
                                startVpn = params.startVpn
                            )
                        },
                        noInternetScreen = { params -> NoInternetScreen(params.onClick) }
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
