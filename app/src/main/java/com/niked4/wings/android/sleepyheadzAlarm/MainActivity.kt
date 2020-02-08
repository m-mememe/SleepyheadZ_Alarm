package com.niked4.wings.android.sleepyheadzAlarm

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
import android.content.Context
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
            unregisterAlarmData(this, alarmData)
        }

        //アラームをセットする
        for(i in 0 until column.toInt()){
            val alarmData = realmResults[i]
            if(alarmData?.bool ?: false){
                registerAlarmData(this, alarmData)
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
        registerAlarm(this, "snooze")
        Toast.makeText(this, R.string.tv_snooze, Toast.LENGTH_SHORT).show()
    }

    //アラームの開始時間と差分時間を求める
    private fun getStartDelta(startHour: Int?, startMinute: Int?, alarmTime: Int?, count: Int?): Pair<Int, Double>{
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        val timeNow = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        var startTime = (startHour ?: 0) * 60 + (startMinute ?: 0) - timeNow
        if (startTime < 0) startTime += 24 * 60
        var deltaTime: Double = alarmTime?.toDouble() ?: 0.0
        if(count == 1) deltaTime = 0.0
        else deltaTime /= (count ?: 0) - 1
        return Pair(startTime, deltaTime)
    }

    //アラームのセット、時間のデフォルトは5分でスヌーズ用
    private fun registerAlarm(context: Context, str: String, minute: Int=5){
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.MINUTE, minute)

        val intent = Intent(context, AlarmBroadcastReceiver::class.java)
        intent.type = str
        val pending = PendingIntent.getBroadcast(context,0,intent,0)
        val am : AlarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        am.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pending)
    }

    //アラームのリセット
     private fun unregisterAlarm(context: Context, str: String){
        val intent = Intent(context, AlarmBroadcastReceiver::class.java)
        intent.type = str
        val pending = PendingIntent.getBroadcast(context,0,intent,0)
        val am : AlarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        pending.cancel()
        am.cancel(pending)
    }

    //alarmData単位でのセット
    fun registerAlarmData(context: Context, alarmData: AlarmData?){
        val startDelta = getStartDelta(alarmData?.startHour, alarmData?.startMinute, alarmData?.alarmTime, alarmData?.count)
        val startTime = startDelta.first
        val deltaTime = startDelta.second
        for(j in 0 until (alarmData?.count ?: 0)) {
            val alarmId = "alarm:${alarmData?.id}.${j}"
            val addTime  = kotlin.math.floor(startTime + j * deltaTime).toInt()
            registerAlarm(context, alarmId, addTime)
        }
    }

    //alarmData単位でのリセット
    fun unregisterAlarmData(context: Context, alarmData: AlarmData?){
        for(j in 0 until (alarmData?.count ?: 0)) {
            val alarmId = "alarm:${alarmData?.id}.${j}"
            unregisterAlarm(context, alarmId)
        }
    }

    //alarmDataIdのデータをデータベースから消す
    fun deleteAlarmData(realm: Realm, alarmData: AlarmData?){
        val alarmDataId = alarmData?.id
        realm.executeTransaction{
            val alarmData = realm.where<AlarmData>()
                .equalTo("id", alarmDataId)
                ?.findFirst()
                ?.deleteFromRealm()
        }
    }
}
