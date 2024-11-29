package common.util.timer

import android.os.CountDownTimer

class VpnConnectionTimer {

    private var elapsedTimeInSeconds = 0
    private var isTimerStart = false
    private var timer: CountDownTimer? = null

    fun startTimer(updateUI: (String) -> Unit) {
        if (!isTimerStart) {
            timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    elapsedTimeInSeconds++
                    val hours = elapsedTimeInSeconds / 3600
                    val minutes = (elapsedTimeInSeconds % 3600) / 60
                    val sec = elapsedTimeInSeconds % 60
                    val timeString = String.format("%02d:%02d:%02d", hours, minutes, sec)
                    updateUI(timeString)
                }

                override fun onFinish() {}
            }
            timer?.start()
            isTimerStart = true
        }
    }

    fun stopTimer() {
        elapsedTimeInSeconds = 0
        timer?.cancel()
        isTimerStart = false
    }

}