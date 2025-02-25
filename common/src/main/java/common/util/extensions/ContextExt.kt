package common.util.extensions

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import common.App


fun isNetworkAvailable(): Boolean {
    val connectivityManager =
        App.instance.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo = connectivityManager.activeNetworkInfo
    return networkInfo != null && networkInfo.isConnected
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