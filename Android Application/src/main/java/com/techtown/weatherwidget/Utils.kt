package com.techtown.weatherwidget

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi

object Utils {
    @SuppressLint("UnspecifiedImmutableFlag")
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun setAlarm(context: Context, timeOfAlarm: Long) {

        // Intent to start the Broadcast Receiver
        val broadcastIntent = Intent(context
            , AlarmReceiver::class.java)
        val pIntent = PendingIntent.getBroadcast(
            context,
            0,
            broadcastIntent,
            0
        )

        // 알람 매니저 세팅
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (System.currentTimeMillis() < timeOfAlarm) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmMgr.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    timeOfAlarm,
                    pIntent
                )
            }
        }
    }
}

//날씨 데이터 계산 값
object WeaVariable{
    var TempArray = DoubleArray(12)
    var WeatherIdArray = IntArray(12)
    var maxTemp : Double = 0.0
    var minTemp : Double = 0.0
    var Tempdif = maxTemp - minTemp
    var alarmMsg : String? = null
    var alarmMsga : String? = "123456"      // 나중에 지울 것
    var alarmMsgTwo: String? = null
}