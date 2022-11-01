package com.techtown.weatherwidget


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity


class AlarmReceiver : BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context?, intent: Intent?) {
        var wakeLock: PowerManager.WakeLock? = null

        if (wakeLock != null) {
            return
        }


        // 알림채널 생성
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Alarm"
            val descriptionText = "Alarm details"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel("AlarmId", name, importance)
            mChannel.description = descriptionText
            val notificationManager =
                context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }


        val fullscreenIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val fullscreenPendingIntent = PendingIntent.getActivity(context,
            0,
            fullscreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT)


        val mBuilder = NotificationCompat.Builder(context!!, "AlarmId")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Alarm")
            .setContentText(WeaVariable.alarmMsg)
            /*
            .setStyle(
                NotificationCompat.InboxStyle()
                    .addLine("")
                    .addLine("dadasd")
                    .addLine("sas")
                    .addLine("as")
                    .addLine("as")
            )
            */
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(fullscreenPendingIntent)
            .setFullScreenIntent(fullscreenPendingIntent, true)

        val am = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val id = System.currentTimeMillis() / 1000
        am.notify(id.toInt(), mBuilder.build())
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
                    PowerManager.ACQUIRE_CAUSES_WAKEUP or
                    PowerManager.ON_AFTER_RELEASE, "app:alarm")
        wakeLock.acquire(0)


        val intent_: Intent = Intent(context, MainActivity::class.java)
        intent_.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        context.startActivity(intent_)
        fullscreenPendingIntent.send()
        //context.startActivity(fullscreenIntent) //액티비티 실행
    }
}


