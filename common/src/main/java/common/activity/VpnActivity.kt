package common.activity

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
import common.App
import common.App.Companion.duntaManager
import common.callback.SocketCallbackImpl
import common.domain.model.Server
import common.util.enum.HomeScreenState
import common.viewmodel.MainViewModel
import common.viewmodel.MainViewModel.Companion.initDuntaSDK
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
import java.lang.ref.WeakReference

open class VpnActivity : ComponentActivity() {
    protected lateinit var mainViewModel: MainViewModel

    private var isServiceBind = false
    private var vpnService: WeakReference<OpenVPNService>? = null
    private var vpnProfile: VpnProfile? = null
    private var job: Job? = null

    private var isVpnConnecting = false

    val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as OpenVPNService.LocalBinder
            vpnService = WeakReference(binder.service)
            vpnService?.get()?.let {
                duntaManager.setSocketCallback(SocketCallbackImpl(it))
                initDuntaSDK()
            }

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

    protected val requestPermissionLauncher =
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
        mainViewModel.screenState.value = HomeScreenState.Connecting
        job = lifecycleScope.launch(Dispatchers.Main) {
            delay(15_000)
            if(mainViewModel.screenState.value != HomeScreenState.Connected && isVpnConnecting) {
                Toast.makeText(this@VpnActivity, "This server is unavailable", Toast.LENGTH_SHORT).show()
                mainViewModel.screenState.value = HomeScreenState.Disconnected
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
        vpnService?.get()?.management?.stopVPN(false)
    }


    override fun onResume() {
        super.onResume()
        val intent = Intent(this, OpenVPNService::class.java)
        intent.setAction(OpenVPNService.START_SERVICE)
        isServiceBind
            this.bindService(intent, connection, BIND_AUTO_CREATE)
    }

    override fun onPause() {
        if (isServiceBind) {
            isServiceBind = false
            unbindService(connection)
        }
        super.onPause()
    }
}