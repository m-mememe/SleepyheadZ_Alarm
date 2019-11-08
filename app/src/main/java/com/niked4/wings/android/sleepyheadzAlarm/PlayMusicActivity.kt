package com.niked4.wings.android.sleepyheadzAlarm

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
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

        val calendar = Calendar.getInstance()
        val AlarmTime = findViewById<TextView>(R.id.AlarmTime)
        if(calendar.get(Calendar.MINUTE) < 10) {
            AlarmTime.text = "${calendar.get(Calendar.HOUR)} : 0${calendar.get(Calendar.MINUTE)}"
        }
        else{
            AlarmTime.text = "${calendar.get(Calendar.HOUR)} : ${calendar.get(Calendar.MINUTE)}"
        }
        startService(Intent(this, PlayMusicService::class.java))
        Toast.makeText(this, R.string.tv_alarm_awake, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        stopService(Intent(this, PlayMusicService::class.java))
        super.onDestroy()
    }

    fun stopAlarm(view: View){
        finish()
    }
}