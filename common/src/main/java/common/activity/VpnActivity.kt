package common.activity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.common.R
import common.util.dialog.VpnDisconnectDialog
import common.util.enum.HomeScreenState
import common.util.extensions.startActivityIfNetworkIsAvailable
import common.util.parse.ParseFlag
import common.util.timer.UpdateServerListTimer
import common.util.timer.VpnConnectionTimer
import common.util.validate.ValidateUtil
import common.viewmodel.HomeViewModel
import data.room.entity.Server

abstract class VpnActivity : AppCompatActivity() {

    abstract val vm: HomeViewModel
    private var isConnected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCurrentServer()
    }

    private fun startVpn() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            requestPermissionLauncher.launch(intent)
        } else {
            vm.startVpn(baseContext)
            vm.screenStateLiveData.value = HomeScreenState.Connecting
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                vm.screenStateLiveData.value = HomeScreenState.Connecting
                vm.startVpn(baseContext)
            } else {
                startVpn()
            }
        }

    fun updateUiCurrentServer(
        tvCountryName: TextView,
        tvIpAddress: TextView,
        ivCountryIcon: ImageView

    ) {
        val srv = vm.currentServer
        tvCountryName.text = ValidateUtil.validateIfCityExist(
            srv?.country ?: "Unknown", srv?.city ?: ""
        )
        tvIpAddress.text = srv?.ip ?: "000.000.000.000"
        ivCountryIcon.setImageDrawable(
            AppCompatResources.getDrawable(
                this, ParseFlag.findFlagForServer(srv)
            )
        )
    }

    private fun setCurrentServer() {
        val extraServer = intent.getParcelableExtra<Server>(Server::class.java.canonicalName)
        if (extraServer != null) {
            vm.currentServer = extraServer
        }
    }

    fun observeScreenStateFlow(
        tvDownloadSpeed: TextView,
        tvUploadSpeed: TextView,
        ivButtonBackground: ImageView,
        ibConnect: ImageButton,
        layoutId: Int,
        cancelButtonId: Int,
        disconnectButtonId: Int,
        ibDisconnect: Int,
        tvStatusInfo: TextView,
        ibConnectId: Int
    ) {
        vm.screenStateLiveData.observe(this) { state ->
            when (state) {
                HomeScreenState.Connected -> updateUiConnected(
                    layoutId = layoutId,
                    disconnectButtonId = disconnectButtonId,
                    cancelButtonId = cancelButtonId,
                    ivButtonBackground = ivButtonBackground,
                    ibConnect = ibConnect,
                    tvDownloadSpeed = tvDownloadSpeed,
                    tvUploadSpeed = tvUploadSpeed,
                    ibDisconnect = ibDisconnect
                )

                HomeScreenState.Disconnected -> updateUiDisconnected(
                    ivButtonBackground = ivButtonBackground,
                    ibConnect = ibConnect,
                    tvDownloadSpeed = tvDownloadSpeed,
                    tvUploadSpeed = tvUploadSpeed,
                    tvStatusInfo = tvStatusInfo,
                    ibConnectId = ibConnectId
                )

                HomeScreenState.Connecting -> updateUiConnecting(
                    tvStatusInfo = tvStatusInfo,
                    ivButtonBackground = ivButtonBackground,
                    ibConnect = ibConnect
                )

                null -> {}
            }
        }
    }

    private fun updateUiDisconnected(
        ivButtonBackground: ImageView,
        ibConnect: ImageButton,
        tvStatusInfo: TextView,
        tvDownloadSpeed: TextView,
        tvUploadSpeed: TextView,
        ibConnectId: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(android.app.NotificationManager::class.java).deleteNotificationChannel(
                "1"
            )
        }
        ivButtonBackground.clearAnimation()
        isConnected = false
        ibConnect.visibility = View.VISIBLE
        ibConnect.setImageDrawable(
            AppCompatResources.getDrawable(
                this@VpnActivity, ibConnectId
            )
        )
        tvStatusInfo.text = "Click to Connect"
        tvDownloadSpeed.text = "--"
        tvUploadSpeed.text = "--"
        onClickStartConnect(
            ibConnect = ibConnect,
            ivButtonBackground = ivButtonBackground
        )
    }

    private fun updateUiConnecting(
        ivButtonBackground: ImageView,
        tvStatusInfo: TextView,
        ibConnect: ImageButton
    ) {
        val rotateAnimation = AnimationUtils.loadAnimation(this@VpnActivity, R.anim.rotate_anim)
        ivButtonBackground.startAnimation(rotateAnimation)
        tvStatusInfo.text = "Connecting..."
        ibConnect.visibility = View.GONE
    }

    private fun updateUiConnected(
        tvDownloadSpeed: TextView,
        tvUploadSpeed: TextView,
        ivButtonBackground: ImageView,
        ibConnect: ImageButton,
        layoutId: Int,
        cancelButtonId: Int,
        disconnectButtonId: Int,
        ibDisconnect: Int
    ) {
        vm.observeTraffic(tvDownloadSpeed, tvUploadSpeed, this@VpnActivity)
        ivButtonBackground.clearAnimation()
        ibConnect.setImageDrawable(
            AppCompatResources.getDrawable(this@VpnActivity, ibDisconnect)
        )
        ibConnect.visibility = View.VISIBLE
        onClickStopConnect(
            layoutId = layoutId,
            disconnectButtonId = disconnectButtonId,
            cancelButtonId = cancelButtonId,
            ivButtonBackground = ivButtonBackground,
            ibConnect = ibConnect
        )
    }

    private fun onClickStartConnect(
        ivButtonBackground: ImageView,
        ibConnect: ImageButton
    ) {
        ivButtonBackground.setOnClickListener(startConnectClickListener())
        ibConnect.setOnClickListener(startConnectClickListener())
    }

    private fun onClickStopConnect(
        ivButtonBackground: ImageView,
        ibConnect: ImageButton,
        layoutId: Int,
        cancelButtonId: Int,
        disconnectButtonId: Int
    ) {
        ivButtonBackground.setOnClickListener(
            stopConnectClickListener(
                layoutId = layoutId,
                disconnectButtonId = disconnectButtonId,
                cancelButtonId = cancelButtonId

            )
        )
        ibConnect.setOnClickListener(
            stopConnectClickListener(
                layoutId = layoutId,
                disconnectButtonId = disconnectButtonId,
                cancelButtonId = cancelButtonId
            )
        )
    }

    private fun stopConnectClickListener(
        layoutId: Int,
        cancelButtonId: Int,
        disconnectButtonId: Int
    ) = View.OnClickListener {
        VpnDisconnectDialog(
            context = this,
            layoutId = layoutId,
            disconnectButtonId = disconnectButtonId,
            cancelButtonId = cancelButtonId
        ) {
            vm.screenStateLiveData.value = HomeScreenState.Disconnected
            VpnConnectionTimer.stopTimer()
            vm.stopVpn()
        }.show()
    }

    private fun startConnectClickListener() = View.OnClickListener {
        if (vm.loadVpnProfile()) {
            startVpn()
            it.setOnClickListener(null)
        }
    }

    fun serversOnClickListener(
        placeholder1: View,
        layoutId: Int,
        cancelButtonId: Int,
        disconnectButtonId: Int,
        fetchServerListActivityClass: Class<out AppCompatActivity>,
        selectServerActivity: Class<out AppCompatActivity>,
        noInternetConnectionActivity: Class<out AppCompatActivity>,
    ) {
        placeholder1.setOnClickListener {
            when (vm.screenStateLiveData.value) {
                HomeScreenState.Connected -> {
                    VpnDisconnectDialog(
                        this@VpnActivity,
                        layoutId = layoutId,
                        cancelButtonId = cancelButtonId,
                        disconnectButtonId = disconnectButtonId
                    ) {
                        vm.screenStateLiveData.value = HomeScreenState.Disconnected
                        VpnConnectionTimer.stopTimer()
                        vm.stopVpn()
                        startSelectServerActivity(
                            fetchServerListActivityClass = fetchServerListActivityClass,
                            selectServerActivity = selectServerActivity,
                            noInternetConnectionActivity = noInternetConnectionActivity
                        )
                    }.show()
                }

                HomeScreenState.Disconnected -> {
                    startSelectServerActivity(
                        fetchServerListActivityClass = fetchServerListActivityClass,
                        selectServerActivity = selectServerActivity,
                        noInternetConnectionActivity = noInternetConnectionActivity
                    )
                }

                HomeScreenState.Connecting -> {
                    Unit
                }

                null -> {}
            }
        }
    }

    private fun startSelectServerActivity(
        fetchServerListActivityClass: Class<out AppCompatActivity>,
        selectServerActivity: Class<out AppCompatActivity>,
        noInternetConnectionActivity: Class<out AppCompatActivity>
    ) {
        val intent: Intent?
        if (UpdateServerListTimer.isTimePassed()) {
            intent = Intent(this, fetchServerListActivityClass)
            intent.putExtra("homeCall", true)
        } else {
            intent = Intent(this, selectServerActivity)
        }
        startActivityIfNetworkIsAvailable(intent, noInternetConnectionActivity)
    }

    override fun onResume() {
        super.onResume()
        vm.bindService(this)
    }

    override fun onPause() {
        if (vm.isServiceBind) {
            vm.isServiceBind = false
            unbindService(vm.connection)
        }
        super.onPause()
    }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager::class.java).deleteNotificationChannel("channel_id")
        }
    }

    override fun onStop() {
        super.onStop()
        notificationChannel()
    }

    private fun notificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "channel_id"
            val channelName = "Channel Name"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance)
            val notificationManager = this.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

}