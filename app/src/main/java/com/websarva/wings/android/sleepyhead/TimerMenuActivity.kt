package com.websarva.wings.android.sleepyhead

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_menu_timer.*
import java.util.*

class TimerMenuActivity : AppCompatActivity() {
    private val tag = "AlarmParameter"
    private lateinit var realm: Realm
    private val maxAlarmTime = 180
    private val minCount = 2
    private val maxCount = 31

    //アラームのパラメータ
    private var _startHour = 0
    private var _startMinute = 0
    private var _endHour = 0
    private var _endMinute = 0
    private var _alarmTime = 20
    private var _count = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_timer)

        //Realm（データベース）のインスタンス取得
        realm = Realm.getDefaultInstance()

        //アラームの新規作成か設定変更かで初期値を変更する
        val alarmDataId = intent.getLongExtra("id", 0L)
        if (alarmDataId > 0L) {
            //idが1以上 = データが存在する場合（設定変更）
            val alarmData = realm.where<AlarmData>().equalTo("id", alarmDataId).findFirst()
            _startHour = alarmData?.startHour ?: 0
            _startMinute = alarmData?.startMinute ?: 0
            _endHour = alarmData?.endHour ?: 0
            _endMinute = alarmData?.endMinute ?: 0
            _alarmTime = alarmData?.alarmTime ?: 20
            _count = alarmData?.count ?: 5
            findViewById<Button>(R.id.bt_delete).visibility = View.VISIBLE
        } else {
            //時間の取得と初期時間設定（新規作成）
            val c = Calendar.getInstance()
            _startHour = c.get(Calendar.HOUR_OF_DAY)
            _startMinute = c.get(Calendar.MINUTE)
            _endHour = c.get(Calendar.HOUR_OF_DAY)
            _endMinute = c.get(Calendar.MINUTE)
            findViewById<Button>(R.id.bt_delete).visibility = View.INVISIBLE
        }
        //EndTimeの更新と描画
        val btStartTime = findViewById<Button>(R.id.bt_start_time)
        val btEndTime = findViewById<Button>(R.id.bt_end_time)
        if(_startMinute < 10){
            btStartTime.text = " ${_startHour} : 0${_startMinute} "
        }
        else{
            btStartTime.text = " ${_startHour} : ${_startMinute} "
        }
        if(_endMinute < 10) {
            btEndTime.text = " ${_endHour} : 0${_endMinute} "
        }
        else{
            btEndTime.text = " ${_endHour} : ${_endMinute} "
        }
        renderCount()
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    //ボタン設定
    //開始時間（時間）を変更
    fun setStartTime(view: View) {
        val fragment = TimePickerDialogFragment()
        val args = Bundle()
        val startTimeText = this.bt_start_time.text.toString()
        val startTimeArray = startTimeText.split(":")
        args.putString("which", "start")
        args.putInt("hour", startTimeArray[0].trim().toInt())
        args.putInt("minute", startTimeArray[1].trim().toInt())
        fragment.arguments = args
        fragment.show(supportFragmentManager, "timePicker")
    }

    //終了時間を変更
    fun setEndTime(view: View){
        val fragment = TimePickerDialogFragment()
        val args = Bundle()
        val endTimeText = this.bt_end_time.text.toString()
        val endTimeArray = endTimeText.split(":")
        args.putString("which", "end")
        args.putInt("hour", endTimeArray[0].trim().toInt())
        args.putInt("minute", endTimeArray[1].trim().toInt())
        fragment.arguments = args
        fragment.show(supportFragmentManager, "timePicker")
    }

    //アラームの回数を変更（1～30）
    fun alarmCountMinus3(view: View){
        _count = kotlin.math.max(minCount, _count - 3)
        renderCount()
    }

    fun alarmCountMinus1(view: View){
        _count = kotlin.math.max(minCount, _count - 1)
        renderCount()
    }

    fun alarmCountPlus1(view: View){
        _count = kotlin.math.min(maxCount, _count + 1)
        renderCount()
    }

    fun alarmCountPlus3(view: View){
        _count = kotlin.math.min(maxCount, _count + 3)
        renderCount()
    }

    //設定完了or設定中止をする場合
    //OKボタンをクリック
    fun onOKButtonClick(view: View){
        //ボタンテキストから時間を取得、パラメータを更新
        val startTimeText = this.bt_start_time.text.toString()
        val endTimeText = this.bt_end_time.text.toString()
        val startTimeArray = startTimeText.split(":")
        val endTimeArray = endTimeText.split(":")
        _startHour = startTimeArray[0].trim().toInt()
        _startMinute = startTimeArray[1].trim().toInt()
        _endHour = endTimeArray[0].trim().toInt()
        _endMinute = endTimeArray[1].trim().toInt()

        //1分あたり1アラームのため、カウントが（終了時間ー開始時間）よりも大きかったら小さくする
        val start = _startHour * 60 + _startMinute
        val end = _endHour * 60 + _endMinute
        val delta = if(end - start < 0) end - start + 24 * 60 else end - start
        _alarmTime = delta
        if(delta + 1 <= _count){
            _count = delta + 1
        }

        //差が3時間以下ならアラームセット
        if(delta <= maxAlarmTime) {
            val alarmDataId = intent.getLongExtra("id", 0L)
            when (alarmDataId) {
                0L -> {
                    //データベースにアラームのデータを追加
                    realm.executeTransaction {
                        val maxId = realm.where<AlarmData>().max("id")
                        val nextId = (maxId?.toLong() ?: 0L) + 1L
                        val alarmData = realm.createObject<AlarmData>(nextId)
                        alarmData.startHour = _startHour
                        alarmData.startMinute = _startMinute
                        alarmData.endHour = _endHour
                        alarmData.endMinute = _endMinute
                        alarmData.alarmTime = _alarmTime
                        alarmData.count = _count
                        alarmData.bool = true
                    }
                }
                else -> {
                    //アラームがセット済みならキャンセルをする
                    for(i in 0 until _count) {
                        val alarmId = "alarm:${alarmDataId}.${i}"
                        unregisterAlarm(alarmId)
                    }
                    //データベースのアラームのデータを編集
                    realm.executeTransaction {
                        val alarmData = realm.where<AlarmData>().equalTo("id", alarmDataId).findFirst()
                        alarmData?.startHour = _startHour
                        alarmData?.startMinute = _startMinute
                        alarmData?.endHour = _endHour
                        alarmData?.endMinute = _endMinute
                        alarmData?.alarmTime = _alarmTime
                        alarmData?.count = _count
                        alarmData?.bool = true
                    }
                }
            }

            //アラームをセット
            val startDelta = getStartDelta(_startHour, _startMinute, _alarmTime, _count)
            val startTime = startDelta.first
            val deltaTime = startDelta.second
            val maxId = realm.where<AlarmData>().max("id")
            for(i in 0 until _count) {
                val addTime = kotlin.math.floor(startTime + i * deltaTime).toInt()
                val alarmId = "alarm:${maxId}.${i}"
                registerAlarm(alarmId, addTime)
            }

            //トーストの表示
            Toast.makeText(applicationContext, R.string.tv_alarm_set, Toast.LENGTH_LONG).show()
            finish()
        }
        else{
            Toast.makeText(applicationContext, R.string.tv_set_error, Toast.LENGTH_LONG).show()
        }
    }

    //キャンセルボタンをクリック
    fun onBackButtonClick(view: View){
        //トーストの表示
        Toast.makeText(applicationContext, R.string.tv_cancel, Toast.LENGTH_LONG).show()
        finish()
    }

    //削除ボタンをクリック
    fun onDeleteButtonClick(view: View){
        //アラームのキャンセル
        val alarmDataId = intent.getLongExtra("id", 0L)
        for(i in 0 until _count) {
            val alarmId = "alarm:${alarmDataId}.${i}"
            unregisterAlarm(alarmId)
        }
        //データベースから削除
        realm.executeTransaction{
            val alarmData = realm.where<AlarmData>()
                .equalTo("id", alarmDataId)
                ?.findFirst()
                ?.deleteFromRealm()
        }
        //トーストの表示
        Toast.makeText(applicationContext, R.string.tv_alarm_delete, Toast.LENGTH_LONG).show()
        finish()
    }

    //アラームの開始時間と差分時間を求める
    private fun getStartDelta(startHour: Int, startMinute: Int, alarmTime: Int, count: Int): Pair<Int, Double>{
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        val timeNow = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        var startTime = startHour * 60 + startMinute - timeNow
        if (startTime < 0) startTime += 24 * 60
        var deltaTime: Double = alarmTime.toDouble()
        deltaTime /= count - 1
        return Pair(startTime, deltaTime)
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

    private fun renderCount(): Unit{
        val tvCount = findViewById<TextView>(R.id.tv_alarm_count)
        tvCount.text = _count.toString()
    }
}
