package io.indianvpn.nav

//@Composable
//fun ComposableNavigation(
//    mainViewModel: MainViewModel,
//    navController: NavHostController,
//    startVpn: (srv: Server) -> Unit,
//    stopVpn: () -> Unit
//) {
//
//    val noNetwork = { navController.navigate(Screen.NoInternet) }
//    val safetyBacKStack = {
//        if (navController.previousBackStackEntry != null) {
//            navController.popBackStack()
//        }
//    }
//
//    NavHost(
//        navController = navController, startDestination = Screen.FetchServer
//    ) {
//        composable<Screen.FetchServer> {
//            FetchServerScreen(
//                fetchServerList = { navHome ->
//                    mainViewModel.fetchServerList(noNetwork, navHome)
//                }
//            ) {
//                navController.navigate(Screen.Home) {
//                    popUpTo(Screen.FetchServer) { inclusive = true }
//                }
//
//            }
//
//        }
//        composable<Screen.Home> {
//            HomeScreen(
//                imageId = ParseFlag.findFlagForServer(mainViewModel.currentServer),
//                countryName = mainViewModel.currentServer?.country,
//                ip = mainViewModel.currentServer?.ip,
//                screenState = mainViewModel.screenState,
//                onClickConnect = { mainViewModel.currentServer?.let { it1 -> startVpn(it1) } },
//                onClickChange = {
//                    navController.navigate(Screen.SelectServer) {
//                        launchSingleTop = true
//                    }
//                },
//                onClickStopVpn = {
//                    stopVpn()
//                    mainViewModel.screenState.value = HomeScreenState.Disconnected
//                }
//            )
//        }
//        composable<Screen.SelectServer> {
//            SelectServerScreen(
//                srvListFlow = mainViewModel.srvListStateFlow,
//                updateSrvList = { mainViewModel.updateSrvListStateFlow() },
//                navHome = {
//                    navController.navigate(Screen.Home) {
//                        popUpTo(Screen.SelectServer) { inclusive = true }
//                        launchSingleTop = true
//                    }
//                },
//                backStack = safetyBacKStack,
//                setCurrentServer = {
//                    mainViewModel.currentServer = it
//                },
//                startVpn = { mainViewModel.currentServer?.let { it1 -> startVpn(it1) } }
//
//            )
//        }
//        composable<Screen.NoInternet> {
//            NoInternetScreen {
//                navController.popBackStack()
//            }
//        }
//    }
//}
//
//sealed class Screen {
//    @Serializable
//    data object FetchServer : Screen()
//
//    @Serializable
//    data object Home : Screen()
//
//    @Serializable
//    data object SelectServer : Screen()
//
//    @Serializable
//    data object NoInternet : Screen()
//
//}