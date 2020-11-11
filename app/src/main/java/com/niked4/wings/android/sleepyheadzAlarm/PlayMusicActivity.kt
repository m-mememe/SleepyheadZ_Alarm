package com.niked4.wings.android.sleepyheadzAlarm

import android.app.SearchManager
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
import com.google.android.material.snackbar.Snackbar
import com.niked4.wings.android.sleepyheadzAlarm.MainActivity.Companion.registerAlarm
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

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
    val area2kanji = mapOf(
        "mito"       to "水戸",
        "utsunomiya" to "宇都宮",
        "maebashi"   to "前橋",
        "saitama"    to "さいたま",
        "chiba"      to "千葉",
        "tokyo"      to "東京",
        "yokohama"   to "横浜"
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
        val alarmId = intent.getStringExtra("alarmId")
        val media = intent.getStringExtra("media")
        val intent = Intent(this, PlayMusicService::class.java)
            .putExtra("media", media)
        startService(intent)
        Toast.makeText(this, R.string.tv_alarm_awake, Toast.LENGTH_LONG).show()

        //24時間後に同じアラームをセットする
        registerAlarm(this, alarmId, 24 * 60, media)

        //天気情報の取得
        val receiver = WeatherReceiver()
        receiver.execute()

        //天気画像にリスナをセット
        val ivWeather = findViewById<ImageView>(R.id.iv_weather)
        ivWeather.setOnClickListener {
            //Snackbarで確認する
            Snackbar.make(it, R.string.tv_weather_search_question, Snackbar.LENGTH_LONG)
                .setAction(R.string.tv_weather_search, View.OnClickListener {
                    //google検索で天気の詳細を調べる
                    val prefs = PreferenceManager.getDefaultSharedPreferences(this@PlayMusicActivity)
                    val town = prefs.getString("livingArea", "tokyo")
                    val id = area2kanji[town]
                    val intent = Intent(Intent.ACTION_WEB_SEARCH)
                    intent.setClassName("com.google.android.googlequicksearchbox", "com.google.android.googlequicksearchbox.SearchActivity")
                    intent.putExtra(SearchManager.QUERY, "天気 $id")
                    startActivity(intent)
                    finish()
                }).show()
        }
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
            try {
                val con = url.openConnection() as HttpURLConnection
                con.requestMethod = "GET"
                con.connect()
                val stream = con.inputStream
                val result = is2String(stream)
                con.disconnect()
                stream.close()
                return result
            }
            catch (ex: IOException){
                return "missedAccess"
            }
        }

        override fun onPostExecute(result: String) {
            //通信に失敗した場合
            if(result == "missedAccess"){
                Log.e("PlayMusicActivity", "HTTPリクエストに失敗")
                findViewById<TextView>(R.id.tv_title).text = "通信に"
                findViewById<TextView>(R.id.tv_telop).text = "失敗しました"
                return
            }

            val rootJSON = JSONObject(result)
            val title = rootJSON.getString("title")
            val forecasts = rootJSON.getJSONArray("forecasts")
            val forecastNow = forecasts.getJSONObject(0)
            val weather = forecastNow.getString("telop")
            val calendar = Calendar.getInstance()
            val clPlayMusic = findViewById<ConstraintLayout>(R.id.cl_play_music)
            val tvTitle = findViewById<TextView>(R.id.tv_title)
            val tvTelop = findViewById<TextView>(R.id.tv_telop)
            val ivWeather = findViewById<ImageView>(R.id.iv_weather)
            val res = applicationContext.resources

            //情報を画面に反映
            tvTitle.text = title
            tvTelop.text = weather
            when(weather){
                "晴れ"     -> ivWeather.setImageBitmap(BitmapFactory.decodeResource(res, R.drawable.sunny))
                "曇り"     -> ivWeather.setImageBitmap(BitmapFactory.decodeResource(res, R.drawable.cloudy))
                "雨"       -> ivWeather.setImageBitmap(BitmapFactory.decodeResource(res, R.drawable.rainy))
                "晴のち曇" -> ivWeather.setImageBitmap(BitmapFactory.decodeResource(res, R.drawable.sunny2cloudy))
                "晴のち雨" -> ivWeather.setImageBitmap(BitmapFactory.decodeResource(res, R.drawable.sunny2rainy))
                "曇のち晴" -> ivWeather.setImageBitmap(BitmapFactory.decodeResource(res, R.drawable.cloudy2sunny))
                "曇のち雨" -> ivWeather.setImageBitmap(BitmapFactory.decodeResource(res, R.drawable.cloudy2rainy))
                "雨のち晴" -> ivWeather.setImageBitmap(BitmapFactory.decodeResource(res, R.drawable.rainy2sunny))
                "雨のち曇" -> ivWeather.setImageBitmap(BitmapFactory.decodeResource(res, R.drawable.rainy2cloudy))
                "晴時々曇" -> ivWeather.setImageBitmap(BitmapFactory.decodeResource(res, R.drawable.sunny_often_cloudy))
                "晴時々雨" -> ivWeather.setImageBitmap(BitmapFactory.decodeResource(res, R.drawable.sunny_often_rainy))
                "曇時々晴" -> ivWeather.setImageBitmap(BitmapFactory.decodeResource(res, R.drawable.cloudy_often_sunny))
                "曇時々雨" -> ivWeather.setImageBitmap(BitmapFactory.decodeResource(res, R.drawable.cloudy_often_rainy))
                "雨時々晴" -> ivWeather.setImageBitmap(BitmapFactory.decodeResource(res, R.drawable.rainy_often_sunny))
                "雨時々曇" -> ivWeather.setImageBitmap(BitmapFactory.decodeResource(res, R.drawable.rainy_often_cloudy))
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
                        "晴れ"     -> ivWeather.setImageBitmap(BitmapFactory.decodeResource(res, R.drawable.moon))
                        "晴のち曇" -> ivWeather.setImageBitmap(BitmapFactory.decodeResource(res, R.drawable.moon2cloudy))
                        "晴のち雨" -> ivWeather.setImageBitmap(BitmapFactory.decodeResource(res, R.drawable.moon2rainy))
                        "晴時々曇" -> ivWeather.setImageBitmap(BitmapFactory.decodeResource(res, R.drawable.moon_often_cloudy))
                        "晴時々雨" -> ivWeather.setImageBitmap(BitmapFactory.decodeResource(res, R.drawable.moon_often_rainy))
                        "曇時々晴" -> ivWeather.setImageBitmap(BitmapFactory.decodeResource(res, R.drawable.cloudy_often_moon))
                        "雨時々晴" -> ivWeather.setImageBitmap(BitmapFactory.decodeResource(res, R.drawable.rainy_often_moon))
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