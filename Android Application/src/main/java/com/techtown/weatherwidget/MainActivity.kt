package com.techtown.weatherwidget

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.os.*
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.android.volley.AuthFailureError
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.techtown.weatherwidget.MapChangeActivity.Companion.KEY_CITYNAME
import com.techtown.weatherwidget.MapChangeActivity.Companion.KEY_LAT
import com.techtown.weatherwidget.MapChangeActivity.Companion.KEY_LON
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToLong
import com.google.android.gms.location.*
import android.app.AlarmManager
import android.view.WindowManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.techtown.weatherwidget.WeaVariable.alarmMsga
import java.text.ParseException
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), LocationListener {

    companion object {
        var requestQueue: RequestQueue? = null
        var MainData: String? = "radioBtnCurrent"
        var GPSLat: String? = null
        var GPSLon: String? = null
        var context_main: Context? = null
    }

    //
    val channel_name: String = "CHANNEL_1"
    val CHANNEL_ID: String = "MY_CH"
    val notificationId: Int = 1002

        //
    private var alarmManager: AlarmManager? = null
    private var mCalender: GregorianCalendar? = null
    private var notificationManager: NotificationManager? = null
    var builder: NotificationCompat.Builder? = null




    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null // 현재 위치를 가져오기 위한 변수
    lateinit var mLastLocation: Location                                          // 위치 값을 가지고 있는 객체
    internal lateinit var mLocationRequest: LocationRequest                       // 위치 정보 요청의 매개변수를 저장하는
    private val REQUEST_PERMISSION_LOCATION = 10

    private var dateView: TextView? = null
    private var cityView: TextView? = null
    private var weatherDescriptionView: TextView? = null
    private var tempView: TextView? = null
    private var weatherIconView: ImageView? = null

    private lateinit var mPreferences: SharedPreferences
    private var difMsg: String? = null
    private var weatherMsg: String? = null

    private var itsRain = false
    private var itsSnow = false
    private var itsDust = false
    var ctempC : String? = null
    var getCityName : String? = null
    var cIconId : Int = 100

    //메뉴 버튼바
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.title_menu, menu)
        return true
    }

    //메뉴 버튼 이벤트
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val firstIntent = Intent(this, MainActivity::class.java)
        val secondIntent = Intent(this@MainActivity, AlarmActivity::class.java)
        val thirdIntent = Intent(this@MainActivity, MapChangeActivity::class.java)

        when (item.itemId) {
            R.id.getCurrentWeatherBtn -> {// 새로고침 버튼
                doWorkOneTime()
                doWorkPeriodic()        //백그라운드시에도 자동 갱신
                getHourlyWeather()
                setWeatherData()
                Toast.makeText(this, "날씨 정보를 가져오는 중", Toast.LENGTH_SHORT).show()
                Thread.sleep(2000)       //데이터를 저장하는 데에 시간이 걸림
                finish()
                startActivity(firstIntent)
                setView()
                return true
            }
            R.id.AlarmLayoutBtn -> {            // 메뉴 알람 버튼
                startActivity(secondIntent)

                notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                mCalender = GregorianCalendar()
                Log.v("HelloAlarmActivity", mCalender!!.getTime().toString())
                CurrAlarm()
                return true
            }
            R.id.setCityLayoutBtn -> { // 메뉴 위치 버튼
                startActivity(thirdIntent)
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //dowork에서 함수를 메인클래스 함수 접근하기 위해 위치 선언
       context_main = this

         //알림 후 잠금화면 위에 액티비티 실행
        window.setFlags(
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                   (WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON))

        setContentView(R.layout.activity_main)
        mLocationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY                // 정확성
        }
        dateView = findViewById(R.id.CurrentTimeText)
        cityView = findViewById(R.id.CityNameText)
        weatherDescriptionView = findViewById(R.id.WeatherDescriptionText)
        tempView = findViewById(R.id.CTempText)
        weatherIconView = findViewById(R.id.WeatherIcon)
        // volley를 쓸 때 큐가 비어있으면 새로운 큐 생성
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(applicationContext)
        }
    }


    // 날씨 API 데이터 가져오기
   fun getHourlyWeather() {

        // 저장되어 있는 도시 키값 받기
        mPreferences = getSharedPreferences(MainData, MODE_PRIVATE)
        val currentlat = mPreferences.getString(KEY_LAT, "")
        val currentlon = mPreferences.getString(KEY_LON, "")
        // API 주소와 키
        val url =
            "https://api.openweathermap.org/data/2.5/onecall?lat=$currentlat&lon=$currentlon&exclude=minutely,daily,alerts&appid="+"{API 키 값}"+"&lang=KR&units=metric"

        val request: StringRequest =
            object : StringRequest(Method.GET, url, Response.Listener { response ->
                try {

                    // API로 받은 파일 jsonobject로 새로운 객체 선언
                    val jsonObject = JSONObject(response)

                    // API로 받은 json을  string으로 변환 후 파일에 저장
                    mPreferences = getSharedPreferences("hourweather", MODE_PRIVATE)
                    val preferencesEditor: SharedPreferences.Editor = mPreferences.edit()
                    preferencesEditor.putString("saveobj", jsonObject.toString())
                    preferencesEditor.apply()

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener { }) {
                @Throws(AuthFailureError::class)
                override fun getParams(): Map<String, String> {
                    return HashMap()
                }
            }

        request.setShouldCache(false)
        requestQueue!!.add(request)
        //Toast.makeText(this, "날씨 정보를 가져오는 중", Toast.LENGTH_SHORT).show()
       //Thread.sleep(2000)  // 기기마다 데이터 저장하는 시간이 다름 (딜레이)

    }


    // API 데이터로 화면 갱신
    fun setWeatherData() {
        mPreferences = getSharedPreferences("hourweather", MODE_PRIVATE)
        val getSaveObj = mPreferences.getString("saveobj", null)

        while (getSaveObj != null) {
            try {
                mPreferences = getSharedPreferences(MainData, MODE_PRIVATE)
                getCityName = mPreferences.getString(KEY_CITYNAME, "현재 위치")
                val jsonObject = JSONObject(getSaveObj)

                // 시간대별 데이터
                for (i in 1..12) {
                    val hourlyJsonObject = jsonObject.getJSONArray("hourly")
                    // 1시간 후 ~ 12시간 후 날씨 데이터 추출
                    val houlryTimeJsonObject = hourlyJsonObject.getJSONObject(i)
                    val hourlyWeatherJsonArray = houlryTimeJsonObject.getJSONArray("weather")
                    val hourlyWeatherJsonObject = hourlyWeatherJsonArray.getJSONObject(0)
                    val hiconID = hourlyWeatherJsonObject.getInt("id")
                    val htempC = houlryTimeJsonObject.getString("temp")
                    val htempK = ((htempC.toDouble() + 274.15) * 100).roundToLong() / 100.0

                    //12시간 온도 배열
                    WeaVariable.TempArray[i - 1] = htempK
                    //12시간 날씨 배열
                    WeaVariable.WeatherIdArray[i - 1] = hiconID
                }

                WeaVariable.maxTemp = WeaVariable.TempArray[0]
                // 최대 온도 구하기
                for (i in 1..12) {
                    if (WeaVariable.TempArray[i - 1] > WeaVariable.maxTemp) {
                        WeaVariable.maxTemp = WeaVariable.TempArray[i - 1]
                    }
                }

                // 최소 온도 구하기
                WeaVariable.minTemp = WeaVariable.TempArray[0]
                for (i in 1..12) {
                    if (WeaVariable.TempArray[i - 1] < WeaVariable.minTemp) {
                        WeaVariable.minTemp = WeaVariable.TempArray[i - 1]
                    }
                }

                // 일교차 조건
                difMsg = if (WeaVariable.Tempdif > 10) {
                    "일교차가 크므로 겉옷을 챙겨가시고, "
                } else {
                    ""
                }
                for (i in 0..11) {
                    val weatherID = WeaVariable.WeatherIdArray[i]
                    if (weatherID / 100 == 3) {
                        itsRain = true
                    } else if (weatherID / 100 == 5) {
                        itsRain = true
                    } else if (weatherID / 100 == 6) {
                        itsSnow = true
                    } else if (weatherID / 100 == 2) {
                        itsRain = true
                    } else if (weatherID == 761 || weatherID == 751 || weatherID == 731) {
                        itsDust = true
                    }
                }
                if (itsRain && !itsSnow && !itsDust) {
                    weatherMsg = "비가 올 예정이니 우산을 챙겨가세요."
                } else if (!itsRain && itsSnow && !itsDust) {
                    weatherMsg = "눈이 올 예정이니 우산을 챙기고 옷을 따듯하게 입으세요."
                } else if (!itsRain && !itsSnow && itsDust) {
                    weatherMsg = "미세먼지가 많거나 황사가 있을 예정이니 마스크를 챙겨가세요."
                } else if (itsRain && itsSnow && !itsDust) {
                    weatherMsg = "비와 눈이 올 예정이니 우산을 챙기고 옷을 따듯하게 입으세요."
                } else if (itsRain && !itsSnow && itsDust) {
                    weatherMsg = "미세먼지와 황사가 있고 비가 올 예정이니 우산과 마스크를 챙기세요"
                } else if (!itsRain && itsSnow && itsDust) {
                    weatherMsg = "미세먼지와 황사가 있고 눈이 올 예정이니 우산과 마스크를 챙기고 옷을 따듯하게 입으세요."
                } else if (itsRain && itsSnow && itsDust) {
                    weatherMsg = ""
                } else {
                    weatherMsg = "특별한 기상 상황이 없습니다. 좋은 하루 되세요."
                }


                // 알림 메세지 출력
                WeaVariable.alarmMsg = "$difMsg 금일 $weatherMsg"
                WeaVariable.alarmMsgTwo = "안녕 하세요"


                // 현재 온도 가져오기
                val currentJsonObJect = jsonObject.getJSONObject("current")
                val currentWeatherJsonArray = currentJsonObJect.getJSONArray("weather")
                val currentWeatherJsonObject = currentWeatherJsonArray.getJSONObject(0)
                val cDescriptionString = currentWeatherJsonObject.getString("description")
                weatherDescriptionView!!.text = cDescriptionString

                cIconId = currentWeatherJsonObject.getInt("id")

                // 아이콘 수정

                // 기온 키값 받기
                ctempC = currentJsonObJect.getString("temp")

                break
            } catch (e: JSONException) {
                val elem = e.stackTrace
                for (i in elem.indices) {
                    error(elem[i])
                }
            }
        }
    }

    fun setView(){
        tempView!!.text = "$ctempC°C"
        cityView!!.text = getCityName
        val now = System.currentTimeMillis()
        val date = Date(now)
        //년, 월, 일 형식으로. 시,분,초 형식으로 객체화하여 String에 형식대로 넣음
        val simpleDateFormatDay = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val simpleDateFormatTime = SimpleDateFormat("HH:mm", Locale.getDefault())
        val getDay = simpleDateFormatDay.format(date)
        val getTime = simpleDateFormatTime.format(date)

        //getDate에 개행을 포함한 형식들을 넣은 후 dateView에 text설정
        val getDate = """
                      $getDay
                      $getTime
                      """.trimIndent()

        dateView!!.text = getDate
        when {
            cIconId >= 801 -> {
                weatherIconView!!.setImageResource(R.drawable.cloudy)
            }
            cIconId == 800 -> {
                weatherIconView!!.setImageResource(R.drawable.sunrise)
            }
            cIconId > 700 -> {
                weatherIconView!!.setImageResource(R.drawable.fog)
            }
            cIconId > 600 -> {
                weatherIconView!!.setImageResource(R.drawable.snow)
            }
            cIconId > 520 -> {
                weatherIconView!!.setImageResource(R.drawable.sudden_rain)
            }
            cIconId == 511 -> {
                weatherIconView!!.setImageResource(R.drawable.snow)
            }
            cIconId >= 500 -> {
                weatherIconView!!.setImageResource(R.drawable.rain)
            }
            cIconId >= 300 -> {
                weatherIconView!!.setImageResource(R.drawable.rain)
            }
            cIconId >= 200 -> {
                weatherIconView!!.setImageResource(R.drawable.rain)
            }
            else -> {
                weatherIconView!!.setImageResource(R.drawable.unknown)
            }
        }
    }

    private fun startLocationUpdates() {

        //FusedLocationProviderClient의 인스턴스를 생성.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // 기기의 위치에 관한 정기 업데이트를 요청하는 메서드 실행
        // 지정한 루퍼 스레드(Looper.myLooper())에서 콜백(mLocationCallback)으로 위치 업데이트를 요청
        mFusedLocationProviderClient!!.requestLocationUpdates(mLocationRequest,
            mLocationCallback,
            Looper.myLooper())
    }

    // 시스템으로 부터 위치 정보를 콜백으로 받음
    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            // 시스템에서 받은 location 정보를 onLocationChanged()에 전달
            locationResult.lastLocation
            onLocationChanged(locationResult.lastLocation)
        }
    }

    // 시스템으로 부터 받은 위치정보를 변수에 저장
    override fun onLocationChanged(location: Location) {
        mLastLocation = location
        GPSLat = mLastLocation.latitude.toString()
        GPSLon = mLastLocation.longitude.toString()
    }

    // 위치 권한이 있는지 확인하는 메서드
    private fun checkPermissionForLocation(context: Context): Boolean {
        // Android 6.0 Marshmallow 이상에서는 위치 권한에 추가 런타임 권한이 필요
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                // 권한이 없으므로 권한 요청 알림 보내기
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSION_LOCATION)
                false
            }
        } else {
            true
        }
    }

    // 사용자에게 권한 요청 후 결과에 대한 처리 로직
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            } else {
                Log.d("ttt", "onRequestPermissionsResult() _ 권한 허용 거부")
                Toast.makeText(this, "권한이 없어 해당 기능을 실행할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBackPressed() {
        super.onPause()
        super.onStop()
        super.onBackPressed()
    }

    override fun onStart() {
        super.onStart()
        setWeatherData()
        setView()
        if (checkPermissionForLocation(this)) {
            startLocationUpdates()
        }
    }


    private fun CurrAlarm() {
        //AlarmReceiver에 값 전달
        val receiverIntent = Intent(this@MainActivity, CurrentAlarm::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this@MainActivity, 0, receiverIntent, 0)
        val from = "2021-12-02 23:00:00" //임의로 날짜와 시간을 지정

        //날짜 포맷을 바꿔주는 소스코드
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        var datetime: Date? = null
        try {
            datetime = dateFormat.parse(from)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        val calendar = Calendar.getInstance()
        calendar.time = datetime
        alarmManager!![AlarmManager.RTC, calendar.timeInMillis] = pendingIntent
    }


    // 반복
    private fun doWorkPeriodic() {
        val workRequest = PeriodicWorkRequestBuilder<com.techtown.weatherwidget.WorkManager>(1, TimeUnit.HOURS).build()
        PeriodicWorkRequest.Builder(com.techtown.weatherwidget.WorkManager::class.java, 1, TimeUnit.HOURS).build()

        val workManager = WorkManager.getInstance()

        workManager.enqueue(workRequest)
    }

    // 함수 실행 후 한번
    private fun doWorkOneTime() {

        //test
        Toast.makeText(this, alarmMsga, Toast.LENGTH_SHORT).show()

        val workRequest = OneTimeWorkRequestBuilder<com.techtown.weatherwidget.WorkManager>().build()

        val workManager = WorkManager.getInstance()
        workManager.enqueue(workRequest)
    }
}




