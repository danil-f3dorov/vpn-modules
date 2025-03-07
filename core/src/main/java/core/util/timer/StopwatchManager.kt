package core.util.timer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object StopwatchManager {
    private var _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime.asStateFlow()

    private var job: Job? = null

    fun start() {
        if (job?.isActive == true) return

        job = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                delay(1000L)
                _elapsedTime.value += 1
            }
        }
    }

    fun stop() {
        _elapsedTime.value = 0L
        job?.cancel()
        job = null
    }
}
