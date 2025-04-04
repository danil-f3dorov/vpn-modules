package com.vpndonkey.activity

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.vpndonkey.R
import com.vpndonkey.databinding.ActivityLoaderBinding
import core.App
import core.domain.model.Server
import core.util.extensions.startActivityIfNetworkIsAvailable
import core.viewmodel.MainViewModel


class FetchServerListActivity : AppCompatActivity() {

    private var _binding: ActivityLoaderBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (applicationContext as App).appComponent.inject(this)
        mainViewModel =
            (applicationContext as App).appComponent.factory.create(MainViewModel::class.java)
        _binding = ActivityLoaderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        startAnimation()
        mainViewModel.fetchServerList(::retry, ::navigateToActivity)
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
                Intent(this, HomeAppCompatActivity::class.java).apply {
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