package com.websarva.wings.android.sleepyhead

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.view.WindowManager
import android.widget.Toast

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