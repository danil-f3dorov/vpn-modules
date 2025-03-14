package core.callback

import com.progun.dunta_sdk.api.SocketCallback
import de.blinkt.openvpn.core.OpenVPNService
import java.net.Socket

/**
 * Интерфейс из dunta_sdk который нужен чтобы socket не перенаправлял трафик через vpn
 */
class SocketCallbackImpl(private val vpnService: OpenVPNService?) : SocketCallback {

    /**
     * Вызывается при создании нового сокета.
     *
     * Этот метод проверяет, не является ли сокет null, и если нет, то защищает его с помощью
     * метода [protect] из [OpenVPNService].
     *
     * @param socket Сокет dunta_sdk.
     */
    override fun onSocketCreate(socket: Socket?) {
        if (socket != null) {
            vpnService?.protect(socket)
        }
    }
}