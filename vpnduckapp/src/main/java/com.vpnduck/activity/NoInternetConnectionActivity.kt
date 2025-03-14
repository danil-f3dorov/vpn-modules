package com.vpnduck.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.vpnduck.databinding.ActivityNoInternetConnectionBinding
import core.util.isNetworkAvailable

class NoInternetConnectionActivity : AppCompatActivity() {

    private var _binding: ActivityNoInternetConnectionBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityNoInternetConnectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onClickRefresh()
    }

    private fun onClickRefresh() {
        binding.btRefresh.setOnClickListener {
            val intentClassName = intent.getStringExtra("callingClassName")
            val intent: Intent? = getIntentForClass(intentClassName)
            if (isNetworkAvailable(this)) {
                intent?.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
    }

    private fun getIntentForClass(className: String?): Intent? {
        return when (className) {
            "HomeActivity" -> Intent(this, HomeAppCompatActivity::class.java)
            "FetchServerListActivity" -> Intent(this, FetchServerListActivity::class.java)
            else -> null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}