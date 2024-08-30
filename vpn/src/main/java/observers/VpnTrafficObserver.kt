package observers

import de.blinkt.openvpn.core.OpenVPNService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

object VpnTrafficObserver {
    private val _downloadSpeed = MutableStateFlow("--")
    private val _uploadSpeed = MutableStateFlow("--")

    val downloadSpeed: Flow<String>
        get() = _downloadSpeed

    val uploadSpeed: Flow<String>
        get() = _uploadSpeed

    private fun setDownloadSpeed(downloadSpeed: String) {
        _downloadSpeed.value = downloadSpeed
    }

    private fun setUploadSpeed(uploadSpeed: String) {
        _uploadSpeed.value = uploadSpeed
    }

    fun calcTraffic(`in`: Long, out: Long) {
        setDownloadSpeed(OpenVPNService.humanReadableByteCount(`in`, false))
        setUploadSpeed(OpenVPNService.humanReadableByteCount(out, false))
    }
}