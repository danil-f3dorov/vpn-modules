package com.vpnduck.screens

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.vpnduck.databinding.ActivityFailFetchServerBinding
import com.vpnduck.screens.fetch.FetchServerListActivity

class FailFetchServerActivity : AppCompatActivity() {

    private var _binding: ActivityFailFetchServerBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityFailFetchServerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onClickRetry()
    }

    fun onClickRetry() {
        binding.btRetry.setOnClickListener {
            startActivity(Intent(this, FetchServerListActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}