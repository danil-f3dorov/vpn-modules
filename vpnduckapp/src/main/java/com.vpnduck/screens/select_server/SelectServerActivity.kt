package com.vpnduck.screens.select_server

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.vpnduck.adapter.ServerListRecyclerAdapter
import com.vpnduck.databinding.ActivityServerListBinding
import com.vpnduck.screens.NoInternetConnectionActivity
import com.vpnduck.screens.home.HomeActivity
import common.viewmodel.HomeViewModel
import common.util.extensions.startActivityIfNetworkIsAvailable
import data.room.entity.Server

class SelectServerActivity : AppCompatActivity() {

    private var _binding: ActivityServerListBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<HomeViewModel>()

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

    private fun initAdapter() {
        val adapter = ServerListRecyclerAdapter(this)
        binding.rcServers.adapter = adapter
        binding.searchView.setOnQueryTextListener(adapter)
    }

    private fun onClickListenerBack() {
        binding.ivButtonBack.setOnClickListener {
            onBackPressed()
        }
    }
}