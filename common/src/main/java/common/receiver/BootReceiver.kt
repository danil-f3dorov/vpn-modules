package common.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import common.viewmodel.HomeViewModel


class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            HomeViewModel.initDuntaSDK()
        }
    }
}