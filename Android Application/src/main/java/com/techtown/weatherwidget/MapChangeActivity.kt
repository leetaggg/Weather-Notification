package com.techtown.weatherwidget

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException

import android.content.SharedPreferences
import android.widget.*
import com.techtown.weatherwidget.MainActivity.Companion.MainData
import com.techtown.weatherwidget.MainActivity.Companion.GPSLat
import com.techtown.weatherwidget.MainActivity.Companion.GPSLon


class MapChangeActivity : AppCompatActivity() {
    companion object {
        const val KEY_LAT = "latitude"
        const val KEY_LON = "longitude"
        const val KEY_CITYNAME = "cityname"
        const val RADIO_ONE = "radioBtnOne"
        const val RADIO_TWO = "radioBtnTwo"
        const val RADIO_THREE = "radioBtnThree"
        const val RADIO_CURRENT = "radioBtnCurrent"
    }

    private lateinit var mPreferences: SharedPreferences
    lateinit var RdGroup: RadioGroup
    lateinit var RBtnOne: RadioButton
    lateinit var RBtnTwo: RadioButton
    lateinit var RBtnThree: RadioButton
    lateinit var RBtnCurr: RadioButton



    var getaddressline: String? = null
    var getLat: String? = null
    var getLon: String? = null



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

    //onCreate후 실행
    override fun onStart() {
        super.onStart()
        getGpsName()
        //RadioBtn 이름 동기화

        mPreferences = getSharedPreferences(RADIO_ONE, MODE_PRIVATE)
        RBtnOne.text = mPreferences.getString(KEY_CITYNAME, "없음")
        mPreferences = getSharedPreferences(RADIO_TWO, MODE_PRIVATE)
        RBtnTwo.text = mPreferences.getString(KEY_CITYNAME, "없음")
        mPreferences = getSharedPreferences(RADIO_THREE, MODE_PRIVATE)
        RBtnThree.text = mPreferences.getString(KEY_CITYNAME, "없음")
        mPreferences = getSharedPreferences(RADIO_CURRENT, MODE_PRIVATE)
        RBtnCurr.text = mPreferences.getString(KEY_CITYNAME, "현재 위치")
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_change)


        RBtnCurr = findViewById(R.id.RdBtnCurr)
        RBtnOne = findViewById(R.id.RdBtnOne)
        RBtnTwo = findViewById(R.id.RdBtnTwo)
        RBtnThree = findViewById(R.id.RdBtnThree)
        RdGroup = findViewById(R.id.RGroup)

        val geocoder = Geocoder(this)
        val conversionBtn = findViewById<View>(R.id.ConversionBtn) as Button
        val mapBtn = findViewById<View>(R.id.MapBtn) as Button
        val addressEdit = findViewById<View>(R.id.AddressEdit) as EditText
        val addressResultBtn = findViewById<View>(R.id.AddressResultText) as TextView
        val saveBtn = findViewById<View>(R.id.SaveBtn) as Button
        val delectBtn = findViewById<View>(R.id.DelectBtn) as Button

        // 뒤로가기 표시
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //변환 버튼 이벤트
        conversionBtn.setOnClickListener {
            var list: List<Address>? = null
            val cityStr = addressEdit.text.toString()   // 지역 이름
            try {
                list = geocoder.getFromLocationName(cityStr, 10 )  // 지역 이름 ,읽을 개수
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("test", "서버 주소변환 에러발생")
            }
            if (list != null) {
                if (list.isEmpty()) {
                    addressResultBtn.text = "해당 주소 정보는 없습니다"
                } else {
                    getaddressline = list[0].getAddressLine(0)    // 주소
                    getLat = list[0].latitude.toString()                // 위도
                    getLon = list[0].longitude.toString()               // 경도
                    addressResultBtn.text = "검색한 주소 : $getaddressline \n 위도 : $getLat, 경도 : $getLon"  // 출력
                }
            }
        }

        // 주소입력후 지도 버튼 클릭시 해당 위도경도값의 지도화면으로 이동
        mapBtn.setOnClickListener {
            var list: List<Address>? = null
            val cityStr = addressEdit.text.toString()
            try {
                list = geocoder.getFromLocationName(
                    cityStr,  // 지역 이름
                    10
                ) // 읽을 개수
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("test", "서버 주소변환 에러발생")
            }
            if (list != null) {
                if (list!!.isEmpty()) {
                    addressResultBtn.text = "해당 주소 정보는 없습니다"
                } else {
                    // 해당되는 주소로 인텐트 날리기
                    val addr = list!![0]
                    val lat = addr.latitude
                    val lon = addr.longitude
                    val sss = String.format("geo:%f,%f", lat, lon)
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(sss)
                    )
                    startActivity(intent)
                }
            }
        }
        saveBtn.setOnClickListener {
            when (RdGroup.checkedRadioButtonId) {
                R.id.RdBtnOne -> {
                    mPreferences = getSharedPreferences(RADIO_ONE, MODE_PRIVATE)
                    val preferencesEditor: SharedPreferences.Editor = mPreferences.edit()
                    preferencesEditor.putString(KEY_CITYNAME, getaddressline)
                    preferencesEditor.putString(KEY_LAT, getLat.toString())
                    preferencesEditor.putString(KEY_LON, getLon.toString())
                    preferencesEditor.apply()
                    MainData = "radioBtnOne"
                }
                R.id.RdBtnTwo -> {
                    mPreferences = getSharedPreferences(RADIO_TWO, MODE_PRIVATE)
                    val preferencesEditor: SharedPreferences.Editor = mPreferences.edit()
                    preferencesEditor.putString(KEY_CITYNAME, getaddressline)
                    preferencesEditor.putString(KEY_LAT, getLat.toString())
                    preferencesEditor.putString(KEY_LON, getLon.toString())
                    preferencesEditor.apply()
                    MainData = "radioBtnTwo"
                }
                R.id.RdBtnThree -> {
                    mPreferences = getSharedPreferences(RADIO_THREE, MODE_PRIVATE)
                    val preferencesEditor: SharedPreferences.Editor = mPreferences.edit()
                    preferencesEditor.putString(KEY_CITYNAME, getaddressline)
                    preferencesEditor.putString(KEY_LAT, getLat.toString())
                    preferencesEditor.putString(KEY_LON, getLon.toString())
                    preferencesEditor.apply()
                    MainData = "radioBtnOne"
                }
                R.id.RdBtnCurr->{}
            }

            //버튼 이름 동기화
            mPreferences = getSharedPreferences(RADIO_ONE, MODE_PRIVATE)
            RBtnOne.text = mPreferences.getString(KEY_CITYNAME, "없음")
            mPreferences = getSharedPreferences(RADIO_TWO, MODE_PRIVATE)
            RBtnTwo.text = mPreferences.getString(KEY_CITYNAME, "없음")
            mPreferences = getSharedPreferences(RADIO_THREE, MODE_PRIVATE)
            RBtnThree.text = mPreferences.getString(KEY_CITYNAME, "없음")
            mPreferences = getSharedPreferences(RADIO_CURRENT, MODE_PRIVATE)
            RBtnCurr.text = mPreferences.getString(KEY_CITYNAME, "현재 위치")
        }


        //삭제 버튼 이벤트
        delectBtn.setOnClickListener {
            when (RdGroup.checkedRadioButtonId) {
                R.id.RdBtnOne -> {
                    mPreferences = getSharedPreferences(RADIO_ONE, MODE_PRIVATE)
                    val preferencesEditor: SharedPreferences.Editor = mPreferences.edit()
                    preferencesEditor.clear()
                    preferencesEditor.apply()
                }
                R.id.RdBtnTwo -> {
                    mPreferences = getSharedPreferences(RADIO_TWO, MODE_PRIVATE)
                    val preferencesEditor: SharedPreferences.Editor = mPreferences.edit()
                    preferencesEditor.clear()
                    preferencesEditor.apply()
                }
                R.id.RdBtnThree -> {
                    mPreferences = getSharedPreferences(RADIO_THREE, MODE_PRIVATE)
                    val preferencesEditor: SharedPreferences.Editor = mPreferences.edit()
                    preferencesEditor.clear()
                    preferencesEditor.apply()
                }
                R.id.RdBtnCurr -> {
                }
            }

            //버튼 이름 동기화
            mPreferences = getSharedPreferences(RADIO_ONE, MODE_PRIVATE)
            RBtnOne.text = mPreferences.getString(KEY_CITYNAME, "없음")
            mPreferences = getSharedPreferences(RADIO_TWO, MODE_PRIVATE)
            RBtnTwo.text = mPreferences.getString(KEY_CITYNAME, "없음")
            mPreferences = getSharedPreferences(RADIO_THREE, MODE_PRIVATE)
            RBtnThree.text = mPreferences.getString(KEY_CITYNAME, "없음")

        }
        RdGroup.setOnCheckedChangeListener{ rdGruop, checkedRadioButtonId ->
            when (rdGruop.checkedRadioButtonId) {
                R.id.RdBtnOne -> {
                    MainData = "radioBtnOne"
                }
                R.id.RdBtnTwo -> {
                    MainData = "radioBtnTwo"
                }
                R.id.RdBtnThree -> {
                    MainData = "radioBtnThree"
                }
                R.id.RdBtnCurr -> {
                    MainData = "radioBtnCurrent"
                }
            }
        }
    }

    // 경도, 위도로 도시 이름 가져오기
    private fun getGpsName(){

        var gpsList: List<Address>? = null
        val geocoder = Geocoder(this)

        try {
            gpsList = geocoder.getFromLocation(
                GPSLat!!.toDouble(), GPSLon!!.toDouble(), 10)
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("test", "입출력 오류")
        }
        if (gpsList != null) {
            if (gpsList.isEmpty()) {
                Toast.makeText(this, "해당되는 주소 정보는 없습니다", Toast.LENGTH_SHORT).show()
            } else {
                getaddressline = gpsList[0].getAddressLine(0)
                getaddressline = getaddressline!!.replace("${gpsList[0].countryName} ", "")
                mPreferences = getSharedPreferences(RADIO_CURRENT, MODE_PRIVATE)
                val preferencesEditor: SharedPreferences.Editor = mPreferences.edit()
                preferencesEditor.putString(KEY_CITYNAME, getaddressline)
                preferencesEditor.putString(KEY_LAT, GPSLat.toString())
                preferencesEditor.putString(KEY_LON, GPSLon.toString())
                preferencesEditor.apply()
            }
        }
    }
}