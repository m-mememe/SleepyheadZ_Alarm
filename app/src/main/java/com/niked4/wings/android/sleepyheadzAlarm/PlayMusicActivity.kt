package com.niked4.wings.android.sleepyheadzAlarm

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.preference.PreferenceManager
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.math.min

class PlayMusicActivity: AppCompatActivity(){
    val area2id = mapOf(
        "mito"       to "080010",
        "utsunomiya" to "090010",
        "maebashi"   to "100010",
        "saitama"    to "110010",
        "chiba"      to "120010",
        "tokyo"      to "130010",
        "yokohama"   to "140010"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarming)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        //現在時刻の表示
        val calendar = Calendar.getInstance()
        val alarmTime = findViewById<TextView>(R.id.AlarmTime)
        val minute = calendar.get(Calendar.MINUTE)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        if(minute < 10)
            alarmTime.text = " ${hour} : 0${minute} "
        else
            alarmTime.text = " ${hour} : ${minute} "

        //アラームの再生（サービス）
        val intent = Intent(this, PlayMusicService::class.java)
            .putExtra("media", intent.getStringExtra("media"))
        startService(intent)
        Toast.makeText(this, R.string.tv_alarm_awake, Toast.LENGTH_LONG).show()

        //天気情報の取得
        val receiver = WeatherReceiver()
        receiver.execute()
    }

    override fun onDestroy() {
        stopService(Intent(this, PlayMusicService::class.java))
        super.onDestroy()
    }

    fun stopAlarm(view: View){
        finish()
    }

    private inner class WeatherReceiver: AsyncTask<String, String, String>() {
        override fun doInBackground(vararg params: String?): String {
            val prefs = PreferenceManager.getDefaultSharedPreferences(this@PlayMusicActivity)
            val town = prefs.getString("livingArea", "tokyo")
            val id = area2id[town]
            val urlStr = "http://weather.livedoor.com/forecast/webservice/json/v1?city=${id}"
            val url = URL(urlStr)
            val con = url.openConnection() as HttpURLConnection
            con.requestMethod = "GET"
            con.connect()
            val stream = con.inputStream
            val result = is2String(stream)
            con.disconnect()
            stream.close()
            return result
        }

        override fun onPostExecute(result: String) {
            val rootJSON = JSONObject(result)
            val title = rootJSON.getString("title")
            val forecasts = rootJSON.getJSONArray("forecasts")
            val forecastNow = forecasts.getJSONObject(0)
            val weather = forecastNow.getString("telop")
            val calendar = Calendar.getInstance()
            val clPlayMusic = findViewById<ConstraintLayout>(R.id.cl_play_music)
            val tvTitle = findViewById<TextView>(R.id.tv_title)
            val tvTelop = findViewById<TextView>(R.id.tv_telop)
            val ivWeather = findViewById<ImageView>(R.id.tv_weather)
            tvTelop.text = weather

            //情報を画面に反映
            tvTitle.text = title
            when(weather){
                "晴れ"     -> ivWeather.setImageBitmap(BitmapFactory.decodeResource(applicationContext.resources, R.drawable.sunny))
                "曇り"     -> ivWeather.setImageBitmap(BitmapFactory.decodeResource(applicationContext.resources, R.drawable.cloudy))
                "雨"       -> ivWeather.setImageBitmap(BitmapFactory.decodeResource(applicationContext.resources, R.drawable.rainy))
                "晴のち曇" -> ivWeather.setImageBitmap(BitmapFactory.decodeResource(applicationContext.resources, R.drawable.sunny2cloudy))
                "晴のち雨" -> ivWeather.setImageBitmap(BitmapFactory.decodeResource(applicationContext.resources, R.drawable.sunny2rainy))
                "曇のち晴" -> ivWeather.setImageBitmap(BitmapFactory.decodeResource(applicationContext.resources, R.drawable.cloudy2sunny))
                "曇のち雨" -> ivWeather.setImageBitmap(BitmapFactory.decodeResource(applicationContext.resources, R.drawable.cloudy2rainy))
                "雨のち晴" -> ivWeather.setImageBitmap(BitmapFactory.decodeResource(applicationContext.resources, R.drawable.rainy2sunny))
                "雨のち曇" -> ivWeather.setImageBitmap(BitmapFactory.decodeResource(applicationContext.resources, R.drawable.rainy2cloudy))
                "晴時々曇" -> ivWeather.setImageBitmap(BitmapFactory.decodeResource(applicationContext.resources, R.drawable.sunny_often_cloudy))
                "晴時々雨" -> ivWeather.setImageBitmap(BitmapFactory.decodeResource(applicationContext.resources, R.drawable.sunny_often_rainy))
                "曇時々晴" -> ivWeather.setImageBitmap(BitmapFactory.decodeResource(applicationContext.resources, R.drawable.cloudy_often_sunny))
                "曇時々雨" -> ivWeather.setImageBitmap(BitmapFactory.decodeResource(applicationContext.resources, R.drawable.cloudy_often_rainy))
                "雨時々晴" -> ivWeather.setImageBitmap(BitmapFactory.decodeResource(applicationContext.resources, R.drawable.rainy_often_sunny))
                "雨時々曇" -> ivWeather.setImageBitmap(BitmapFactory.decodeResource(applicationContext.resources, R.drawable.rainy_often_cloudy))
            }
            //特定の天気は表示を変える可能性がある
            val sunny = Regex("晴")
            if(sunny.containsMatchIn(weather)){
                //昼の時
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                if((6 <= hour) and (hour <= 18)) {
                    clPlayMusic.setBackgroundColor(Color.parseColor("#33aaff"))
                }
                //夜の時
                else{
                    when(weather){
                        "晴れ"     -> ivWeather.setImageBitmap(BitmapFactory.decodeResource(applicationContext.resources, R.drawable.moon))
                        "晴のち曇" -> ivWeather.setImageBitmap(BitmapFactory.decodeResource(applicationContext.resources, R.drawable.moon2cloudy))
                        "晴のち雨" -> ivWeather.setImageBitmap(BitmapFactory.decodeResource(applicationContext.resources, R.drawable.moon2rainy))
                        "晴時々曇" -> ivWeather.setImageBitmap(BitmapFactory.decodeResource(applicationContext.resources, R.drawable.moon_often_cloudy))
                        "晴時々雨" -> ivWeather.setImageBitmap(BitmapFactory.decodeResource(applicationContext.resources, R.drawable.moon_often_rainy))
                        "曇時々晴" -> ivWeather.setImageBitmap(BitmapFactory.decodeResource(applicationContext.resources, R.drawable.cloudy_often_moon))
                        "雨時々晴" -> ivWeather.setImageBitmap(BitmapFactory.decodeResource(applicationContext.resources, R.drawable.rainy_often_moon))
                    }
                }
            }
        }

        private fun is2String(stream: InputStream): String{
            val sb = StringBuilder()
            val reader = BufferedReader(InputStreamReader(stream, "UTF-8"))
            var line = reader.readLine()
            while(line != null){
                sb.append(line)
                line = reader.readLine()
            }
            reader.close()
            return sb.toString()
        }
    }
}