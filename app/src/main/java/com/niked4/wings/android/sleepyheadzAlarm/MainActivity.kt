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
import android.media.RingtoneManager
import android.net.Uri
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import io.realm.RealmConfiguration
import io.realm.kotlin.where
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var realm: Realm
    private lateinit var adapter: CustomRecyclerViewAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Realmの準備
        Realm.init(this)
        val realmConfig = RealmConfiguration.Builder()
            .schemaVersion(1L)
            .migration(AlarmDataMigration())
            .build()
        Realm.setDefaultConfiguration(realmConfig)
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
            alarmData?.let{
                if(it.bool) registerAlarmData(this, it)
            }
        }
    }

    override fun onStart(){
        super.onStart()
        //データベースからの読み出しとRecyclerViewの表示
        val realmResults = realm.where(AlarmData::class.java)
            .findAll()
            .sort("media", Sort.ASCENDING)
            .sort("count", Sort.ASCENDING)
            .sort("endMinute", Sort.ASCENDING)
            .sort("endHour", Sort.ASCENDING)
            .sort("startMinute", Sort.ASCENDING)
            .sort("startHour", Sort.ASCENDING)
        layoutManager = LinearLayoutManager(this)
        val alarmList = findViewById<RecyclerView>(R.id.recyclerView)
        val divider = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        alarmList.layoutManager = layoutManager
        adapter = CustomRecyclerViewAdapter(realmResults)
        alarmList.adapter = this.adapter
        alarmList.addItemDecoration(divider)
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.app_setting, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            //設定メニューに移動
            R.id.appSettingOption -> {
                val intent = Intent(this, SettingActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //アラームの追加
    fun onAddAlarmClick(view: View){
        val intent = Intent(this, TimerMenuActivity::class.java)
        startActivity(intent)
    }

    //5分タイマーのセットボタン
    fun onAddSnoozeClick(view: View){
        registerAlarm(this, "snooze", 5, "default")
        Toast.makeText(this, R.string.tv_snooze, Toast.LENGTH_SHORT).show()
    }

    //アラームの開始時間と差分時間を求める
    private fun getStartDelta(startHour: Int?, startMinute: Int?, endHour: Int?, endMinute: Int?, count: Int?): Pair<Int, Double>{
        //kotlinは負のmodをとっても負の値が返ってきてしまうため、onedayTimeを加えて調整する
        val onedayTime = 24 * 60
        var startTime = (startHour ?: 0) * 60 + (startMinute ?: 0)
        val alarmTime = ((endHour ?: 0) * 60 + (endMinute ?: 0) + onedayTime - startTime) % onedayTime

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        val timeNow = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        var deltaTime: Double = alarmTime.toDouble()

        startTime += onedayTime
        startTime -= timeNow
        startTime %= onedayTime
        if(count == 1)
            deltaTime = 0.0
        else
            deltaTime /= (count ?: 0) - 1
        return Pair(startTime, deltaTime)
    }

    //アラームのセット
    private fun registerAlarm(context: Context, str: String, minute: Int, media: String?){
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.MINUTE, minute)

        val intent = Intent(context, AlarmBroadcastReceiver::class.java)
        intent.type = str
        intent.putExtra("media", media)
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
        val startDelta = getStartDelta(alarmData?.startHour, alarmData?.startMinute, alarmData?.endHour, alarmData?.endMinute, alarmData?.count)
        val startTime = startDelta.first
        val deltaTime = startDelta.second
        for(j in 0 until (alarmData?.count ?: 0)) {
            val alarmId = "alarm:${alarmData?.id}.${j}"
            val addTime  = kotlin.math.floor(startTime + j * deltaTime).toInt() % (24 * 60)
            registerAlarm(context, alarmId, addTime, alarmData?.media)
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
            realm.where<AlarmData>()
                .equalTo("id", alarmDataId)
                ?.findFirst()
                ?.deleteFromRealm()
        }
    }

    //Uriをもとにメディアのタイトルを取得する
    fun uriString2Title(context: Context, uriString: String): String{
        val media = RingtoneManager.getRingtone(context, Uri.parse(uriString))
        return media.getTitle(context)
    }
}
