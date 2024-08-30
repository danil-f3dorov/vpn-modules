package common.util.extensions

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import common.App


private val connectivityManager =
    App.instance.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

fun isNetworkAvailable(): Boolean {
    val networkInfo = connectivityManager.activeNetworkInfo
    return networkInfo != null && networkInfo.isConnected
}

fun checkInternetConnection(context: Context, clazzNoInternet: Class<out AppCompatActivity>): Boolean {
    if (!isNetworkAvailable()) {
        val newIntent = Intent(context, clazzNoInternet)
        newIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        newIntent.putExtra("callingClassName", context.javaClass.simpleName)
        context.startActivity(newIntent)
        return false
    }
    return true
}

fun Context.startActivityIfNetworkIsAvailable(intent: Intent, clazzNoInternet: Class<out AppCompatActivity>) {
    if (!isNetworkAvailable()) {
        val newIntent = Intent(this, clazzNoInternet)
        newIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        newIntent.putExtra("callingClassName", this.javaClass.simpleName)
        startActivity(newIntent)
    } else {
        startActivity(intent)
    }
}