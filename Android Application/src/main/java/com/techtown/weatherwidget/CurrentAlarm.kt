package com.techtown.weatherwidget

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat


class CurrentAlarm : BroadcastReceiver() {
    var manager: NotificationManager? = null
    var builder: NotificationCompat.Builder? = null
    override fun onReceive(context: Context, intent: Intent) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        builder = null
        manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager!!.createNotificationChannel(
                NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT)
            )
            NotificationCompat.Builder(context, CHANNEL_ID)
        } else {
            NotificationCompat.Builder(context)
        }

        //알림창 클릭 시 activity 화면 부름
        val intent2 = Intent(context, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(context, 101, intent2, PendingIntent.FLAG_UPDATE_CURRENT)

        //알림창 제목
        builder!!.setContentTitle(WeaVariable.alarmMsga)
        //알림창 아이콘
        builder!!.setSmallIcon(R.drawable.six)
        //알림창 터치시 자동 삭제
        builder!!.setAutoCancel(true)
        builder!!.setContentIntent(pendingIntent)
        val notification = builder!!.build()
        manager!!.notify(1, notification)
    }

    companion object {
        //오레오 이상은 반드시 채널을 설정해줘야 Notification이 작동함
        private const val CHANNEL_ID = "channel1"
        private const val CHANNEL_NAME = "Channel1"
    }
}