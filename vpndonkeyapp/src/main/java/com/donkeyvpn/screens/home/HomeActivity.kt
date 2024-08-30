package com.donkeyvpn.screens.home

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.donkeyvpn.R
import com.donkeyvpn.databinding.ActivityHomeBinding
import com.donkeyvpn.screens.NoInternetConnectionActivity
import com.donkeyvpn.screens.fetch.FetchServerListActivity
import com.donkeyvpn.screens.select_server.SelectServerActivity
import com.donkeyvpn.util.dialog.VpnDisconnectDialog
import common.util.extensions.checkInternetConnection
import common.util.extensions.startActivityIfNetworkIsAvailable
import common.util.parse.ParseFlag
import common.util.timer.UpdateServerListTimer
import common.util.enum.HomeScreenState
import common.util.timer.VpnConnectionTimer
import common.util.validate.ValidateUtil
import common.viewmodel.HomeViewModel
import data.room.entity.Server
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class HomeActivity : AppCompatActivity() {

    private var isConnected = false
    private var _binding: ActivityHomeBinding? = null
    private val binding get() = _binding!!
    private val connectionTimeoutTimer = ConnectionTimeoutTimer()
    private val vm by viewModels<HomeViewModel>()

    private val stopConnectClickListener = View.OnClickListener {
        VpnDisconnectDialog(this) {
            vm.screenStateLiveData.value = HomeScreenState.Disconnected
            VpnConnectionTimer.stopTimer()
            vm.stopVpn()
        }.show()
    }

    private val startConnectClickListener = View.OnClickListener {
        if (vm.loadVpnProfile()) {
            startVpn()
            it.setOnClickListener(null)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 2)
        super.onCreate(savedInstanceState)
        UpdateServerListTimer.startTimer()
        _binding = ActivityHomeBinding.inflate(layoutInflater)
        updateUiSetServerList()
        setContentView(binding.root)
        serversOnClickListener()
        observeScreenStateFlow()
        vm.observeStatus(this, binding.tvStatusInfo, NoInternetConnectionActivity::class.java)
    }

    private fun observeScreenStateFlow() {
        vm.screenStateLiveData.observe(this) { state ->
            when (state) {
                HomeScreenState.Connected -> updateUiConnected()
                HomeScreenState.Disconnected -> updateUiDisconnected()
                HomeScreenState.Connecting -> updateUiConnecting()
                null -> {}
            }
        }
    }

    private fun updateUiDisconnected() = with(binding) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager::class.java).deleteNotificationChannel("1")
        }
        ConnectionTimeoutTimer().stopTimer()
        binding.ivButtonBackground.clearAnimation()
        isConnected = false
        ibConnect.visibility = View.VISIBLE
        ibConnect.setImageDrawable(
            AppCompatResources.getDrawable(
                this@HomeActivity, R.drawable.ib_connect
            )
        )
        binding.tvStatusInfo.text = "Click to Connect"
        tvDownloadSpeed.text = "--"
        tvUploadSpeed.text = "--"
        onClickStartConnect()
    }

    private fun updateUiConnecting() = with(binding) {
        if (!checkInternetConnection(this@HomeActivity, NoInternetConnectionActivity::class.java)) {
            return@with
        }
        val rotateAnimation = AnimationUtils.loadAnimation(this@HomeActivity, R.anim.rotate_anim)
        binding.ivButtonBackground.startAnimation(rotateAnimation)
        tvStatusInfo.text = "Connecting..."
        ibConnect.visibility = View.GONE
        connectionTimeoutTimer.startTimer()
    }

    private fun updateUiConnected() = with(binding) {
        vm.observeTraffic(binding.tvDownloadSpeed, binding.tvUploadSpeed, this@HomeActivity)
        ivButtonBackground.clearAnimation()
        ibConnect.setImageDrawable(
            AppCompatResources.getDrawable(this@HomeActivity, R.drawable.ib_disconnect)
        )
        ibConnect.visibility = View.VISIBLE
        onClickStopConnect()
    }


    private fun onClickStartConnect() = with(binding) {
        checkInternetConnection(this@HomeActivity, NoInternetConnectionActivity::class.java)
        ivButtonBackground.setOnClickListener(startConnectClickListener)
        ibConnect.setOnClickListener(startConnectClickListener)
    }

    private fun onClickStopConnect() = with(binding) {
        ivButtonBackground.setOnClickListener(stopConnectClickListener)
        ibConnect.setOnClickListener(stopConnectClickListener)
    }

    private fun serversOnClickListener() {
        binding.placeholder1.setOnClickListener {
            when (vm.screenStateLiveData.value) {
                HomeScreenState.Connected -> {
                    VpnDisconnectDialog(this) {
                        vm.screenStateLiveData.value = HomeScreenState.Disconnected
                        VpnConnectionTimer.stopTimer()
                        vm.stopVpn()
                        startSelectServerActivity()
                    }.show()
                }

                HomeScreenState.Disconnected -> {
                    startSelectServerActivity()
                }

                HomeScreenState.Connecting -> {
                    Unit
                }

                null -> {}
            }
        }
    }

    private fun updateUiSetServerList() = with(binding) {
        val extraServer = intent.getParcelableExtra<Server>(Server::class.java.canonicalName)
        vm.currentServer = extraServer
        vm.stopVpn()
        vm.screenStateLiveData.value = HomeScreenState.Disconnected
        tvCountryName.text = ValidateUtil.validateIfCityExist(
            vm.currentServer!!.country, vm.currentServer!!.city
        )
        tvIpAddress.text = vm.currentServer!!.ip
        ivCountryIcon.setImageDrawable(
            AppCompatResources.getDrawable(
                this@HomeActivity, ParseFlag.findFlagForServer(vm.currentServer!!)
            )
        )

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
            if (result.resultCode == Activity.RESULT_OK) {
                vm.screenStateLiveData.value = HomeScreenState.Connecting
                vm.startVpn(baseContext)
            } else {
                startVpn()
            }
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

    private fun startSelectServerActivity() {
        val intent: Intent?
        if (UpdateServerListTimer.isTimePassed()) {
            intent = Intent(this, FetchServerListActivity::class.java)
            intent.putExtra("homeCall", true)
        } else {
            intent = Intent(this, SelectServerActivity::class.java)
        }
        startActivityIfNetworkIsAvailable(intent, NoInternetConnectionActivity::class.java)
    }

    inner class ConnectionTimeoutTimer {

        private var job: Job? = null
        private var internetRedirectFlag = true

        fun startTimer() {
            job = lifecycleScope.launch(Dispatchers.IO) {
                val result = withTimeoutOrNull(30_000) {
                    while (!isConnected) {
                        delay(1000)
                    }
                    true
                }

                withContext(Dispatchers.Main) {
                    if (result == null) { // timeout happened
                        vm.screenStateLiveData.value = HomeScreenState.Disconnected
                        Toast.makeText(
                            this@HomeActivity,
                            "This server is unavailable \n try another server",
                            Toast.LENGTH_SHORT
                        ).show()

                        if (internetRedirectFlag) {
                            checkInternetConnection(this@HomeActivity, NoInternetConnectionActivity::class.java)
                            internetRedirectFlag = false
                        }
                        internetRedirectFlag = true
                    }
                }
            }
        }

        fun stopTimer() {
            job?.cancel()
        }
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

