package com.vpnduck.activity

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.vpnduck.R
import com.vpnduck.databinding.ActivityLoaderBinding
import common.domain.model.Server
import common.util.extensions.startActivityIfNetworkIsAvailable


class FetchServerListActivity : AppCompatActivity() {

    private var _binding: ActivityLoaderBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModel<FetchServerListViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoaderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        startAnimation()
        viewModel.fetchServerList(this, ::retry, ::navigateToActivity)
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
                println(server)
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