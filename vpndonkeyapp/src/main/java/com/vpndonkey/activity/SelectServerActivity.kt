package com.vpndonkey.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.vpndonkey.adapter.ServerListRecyclerAdapter
import com.vpndonkey.databinding.ActivityServerListBinding
import common.domain.model.Server
import common.domain.usecase.GetServerListUseCase
import common.util.extensions.startActivityIfNetworkIsAvailable
import common.viewmodel.MainViewModel


class SelectServerActivity : AppCompatActivity() {

    private var _binding: ActivityServerListBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<MainViewModel>()

    private val getServerListUseCase: GetServerListUseCase by inject(GetServerListUseCase::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityServerListBinding.inflate(layoutInflater)
        super.setContentView(binding.root)
        initAdapter()
        onClickListenerBack()
    }

    val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedServer = result.data?.getParcelableExtra("selectedServer") as Server?
                startHomeActivity(selectedServer)
            }
        }

    private fun startHomeActivity(selectedServer: Server?) {
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("selectedServer", selectedServer)
        viewModel.stopVpn()
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivityIfNetworkIsAvailable(intent, NoInternetConnectionActivity::class.java)
    }

    private fun initAdapter() = with(binding) {
        val adapter = ServerListRecyclerAdapter(this@SelectServerActivity, getServerListUseCase)
        rcServers.adapter = adapter
        searchView.setOnQueryTextListener(adapter)
    }

    private fun onClickListenerBack() {
        binding.ivButtonBack.setOnClickListener {
            onBackPressed()
        }
    }
}