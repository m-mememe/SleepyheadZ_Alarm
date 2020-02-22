package com.niked4.wings.android.sleepyheadzAlarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmBroadcastReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        //AlarmManagerで時刻になったら起動される、音楽を流すアクティビティを起動
        val activityIntent = Intent(context, PlayMusicActivity::class.java)
        activityIntent.putExtra("media", intent?.getStringExtra("media"))
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context?.startActivity(activityIntent)
    }
}