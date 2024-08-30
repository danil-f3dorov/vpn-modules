package com.donkeyvpn.screens.select_server

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.donkeyvpn.adapter.ServerListRecyclerAdapter
import com.donkeyvpn.databinding.ActivityServerListBinding
import com.donkeyvpn.screens.NoInternetConnectionActivity
import com.donkeyvpn.screens.home.HomeActivity
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

    private fun initAdapter() = with(binding) {
        val adapter = ServerListRecyclerAdapter(this@SelectServerActivity)
        rcServers.adapter = adapter
        searchView.setOnQueryTextListener(adapter)
    }

    private fun onClickListenerBack() {
        binding.ivButtonBack.setOnClickListener {
            onBackPressed()
        }
    }
}