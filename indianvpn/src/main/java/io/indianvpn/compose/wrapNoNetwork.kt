package io.indianvpn.compose

import android.content.Context
import core.util.isNetworkAvailable

fun wrapNoNetwork(
    context: Context,
    action: () -> Unit,
    navNoInternet: () -> Unit
) {
    if (isNetworkAvailable(context)) action()
    else navNoInternet()
}