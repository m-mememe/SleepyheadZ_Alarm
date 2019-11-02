package com.websarva.wings.android.sleepyhead

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class AlarmBroadcastReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        //AlarmManagerで時刻になったら起動される、音楽を流すアクティビティを起動
        val activityIntent = Intent(context, PlayMusicActivity::class.java)
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context?.startActivity(activityIntent)
        Toast.makeText(context, "AlarmSet", Toast.LENGTH_LONG).show()
    }
}