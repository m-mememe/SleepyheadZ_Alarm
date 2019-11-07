package com.websarva.wings.android.sleepyhead

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.realm.Realm
import io.realm.Sort
import android.app.AlarmManager
import android.app.PendingIntent
import android.util.Log
import android.widget.Toast
import io.realm.kotlin.where
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var realm: Realm
    private lateinit var adapter: CustomRecyclerViewAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Realm（データベース）のインスタンス取得
        realm = Realm.getDefaultInstance()

        //データベースを元にセットされていたアラームを全てキャンセル
        val realmResults = realm.where(AlarmData::class.java)
            .findAll()
            .sort("id", Sort.ASCENDING)
        val column = realm.where<AlarmData>().count()
        for(i in 0 until column.toInt()){
            val alarmData = realmResults[i]
            for(j in 0 until (alarmData?.count ?: 0)) {
                val alarmId = "alarm:${alarmData?.id}.${j}"
                unregisterAlarm(alarmId)
            }
        }

        //アラームをセットする
        val c = Calendar.getInstance()
        val timeNow = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE)
        for(i in 0 until column.toInt()){
            val alarmData = realmResults[i]
            //アラームの設定がONならセット
            if(alarmData?.bool ?: false){
                var startTime = ((alarmData?.startHour ?: 0) * 60 + (alarmData?.startMinute ?: 0)  - timeNow)
                if (startTime<0) startTime += 24 * 60
                var deltaTime :Double = alarmData?.alarmTime?.toDouble() ?: 0.0
                deltaTime /= ((alarmData?.count ?: 2) - 1)
                for(j in 0 until (alarmData?.count ?: 0)) {
                    val alarmId = "alarm:${alarmData?.id}.${j}"
                    val addTime  = kotlin.math.floor(startTime + j * deltaTime).toInt()
                    registerAlarm(alarmId, addTime)
                }
            }
        }
    }

    override fun onStart(){
        super.onStart()
        //データベースからの読み出しとRecyclerViewの表示
        val realmResults = realm.where(AlarmData::class.java)
            .findAll()
            .sort("count", Sort.ASCENDING)
            .sort("alarmTime", Sort.ASCENDING)
            .sort("startMinute", Sort.ASCENDING)
            .sort("startHour", Sort.ASCENDING)
        layoutManager = LinearLayoutManager(this)
        val alarmList = findViewById<RecyclerView>(R.id.recyclerView)
        alarmList.layoutManager = layoutManager
        adapter = CustomRecyclerViewAdapter(realmResults)
        alarmList.adapter = this.adapter
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    //アラームの追加
    fun onAddAlarmClick(view: View){
        //アラームセットシーンに遷移
        val intent = Intent(this, TimerMenuActivity::class.java)
        startActivity(intent)
    }

    //5分タイマーのセットボタン
    fun onAddSnoozeClick(view: View){
        registerAlarm("snooze")
        Toast.makeText(this, R.string.tv_snooze, Toast.LENGTH_SHORT).show()
    }

    //アラームのセット、時間のデフォルトは5分でスヌーズ用
    private fun registerAlarm(str: String, minute: Int=5){
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.MINUTE, minute)

        val intent = Intent(this, AlarmBroadcastReceiver::class.java)
        intent.type = str
        val pending = PendingIntent.getBroadcast(this,0,intent,0)
        val am : AlarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        am.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pending)
    }

    //アラームのリセット
    private fun unregisterAlarm(str: String){
        val intent = Intent(this, AlarmBroadcastReceiver::class.java)
        intent.type = str
        val pending = PendingIntent.getBroadcast(this,0,intent,0)
        val am : AlarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        pending.cancel()
        am.cancel(pending)
    }
}
