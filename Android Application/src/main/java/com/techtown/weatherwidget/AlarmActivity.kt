package com.techtown.weatherwidget

import android.annotation.SuppressLint
import android.app.Application
import android.app.TimePickerDialog
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*
import android.view.WindowManager



class AlarmActivity : AppCompatActivity() {

    @SuppressLint("ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_alarm)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        //뒤로가기 버튼 표시
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        var timeInMilliSeconds: Long = 0
        val receiver = ComponentName(applicationContext, BootCompleteReceiver::class.java)
        applicationContext.packageManager?.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )

        //현재 시간 가져오기
        val startTimeText: TextView = findViewById(R.id.StartTimeText)
        val setStartBtn = findViewById<Button>(R.id.setStartBtn)
        setStartBtn.setOnClickListener{
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            // 타임피커 사용
            val timePickerDialog = TimePickerDialog(this,android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                { _, hourOfDay, minuteOfHour ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minuteOfHour)
                    calendar.set(Calendar.SECOND, 0)
                    val formattedTime = String.format("%02d:%02d", hourOfDay, minuteOfHour)
                    startTimeText.text = formattedTime
                    val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
                    val formattedDate = sdf.format(calendar.time)
                    val date = sdf.parse(formattedDate)
                    timeInMilliSeconds = date!!.time
                }, hour, minute, false)
            timePickerDialog.show()
        }

        //알람시작 버튼
        val setAlarm = findViewById<Button>(R.id.setAlarmTimeBtn)
        setAlarm.setOnClickListener {
            if (timeInMilliSeconds.toInt() != 0) {
                Toast.makeText(this, "알람이 설정되었습니다.", Toast.LENGTH_LONG).show()

                val sharedPref = this.getSharedPreferences("MyPref", Context.MODE_PRIVATE)
                    ?: return@setOnClickListener
                with(sharedPref.edit()) {
                    putLong("timeInMilli", timeInMilliSeconds)
                    apply()
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    Utils.setAlarm(this, timeInMilliSeconds)
                }
            } else {
                Toast.makeText(this, "알람 시간 설정을 먼저 해주세요.", Toast.LENGTH_LONG).show()
            }
        }
    }
    //뒤로가기 버튼 클릭 이벤트
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }
}
