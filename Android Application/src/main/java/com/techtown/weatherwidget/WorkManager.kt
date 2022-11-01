package com.techtown.weatherwidget

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class WorkManager(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams){
    override fun doWork(): Result {

        // 함수 호출
        (MainActivity.context_main as MainActivity).getHourlyWeather()
        (MainActivity.context_main as MainActivity).setWeatherData()
        return Result.success()
    }
}