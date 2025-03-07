package core.nav

import androidx.compose.runtime.Composable

data class NavigationScreens(
    val fetchServerScreen: @Composable (FetchServerScreenParams) -> Unit,
    val homeScreen: @Composable (HomeScreenParams) -> Unit,
    val selectServerScreen: @Composable (SelectServerScreenParams) -> Unit,
    val noInternetScreen: @Composable (NoInternetScreenParams) -> Unit
)