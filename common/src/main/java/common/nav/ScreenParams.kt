package common.nav

import androidx.compose.runtime.State
import common.domain.model.Server
import common.util.enum.HomeScreenState
import kotlinx.coroutines.flow.StateFlow

data class FetchServerScreenParams(
    val fetchServerList: () -> Unit
)

data class HomeScreenParams(
    val imageId: Int?,
    val countryName: String?,
    val ip: String?,
    val screenState: State<HomeScreenState>,
    val onClickConnect: () -> Unit,
    val onClickChange: () -> Unit,
    val onClickStopVpn: () -> Unit
)

data class SelectServerScreenParams(
    val srvListFlow: StateFlow<List<Server>?>,
    val updateSrvList: () -> Unit,
    val navHome: () -> Unit,
    val backStack: () -> Unit,
    val setCurrentServer: (srv: Server) -> Unit,
    val startVpn: () -> Unit
)

data class NoInternetScreenParams(
    val onClick: () -> Unit
)