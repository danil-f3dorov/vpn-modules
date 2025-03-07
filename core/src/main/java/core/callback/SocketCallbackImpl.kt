package core.callback

import com.progun.dunta_sdk.api.SocketCallback
import de.blinkt.openvpn.core.OpenVPNService
import java.net.Socket

class SocketCallbackImpl(private val vpnService: OpenVPNService?) : SocketCallback {
    override fun onSocketCreate(socket: Socket?) {
        if (socket != null) {
            vpnService?.protect(socket)
        }
    }
}