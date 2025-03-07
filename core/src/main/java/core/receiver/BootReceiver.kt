package core.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import core.viewmodel.MainViewModel


class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            MainViewModel.initDuntaSDK()
        }
    }
}