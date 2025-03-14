package com.vpndonkey.activity

import android.Manifest
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import com.vpndonkey.R
import com.vpndonkey.databinding.ActivityHomeBinding
import common.viewmodel.HomeViewModel
import core.activity.VpnAppCompatActivity
import core.util.timer.UpdateServerListTimer


class HomeAppCompatActivity : VpnAppCompatActivity() {

    private var _binding: ActivityHomeBinding? = null
    private val binding get() = _binding!!
    override val vm: HomeViewModel by viewModels<HomeViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 2)
        super.onCreate(savedInstanceState)
        UpdateServerListTimer.startTimer()
        _binding = ActivityHomeBinding.inflate(layoutInflater)
        updateUiCurrentServer(
            tvCountryName = binding.tvCountryName,
            tvIpAddress = binding.tvIpAddress,
            ivCountryIcon = binding.ivCountryIcon
        )
        setContentView(binding.root)
        observeScreenStateFlow(
            layoutId = R.layout.vpn_disconect_dialog,
            disconnectButtonId = R.id.btDisconnect,
            cancelButtonId = R.id.btCancel,
            tvStatusInfo = binding.tvStatusInfo,
            tvDownloadSpeed = binding.tvDownloadSpeed,
            tvUploadSpeed = binding.tvUploadSpeed,
            ivButtonBackground = binding.ivButtonBackground,
            ibConnect = binding.ibConnect,
            ibDisconnect = R.drawable.ib_disconnect,
            ibConnectId = R.drawable.ib_connect

        )
        serversOnClickListener(
            placeholder1 = binding.placeholder1,
            layoutId = R.layout.vpn_disconect_dialog,
            cancelButtonId = R.id.btCancel,
            disconnectButtonId = R.id.btDisconnect,
            fetchServerListActivityClass = FetchServerListActivity::class.java,
            noInternetConnectionActivity = NoInternetConnectionActivity::class.java,
            selectServerActivity = SelectServerActivity::class.java
        )
        vm.observeStatus()
    }
}

