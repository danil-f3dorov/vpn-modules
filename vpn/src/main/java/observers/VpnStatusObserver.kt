package observers

import de.blinkt.openvpn.core.VpnStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

object VpnStatusObserver {
    private val _vpnState = MutableStateFlow(VpnStatus.ConnectionState.LEVEL_NOT_CONNECTED)

    val vpnState: Flow<VpnStatus.ConnectionState>
        get() = _vpnState

    fun setVpnState(status: VpnStatus.ConnectionState) {
        _vpnState.value = status
    }

}