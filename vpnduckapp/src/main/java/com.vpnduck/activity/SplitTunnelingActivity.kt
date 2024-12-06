package com.vpnduck.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.vpnduck.compose.SplitTunnelingScreen
import common.viewmodel.SplitTunnelingVIewModel

class SplitTunnelingActivity : AppCompatActivity() {

    private val viewModel by viewModels<SplitTunnelingVIewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SplitTunnelingScreen(
                vIewModel = viewModel
            ) {
                onBackPressed()
            }
        }
    }
}