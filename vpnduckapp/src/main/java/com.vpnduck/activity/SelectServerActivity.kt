package com.vpnduck.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.vpnduck.adapter.ServerListRecyclerAdapter
import com.vpnduck.databinding.ActivityServerListBinding
import core.App
import core.domain.model.Server
import core.domain.usecase.GetServerListUseCase
import core.util.extensions.startActivityIfNetworkIsAvailable


class SelectServerActivity : AppCompatActivity() {
    private var _binding: ActivityServerListBinding? = null
    private val binding get() = _binding!!

    private lateinit var getServerListUseCase: GetServerListUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (applicationContext as App).appComponent.inject(this)
        getServerListUseCase = (applicationContext as App).appComponent.getServerListUseCase()
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
        val intent = Intent(this, HomeAppCompatActivity::class.java)
        intent.putExtra("selectedServer", selectedServer)
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivityIfNetworkIsAvailable(intent, NoInternetConnectionActivity::class.java)
    }

    private fun initAdapter() {
        val adapter = ServerListRecyclerAdapter(this, getServerListUseCase)
        binding.rcServers.adapter = adapter
        binding.searchView.setOnQueryTextListener(adapter)
    }

    private fun onClickListenerBack() {
        binding.ivButtonBack.setOnClickListener {
            onBackPressed()
        }
    }
}