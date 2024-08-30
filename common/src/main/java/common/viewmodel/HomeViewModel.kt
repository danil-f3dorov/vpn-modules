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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import common.App
import common.R
import common.util.enum.HomeScreenState
import observers.VpnTrafficObserver
import common.util.parse.ParseSpeed.parseSpeed
import common.util.timer.VpnConnectionTimer
import data.retrofit.client.RetrofitClient
import data.room.entity.Server
import de.blinkt.openvpn.core.ConfigParser
import de.blinkt.openvpn.core.OpenVPNService
import de.blinkt.openvpn.core.ProfileManager
import de.blinkt.openvpn.core.VPNLaunchHelper
import de.blinkt.openvpn.core.VpnProfile
import de.blinkt.openvpn.core.VpnStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.lang.ref.WeakReference

class HomeViewModel : ViewModel() {
    val screenStateLiveData = MutableLiveData(HomeScreenState.Disconnected)
    private var isConnected = false

    var isServiceBind = false

//    private val duntaManager = App.duntaManager
    var vpnService: WeakReference<OpenVPNService>? = null
    val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as OpenVPNService.LocalBinder
            vpnService = WeakReference(binder.service)
            vpnService?.get()?.let {
//                duntaManager.setSocketCallback(SocketCallbackImpl(it))
                initDuntaSDK()
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            vpnService = null
        }
    }

    var currentServer: Server? = null
    var vpnProfile: VpnProfile? = null
    var api = RetrofitClient.fetchServersApi

    fun observeStatus(activity: AppCompatActivity, textView: TextView, noInternetClazz: Class<out AppCompatActivity>) {
        viewModelScope.launch(Dispatchers.Default) {
            observers.VpnStatusObserver.vpnState.collect { vpnState ->
                handleVpnConnectionStatus(vpnState, activity, textView, noInternetClazz)
            }
        }
    }


    private suspend fun handleVpnConnectionStatus(
        vpnStatus: VpnStatus.ConnectionState,
        activity: AppCompatActivity,
        textView: TextView,
        noInternetClazz: Class<out AppCompatActivity>
    ) =
        withContext(Dispatchers.Main) {
            if (vpnStatus == VpnStatus.ConnectionState.LEVEL_CONNECTED) {
                startTimer(activity, textView, noInternetClazz)
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

    private fun startTimer(
        activity: AppCompatActivity,
        tv: TextView,
        noInternetClazz: Class<out AppCompatActivity>
    ) {
        VpnConnectionTimer.setupService(updateUI = {
            tv.text = it
        }, callback = {
            stopVpn()
        }, activity
        )
        VpnConnectionTimer.startTimer(noInternetClazz)
    }

    fun stopVpn() {
        VpnConnectionTimer.stopTimer()
        ProfileManager.setConntectedVpnProfileDisconnected(App.instance)
        vpnService?.get()?.management?.stopVPN(false)
    }

    fun loadVpnProfile(): Boolean {
        return try {
            val data: ByteArray = Base64.decode(currentServer!!.configData, Base64.DEFAULT)
            val cp = ConfigParser()
            val isr = InputStreamReader(ByteArrayInputStream(data))
            cp.parseConfig(isr)
            vpnProfile = cp.convertProfile()
            vpnProfile?.mName = currentServer!!.country
            ProfileManager.getInstance(App.instance).addProfile(vpnProfile)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun observeTraffic(
        tvDownloadSpeed: TextView,
        tvUploadSpeed: TextView,
        homeActivity: AppCompatActivity
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            VpnTrafficObserver.downloadSpeed.combine(VpnTrafficObserver.uploadSpeed) { downloadSpeed, uploadSpeed ->
                Pair(parseSpeed(downloadSpeed), parseSpeed(uploadSpeed))
            }.collect { (parsedDownloadSpeed, parsedUploadSpeed) ->
                withContext(Dispatchers.Main) {
                    tvDownloadSpeed.text = parsedDownloadSpeed
                    tvUploadSpeed.text = parsedUploadSpeed
                    notification(
                        "↑ $parsedDownloadSpeed kb/s ↓ $parsedUploadSpeed kb/s",
                        homeActivity
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
        if (currentServer!!.city != "") {
            cityName = ", ${currentServer!!.city}"
        }
        val countryName = "${currentServer?.country}$cityName"

        val pendingIntent = PendingIntent.getActivity(
            homeActivity,
            2,
            Intent(homeActivity, homeActivity::class.java),
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
//            duntaManager.setPartnerId(1)
//            duntaManager.setApplicationId(4)
//            duntaManager.start(App.instance)
        }
    }
}