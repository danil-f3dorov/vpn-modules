package common.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import common.domain.model.Server
import common.util.enum.HomeScreenState
import common.util.parse.ParseFlag
import common.viewmodel.MainViewModel

@Composable
fun VpnNavigation(
    mainViewModel: MainViewModel,
    navController: NavHostController,
    screens: NavigationScreens,
    startVpn: (srv: Server) -> Unit,
    stopVpn: () -> Unit,
) {
    val noNetwork = { navController.navigate(Screen.NoInternet) }
    val safetyBackStack = {
        if (navController.previousBackStackEntry != null) {
            navController.popBackStack()
        }
    }

    NavHost(
        navController = navController, startDestination = Screen.FetchServer
    ) {
        composable<Screen.FetchServer> {
            screens.fetchServerScreen(
                FetchServerScreenParams(
                    fetchServerList = {
                        mainViewModel.fetchServerList(noNetwork, navHome = {
                            navController.navigate(Screen.Home) {
                                popUpTo(Screen.SelectServer) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                        )
                    }
                )
            )
        }

        composable<Screen.Home> {
            screens.homeScreen(
                HomeScreenParams(
                    imageId = ParseFlag.findFlagForServer(mainViewModel.currentServer),
                    countryName = mainViewModel.currentServer?.country,
                    ip = mainViewModel.currentServer?.ip,
                    screenState = mainViewModel.screenState,
                    onClickConnect = { mainViewModel.currentServer?.let { startVpn(it) } },
                    onClickChange = { navController.navigate(Screen.SelectServer) },
                    onClickStopVpn = {
                        stopVpn()
                        mainViewModel.screenState.value = HomeScreenState.Disconnected
                    }
                )
            )
        }

        composable<Screen.SelectServer> {
            screens.selectServerScreen(
                SelectServerScreenParams(
                    srvListFlow = mainViewModel.srvListStateFlow,
                    updateSrvList = { mainViewModel.updateSrvListStateFlow() },
                    navHome = {
                        navController.navigate(Screen.Home) {
                            popUpTo(Screen.SelectServer) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    backStack = safetyBackStack,
                    setCurrentServer = { mainViewModel.currentServer = it },
                    startVpn = { mainViewModel.currentServer?.let { startVpn(it) } }
                )
            )
        }

        composable<Screen.NoInternet> {
            screens.noInternetScreen(
                NoInternetScreenParams(
                    onClick = { navController.popBackStack() }
                )
            )
        }
    }
}