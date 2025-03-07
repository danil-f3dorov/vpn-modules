package core.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.core.R
import core.App

class NotificationReceiver : BroadcastReceiver() {
    val debug = "${App.instance.packageName}.activity.FetchServerListActivity"
    override fun onReceive(context: Context, intent: Intent) {
        val homeIntent = Intent().setClassName(
            context,
            "${App.instance.packageName}.activity.FetchServerListActivity"
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            12,
            homeIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, "channel_id")
            .setContentTitle("WARNING!")
            .setContentText("Protect your data connect to one of our VPN servers!")
            .setSmallIcon(R.drawable.sphere)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(12, notification)
    }
}
