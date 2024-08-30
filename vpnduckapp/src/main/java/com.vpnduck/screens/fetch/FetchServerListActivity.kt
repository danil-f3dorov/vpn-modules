package com.vpnduck.screens.fetch

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.vpnduck.R
import com.vpnduck.databinding.ActivityLoaderBinding
import com.vpnduck.screens.FailFetchServerActivity
import com.vpnduck.screens.NoInternetConnectionActivity
import com.vpnduck.screens.home.HomeActivity
import com.vpnduck.screens.select_server.SelectServerActivity
import common.viewmodel.FetchServerListViewModel
import common.util.extensions.startActivityIfNetworkIsAvailable
import data.room.entity.Server


class FetchServerListActivity : AppCompatActivity() {

    private var _binding: ActivityLoaderBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<FetchServerListViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoaderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        startAnimation()
        viewModel.fetchServerList(::retry, ::navigateToActivity)
    }

    private fun retry() {
        startActivityIfNetworkIsAvailable(Intent(this, FailFetchServerActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }, NoInternetConnectionActivity::class.java)
    }

    private fun navigateToActivity(server: Server) {
        val intent: Intent =
            if (intent.hasExtra("homeCall")) {
                Intent(this, SelectServerActivity::class.java)
            } else {
                Intent(this, HomeActivity::class.java).apply {
                    putExtra(Server::class.java.canonicalName, server)
                }
            }
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivityIfNetworkIsAvailable(intent, NoInternetConnectionActivity::class.java)
    }

    private fun startAnimation() {
        val rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_anim)
        binding.animationImage.startAnimation(rotateAnimation)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}