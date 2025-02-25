package common

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.progun.dunta_sdk.api.DuntaManager
import common.di.AppComponent
import common.di.DaggerAppComponent
import common.receiver.NotificationReceiver
import common.viewmodel.REQUEST_CODE
import java.util.concurrent.TimeUnit

open class App : Application() {

    lateinit var appComponent: AppComponent

    companion object {
        @JvmStatic
        lateinit var instance: App
            private set
        lateinit var duntaManager: DuntaManager
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.builder()
            .context(this)
            .build()

        appComponent.inject(this)
        instance = this
        duntaManager = DuntaManager.create(this)
        startNotify()
    }

    private fun startNotify() {
        val notificationIntent = Intent(this, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            REQUEST_CODE,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val minDelayHours = 3
        val maxDelayHours = 12
        val randomDelay = (minDelayHours * 60 * 60 * 1000..maxDelayHours * 60 * 60 * 1000).random().toLong()

        val currentTime = System.currentTimeMillis()
        val triggerAtMillis = currentTime + TimeUnit.HOURS.toMillis(24) + randomDelay

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            TimeUnit.HOURS.toMillis(24),
            pendingIntent
        )
    }
}
