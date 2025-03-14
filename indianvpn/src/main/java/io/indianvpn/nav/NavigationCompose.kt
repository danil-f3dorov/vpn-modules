package io.indianvpn.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import core.domain.model.Server
import core.util.enum.ScreenState
import core.util.parse.ParseFlag
import core.viewmodel.MainViewModel
import io.indianvpn.screen.FetchServerScreen
import io.indianvpn.screen.HomeScreen
import io.indianvpn.screen.NoInternetScreen
import io.indianvpn.screen.SelectServerScreen
import kotlinx.serialization.Serializable

@Composable
fun ComposableNavigation(
    mainViewModel: MainViewModel,
    navController: NavHostController,
    startVpn: (srv: Server) -> Unit,
    stopVpn: () -> Unit
) {

    val noNetwork = { navController.navigate(Screen.NoInternet) }
    val safetyBacKStack = {
        if (navController.previousBackStackEntry != null) {
            navController.popBackStack()
        }
    }

    NavHost(
        navController = navController, startDestination = Screen.FetchServer
    ) {
        composable<Screen.FetchServer> {
            FetchServerScreen(
                fetchServerList = {
                    mainViewModel.fetchServerList(noNetwork) {
                        navController.navigate(Screen.Home) {
                            popUpTo(Screen.FetchServer) { inclusive = true }
                        }

                    }
                }
            )

        }
        composable<Screen.Home> {
            HomeScreen(
                imageId = ParseFlag.findFlagForServer(mainViewModel.currentServer),
                countryName = mainViewModel.currentServer?.country,
                ip = mainViewModel.currentServer?.ip,
                screenState = mainViewModel.screenState,
                onClickConnect = { mainViewModel.currentServer?.let { it1 -> startVpn(it1) } },
                onClickChange = {
                    navController.navigate(Screen.SelectServer) {
                        launchSingleTop = true
                    }
                },
                onClickStopVpn = {
                    stopVpn()
                    mainViewModel.screenState.value = ScreenState.Disconnected
                },
                navNoInternet = noNetwork
            )
        }
        composable<Screen.SelectServer> {
            SelectServerScreen(
                srvListFlow = mainViewModel.srvListStateFlow,
                updateSrvList = { mainViewModel.updateSrvListStateFlow() },
                navHome = {
                    navController.navigate(Screen.Home) {
                        popUpTo(Screen.SelectServer) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                backStack = safetyBacKStack,
                setCurrentServer = {
                    mainViewModel.currentServer = it
                },
                startVpn = { mainViewModel.currentServer?.let { it1 -> startVpn(it1) } }

            )
        }
        composable<Screen.NoInternet> {
            NoInternetScreen {
                navController.popBackStack()
            }
        }
    }
}

sealed class Screen {
    @kotlinx.serialization.Serializable
    data object FetchServer : Screen()

    @kotlinx.serialization.Serializable
    data object Home : Screen()

    @kotlinx.serialization.Serializable
    data object SelectServer : Screen()

    @Serializable
    data object NoInternet : Screen()

}