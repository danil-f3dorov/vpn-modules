package common.util.timer

object UpdateServerListTimer {
    private const val TARGET_MINUTES = 10 * 60 * 1000
    private var startTime: Long? = null

    fun startTimer() {
        if(startTime == null) {
            startTime = System.currentTimeMillis()
        }
    }

    fun isTimePassed(): Boolean {
        if(startTime != null) {
            val currentTime = System.currentTimeMillis()
            val isTimePassed =  currentTime - startTime!! >= TARGET_MINUTES
            if(isTimePassed) {
                startTime = null
            }
            return isTimePassed
        }
        else {
            return false
        }
    }
}