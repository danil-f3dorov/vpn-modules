package common.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import common.R

class NotificationReceiver(private val homeClazz: Class<out AppCompatActivity>) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val homeIntent =
            Intent(context, homeClazz).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

        val pendingIntent = PendingIntent.getActivity(
            context,
            12,
            homeIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, "channel_id")
            .setContentTitle("WARNING!")
            .setContentText("Protect your data connect to one of our VPN servers!")
            .setSmallIcon(R.drawable.icon)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(12, notification)
    }
}