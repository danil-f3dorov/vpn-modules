package common.viewmodel

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Base64
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatActivity.BIND_AUTO_CREATE
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.common.R
import common.App
import common.App.Companion.duntaManager
import common.callback.SocketCallbackImpl
import common.data.remote.client.RetrofitClient
import common.domain.model.Server
import common.model.ServerParcelable
import common.util.enum.HomeScreenState
import common.util.extensions.toParcelable
import common.util.parse.ParseSpeed.parseSpeed
import common.util.timer.VpnConnectionTimer
import de.blinkt.openvpn.core.ConfigParser
import de.blinkt.openvpn.core.OpenVPNService
import de.blinkt.openvpn.core.ProfileManager
import de.blinkt.openvpn.core.VPNLaunchHelper
import de.blinkt.openvpn.core.VpnProfile
import de.blinkt.openvpn.core.VpnStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import observers.VpnTrafficObserver
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.lang.ref.WeakReference

const val REQUEST_CODE = 919


class HomeViewModel : ViewModel() {
    private val appContext = App.instance
    val screenStateLiveData = MutableStateFlow(HomeScreenState.Disconnected)
    private var isConnected = false
    var isServiceBind = false
    var vpnService: WeakReference<OpenVPNService>? = null
    var currentServer: Server? = null
    private var vpnProfile: VpnProfile? = null
    var api = RetrofitClient.fetchServerListApi

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

    fun observeStatus() {
        viewModelScope.launch(Dispatchers.Default) {
            observers.VpnStatusObserver.vpnState.collect { vpnState ->
                handleVpnConnectionStatus(vpnState)
            }
        }
    }

    private suspend fun handleVpnConnectionStatus(
        vpnStatus: VpnStatus.ConnectionState,
    ) = withContext(Dispatchers.Main) {
        if (vpnStatus == VpnStatus.ConnectionState.LEVEL_CONNECTED) {
            isConnected = true
            screenStateLiveData.value = HomeScreenState.Connected
        }
    }

    fun startVpn(baseContext: Context) {
        VPNLaunchHelper.startOpenVpn(vpnProfile, baseContext)
    }

    fun bindService(activity: AppCompatActivity) {
        val intent = Intent(activity, OpenVPNService::class.java)
        intent.setAction(OpenVPNService.START_SERVICE)
        isServiceBind = activity.bindService(intent, connection, BIND_AUTO_CREATE)
    }

    fun stopVpn() {
        VpnConnectionTimer.stopTimer()
        ProfileManager.setConntectedVpnProfileDisconnected(appContext)
        vpnService?.get()?.management?.stopVPN(false)
    }

    fun loadVpnProfile(): Boolean {
        return try {
            val data: ByteArray = Base64.decode(currentServer!!.configData, Base64.DEFAULT)
            val cp = ConfigParser()
            val isr = InputStreamReader(ByteArrayInputStream(data))
            cp.parseConfig(isr)
            vpnProfile = cp.convertProfile()
            vpnProfile?.mName = currentServer?.country
            ProfileManager.getInstance(appContext).addProfile(vpnProfile)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun observeTraffic(
        tvDownloadSpeed: TextView, tvUploadSpeed: TextView, homeActivity: AppCompatActivity
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            VpnTrafficObserver.downloadSpeed.combine(VpnTrafficObserver.uploadSpeed) { downloadSpeed, uploadSpeed ->
                Pair(parseSpeed(downloadSpeed), parseSpeed(uploadSpeed))
            }.collect { (parsedDownloadSpeed, parsedUploadSpeed) ->
                withContext(Dispatchers.Main) {
                    tvDownloadSpeed.text = parsedDownloadSpeed
                    tvUploadSpeed.text = parsedUploadSpeed
                    notification(
                        "↑ $parsedDownloadSpeed kb/s ↓ $parsedUploadSpeed kb/s", homeActivity
                    )
                }
            }
        }
    }

    private fun notification(speed: String, homeActivity: AppCompatActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel("1", "notification", NotificationManager.IMPORTANCE_LOW)
            notificationChannel.setSound(null, null)
            val notificationManager = homeActivity.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        var cityName = ""
        if (currentServer?.city != "") {
            cityName = ", ${currentServer?.city}"
        }
        val countryName = "${currentServer?.country}$cityName"

        val intent = Intent(homeActivity, homeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        intent.putExtra(ServerParcelable::class.java.canonicalName, currentServer!!.toParcelable())

        val pendingIntent = PendingIntent.getActivity(
            homeActivity,
            2,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification =
            NotificationCompat.Builder(homeActivity, "1").setSmallIcon(R.drawable.sphere_small)
                .setContentTitle("VPN is connected")
                .setContentText("Connected to $countryName\n$speed").setContentIntent(pendingIntent)
                .build()

        val notificationManagerCompat = NotificationManagerCompat.from(homeActivity)
        if (ActivityCompat.checkSelfPermission(
                homeActivity, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                homeActivity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 2
            )
            return
        }
        notificationManagerCompat.notify(1, notification)
    }

    companion object {
        fun initDuntaSDK() {
            duntaManager.setPartnerId(1)
            duntaManager.setApplicationId(getApplicationId())
            duntaManager.start(App.instance)
        }

        private fun getApplicationId(): Int {
            return when (App.instance.packageName) {
                "com.vpnduck" -> 3
                else -> 4
            }
        }
    }
}