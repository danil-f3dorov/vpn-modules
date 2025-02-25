package io.indianvpn.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.indianvpn.ui.theme.labelLarge

@Composable
fun FetchServerScreen(
    modifier: Modifier = Modifier,
    fetchServerList: (navHome: () -> Unit) -> Unit,
    navigateHome: () -> Unit
) {
    LaunchedEffect(Unit) {
        fetchServerList { navigateHome() }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "IndianVPN",
            style = labelLarge
        )
    }
}