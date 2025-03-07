package core.activity

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.net.VpnService
import android.os.Bundle
import android.os.IBinder
import android.util.Base64
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import core.App
import core.App.Companion.duntaManager
import core.callback.SocketCallbackImpl
import core.domain.model.Server
import core.util.enum.ScreenState
import core.viewmodel.MainViewModel
import core.viewmodel.MainViewModel.Companion.initDuntaSDK
import de.blinkt.openvpn.core.ConfigParser
import de.blinkt.openvpn.core.OpenVPNService
import de.blinkt.openvpn.core.ProfileManager
import de.blinkt.openvpn.core.VPNLaunchHelper
import de.blinkt.openvpn.core.VpnProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.InputStreamReader

open class VpnActivity : ComponentActivity() {
    protected lateinit var mainViewModel: MainViewModel

    private var isServiceBind = false
    private var vpnService: OpenVPNService? = null
    private var vpnProfile: VpnProfile? = null
    private var job: Job? = null

    private var isVpnConnecting = false

    val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as OpenVPNService.LocalBinder
            vpnService = binder.service
            duntaManager.setSocketCallback(SocketCallbackImpl(vpnService))
            initDuntaSDK()
        }
        override fun onServiceDisconnected(arg0: ComponentName) {
            vpnService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (applicationContext as App).appComponent.inject(this)
        mainViewModel =
            (applicationContext as App).appComponent.factory.create(MainViewModel::class.java)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 2)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                startVpnService()
            } else {

            }
        }

    fun startVpn(currSrv: Server) {
        if (loadVpnProfile(currSrv)) {
            val intent = VpnService.prepare(App.instance)
            if (intent != null) {
                requestPermissionLauncher.launch(intent)
            } else {
                startVpnService()
            }
        }
    }

    private fun startVpnService() {
        mainViewModel.observeTraffic(this)
        isVpnConnecting = true
        VPNLaunchHelper.startOpenVpn(vpnProfile, App.instance)
        mainViewModel.screenState.value = ScreenState.Connecting
        job = lifecycleScope.launch(Dispatchers.Main) {
            delay(15_000)
            if (mainViewModel.screenState.value != ScreenState.Connected && isVpnConnecting) {
                Toast.makeText(this@VpnActivity, "This server is unavailable", Toast.LENGTH_SHORT)
                    .show()
                mainViewModel.screenState.value = ScreenState.Disconnected
                stopVpn()
                this.cancel()
            }
        }
    }

    private fun loadVpnProfile(currentServer: Server): Boolean {
        return try {
            val data: ByteArray = Base64.decode(currentServer.configData, Base64.DEFAULT)
            val cp = ConfigParser()
            val isr = InputStreamReader(ByteArrayInputStream(data))
            cp.parseConfig(isr)
            vpnProfile = cp.convertProfile()
            vpnProfile?.mName = currentServer.country
            ProfileManager.getInstance(App.instance).addProfile(vpnProfile)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun stopVpn() {
        job?.cancel()
        job = null
        isVpnConnecting = false
        ProfileManager.setConntectedVpnProfileDisconnected(App.instance)
        vpnService?.management?.stopVPN(false)
    }

    override fun onResume() {
        super.onResume()
        Intent(this, OpenVPNService::class.java).also {
            it.setAction(OpenVPNService.START_SERVICE)
        }
        isServiceBind = true
        this.bindService(intent, connection, BIND_AUTO_CREATE)
    }

    override fun onPause() {
        super.onPause()
        unbindService(connection)
    }
}