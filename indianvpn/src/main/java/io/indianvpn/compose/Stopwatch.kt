package io.indianvpn.compose

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import io.indianvpn.ui.theme.Gilroy
import io.indianvpn.ui.theme.color1
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun Stopwatch() {
    var elapsedTime by remember { mutableLongStateOf(0L) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            while (isActive) {
                delay(1000L)
                elapsedTime++
            }
        }
    }

    val formattedTime = formatTime(elapsedTime)

    Text(
        text = formattedTime,
        fontFamily = Gilroy.heavy,
        fontSize = 44.sp,
        color = color1
    )
}

fun formatTime(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, secs)
}

@Preview(showBackground = true)
@Composable
private fun StopwatchPreview() {
    Stopwatch()
}