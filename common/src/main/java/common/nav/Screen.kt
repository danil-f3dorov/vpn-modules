package common.nav

import kotlinx.serialization.Serializable

sealed class Screen {
    @Serializable
    data object FetchServer : Screen()

    @Serializable
    data object Home : Screen()

    @Serializable
    data object SelectServer : Screen()

    @Serializable
    data object NoInternet : Screen()

}