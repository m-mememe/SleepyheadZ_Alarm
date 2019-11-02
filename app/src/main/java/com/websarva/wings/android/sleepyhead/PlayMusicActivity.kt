package com.websarva.wings.android.sleepyhead

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.view.WindowManager
import android.widget.Toast

//import androidx.core.app.ComponentActivity
//import androidx.core.app.ComponentActivity.ExtraData
//import androidx.core.content.ContextCompat.getSystemService
//import android.icu.lang.UCharacter.GraphemeClusterBreak.T

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
        Toast.makeText(this, "AlarmAwake", Toast.LENGTH_LONG).show()
    }

    fun stopAlarm(view: View){
        stopService(Intent(this, PlayMusicService::class.java))
        finish()
    }
}