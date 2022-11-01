package com.techtown.weatherwidget

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class BootCompleteReceiver : BroadcastReceiver() {
    @SuppressLint("ObsoleteSdkInt")
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            val sharedPref = context.getSharedPreferences("MyPref", Context.MODE_PRIVATE) ?: return
            val timeInMilli = sharedPref.getLong("timeInMilli", 1)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Utils.setAlarm(context, timeInMilli)
            }
        }
    }
}
