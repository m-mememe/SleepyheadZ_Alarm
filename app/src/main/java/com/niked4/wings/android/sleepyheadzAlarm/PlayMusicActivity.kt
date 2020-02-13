package com.niked4.wings.android.sleepyheadzAlarm

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class PlayMusicActivity: AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarming)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        //現在時刻の表示と音楽の再生
        val calendar = Calendar.getInstance()
        val alarmTime = findViewById<TextView>(R.id.AlarmTime)
        if(calendar.get(Calendar.MINUTE) < 10)
            alarmTime.text = "${calendar.get(Calendar.HOUR)} : 0${calendar.get(Calendar.MINUTE)}"
        else
            alarmTime.text = "${calendar.get(Calendar.HOUR)} : ${calendar.get(Calendar.MINUTE)}"
        startService(Intent(this, PlayMusicService::class.java))
        Toast.makeText(this, R.string.tv_alarm_awake, Toast.LENGTH_LONG).show()

        //天気情報の取得
        val receiver = WeatherReceiver()
        receiver.execute()
    }

    override fun onDestroy() {
        stopService(Intent(this, PlayMusicService::class.java))
        super.onDestroy()
    }

    private inner class WeatherReceiver: AsyncTask<String, String, String>() {
        override fun doInBackground(vararg params: String?): String {
            val id = 120010
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
            val tvTitle = findViewById<TextView>(R.id.tvTitle)
            val tvWeather = findViewById<TextView>(R.id.tvWeather)
            tvTitle.text = title
            tvWeather.text = weather
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

    fun stopAlarm(view: View){
        finish()
    }
}