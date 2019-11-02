package com.websarva.wings.android.sleepyhead

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuInflater
import android.view.View
import android.widget.ListView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.realm.Realm
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_main.*
import java.nio.file.Files.delete
import android.R.string.cancel
import android.content.Context.ALARM_SERVICE
import androidx.core.content.ContextCompat.getSystemService
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.widget.Toast
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

        //データベースを元にアラームセット
        registerAlarm()
    }

    override fun onStart(){
        super.onStart()

        //データベースからの読み出しとRecyclerViewの表示
        val realmResults = realm.where(AlarmData::class.java)
            .findAll()
            .sort("id", Sort.ASCENDING)
        layoutManager = LinearLayoutManager(this)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = layoutManager
        adapter = CustomRecyclerViewAdapter(realmResults)
        recyclerView.adapter = this.adapter
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

    //アラームのセット
    private fun registerAlarm(){
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.SECOND,5)

        val intent = Intent(this, AlarmBroadcastReceiver::class.java)
        val pending = PendingIntent.getBroadcast(this,0,intent,0)

        val am : AlarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        am.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pending)
        Toast.makeText(this,"RegisterAlarm",Toast.LENGTH_SHORT).show()
    }

    //アラームのリセット
    private fun unregisterAlarm(){
//        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        alarmManager.cancel(getPendingIntent())
//        pref.delete("alarm_time")
    }
}
