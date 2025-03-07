package core.util.enum

sealed class ScreenState {
    data object Connected: ScreenState()
    data object Disconnected: ScreenState()
    data object Connecting: ScreenState()
}