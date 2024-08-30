package common.util.timer

import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import common.util.extensions.checkInternetConnection
import common.util.extensions.isNetworkAvailable

object VpnConnectionTimer {

    private lateinit var activity: AppCompatActivity
    private lateinit var callback: () -> Unit
    private var elapsedTimeInSeconds = 0
    private lateinit var updateUI: (String) -> Unit
    private var isTimerStart = false
    private var timer: CountDownTimer? = null
    private var internetRedirectFlag = true

    fun startTimer(noInternetClazz: Class<out AppCompatActivity>) {
        if (!isTimerStart) {
            timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    elapsedTimeInSeconds++
                    val hours = elapsedTimeInSeconds / 3600
                    val minutes = (elapsedTimeInSeconds % 3600) / 60
                    val sec = elapsedTimeInSeconds % 60
                    val timeString = String.format("%02d:%02d:%02d", hours, minutes, sec)
                    updateUI(timeString)
                    if (!isNetworkAvailable() && internetRedirectFlag) {
                        checkInternetConnection(activity, noInternetClazz)
                        internetRedirectFlag = false
                        callback()
                    }
                }

                override fun onFinish() {}
            }
            timer?.start()
            isTimerStart = true
            internetRedirectFlag = true
        }
    }

    fun setupService(
        updateUI: (String) -> Unit,
        callback: () -> Unit,
        activity: AppCompatActivity
    ) {
        VpnConnectionTimer.updateUI = updateUI
        VpnConnectionTimer.callback = callback
        VpnConnectionTimer.activity = activity
    }


    fun stopTimer() {
        elapsedTimeInSeconds = 0
        timer?.cancel()
        isTimerStart = false
    }

}