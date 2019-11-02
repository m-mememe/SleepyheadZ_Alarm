package com.websarva.wings.android.sleepyhead

import android.app.AlertDialog
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*
import androidx.fragment.app.DialogFragment
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import java.util.*

class TimerMenuActivity : AppCompatActivity() {
    private val tag = "AlarmParameter"
    private lateinit var realm: Realm
    private val minAlarmTime = 5
    private val maxAlarmTime = 180
    private val minCount = 1
    private val maxCount = 30

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
            _startHour = alarmData?.startHour!!.toInt()
            _startMinute = alarmData.startMinute
            _alarmTime = alarmData.alarmTime
            _count = alarmData.count
            findViewById<Button>(R.id.bt_delete).visibility = View.VISIBLE
        } else {
            //時間の取得と初期時間設定（新規作成）
            val c = Calendar.getInstance()
            _startHour = c.get(Calendar.HOUR_OF_DAY)
            _startMinute = c.get(Calendar.MINUTE)
            findViewById<Button>(R.id.bt_delete).visibility = View.INVISIBLE
        }
        //EndTimeの更新と描画
        updateEndTime(_startHour, _startMinute, _alarmTime)
        renderTime()
        renderAlarmTime()
        renderCount()
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    //ボタン設定
    //開始時間（時間）を変更
    fun setStartTimeHour(view: View) {
        val btStartHourButton = findViewById<Button>(R.id.bt_start_time_hour)
        btStartHourButton.setOnClickListener {
            val myedit = EditText(this)
            myedit.inputType = InputType.TYPE_CLASS_NUMBER
            myedit.maxLines = 1
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("開始時間（時間）")
            dialog.setView(myedit)
            dialog.setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
                val userText = myedit.getText().toString()
                //値が0～23に収まっていたらセット
                if (userText.toInt() in 0..23) {
                    _startHour = userText.toInt()
                    updateEndTime(_startHour, _startMinute, _alarmTime)
                    renderTime()
                }
                //エラーのトーストを返す
                else {
                    Toast.makeText(applicationContext, "0から23の数字を入力してね！", Toast.LENGTH_LONG).show()
                }
            })
            dialog.setNegativeButton("キャンセル", null)
            dialog.show()
        }

//        btStartHourButton.setOnClickListener{
//            val dialog = DialogFragment()
//        }
//        class TimePickerDialogFragment: DialogFragment(), TimePickerDialog.OnTimeSetListener{
//
//            override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//                //デフォルト値を今の時間にする
//                val c = Calendar.getInstance()
//                val hour = c.get(Calendar.HOUR_OF_DAY)
//                val minute = c.get(Calendar.MINUTE)
//
//                return TimePickerDialog(activity as TimerMenuActivity?, android.R.style.Theme_Holo_Dialog, this, hour, minute, DateFormat.is24HourFormat(activity))
//            }
//
//            override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int){
//                Log.e("a","${hourOfDay} : ${minute}")
//            }
//        }
    }

    //終了時間を変更
//    fun setEndTime(view: View){
//    }

    //開始時間（分）を変更
    fun setStartTimeMinute(view: View){
        val btStartMinuteButton = findViewById<Button>(R.id.bt_start_time_minute)
        btStartMinuteButton.setOnClickListener {
            val myedit = EditText(this)
            myedit.inputType = InputType.TYPE_CLASS_NUMBER
            myedit.maxLines = 1
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("開始時間（分）")
            dialog.setView(myedit)
            dialog.setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
                val userText = myedit.getText().toString()
                //値が0～59に収まっていたらセット
                if(userText.toInt() in 0..59){
                    _startMinute = userText.toInt()
                    updateEndTime(_startHour, _startMinute, _alarmTime)
                    renderTime()
                }
                //エラーのトーストを返す
                else{
                    Toast.makeText(applicationContext, "0から59の数字を入力してね！", Toast.LENGTH_LONG).show()
                }
            })
            dialog.setNegativeButton("キャンセル", null)
            dialog.show()
        }
    }

    //アラームを鳴らす時間を変更（5～180）
    fun alarmTimeMinus20(view: View){
        _alarmTime = kotlin.math.max(minAlarmTime, _alarmTime - 20)
        updateEndTime(_startHour, _startMinute, _alarmTime)
        renderTime()
        renderAlarmTime()
    }

    fun alarmTimeMinus5(view: View){
        _alarmTime = kotlin.math.max(minAlarmTime, _alarmTime - 5)
        updateEndTime(_startHour, _startMinute, _alarmTime)
        renderTime()
        renderAlarmTime()
    }

    fun alarmTimePlus5(view: View){
        _alarmTime = kotlin.math.min(maxAlarmTime, _alarmTime + 5)
        updateEndTime(_startHour, _startMinute, _alarmTime)
        renderTime()
        renderAlarmTime()
    }

    fun alarmTimePlus20(view: View){
        _alarmTime = kotlin.math.min(maxAlarmTime, _alarmTime + 20)
        updateEndTime(_startHour, _startMinute, _alarmTime)
        renderTime()
        renderAlarmTime()
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
        //1分あたり1アラームのため、カウントが（終了時間ー開始時間）よりも大きかったら小さくする
        val start = _startHour * 60 + _startMinute
        val end = _endHour * 60 + _endMinute
        if(end - start + 1 < _count){
            //日をまたがない場合
            if(start < end) _count = end - start + 1
            //日をまたぐ場合
        }

        val alarmDataId = intent.getLongExtra("id", 0L)
        when(alarmDataId){
            0L -> {
                //データベースにアラームのデータを追加
                realm.executeTransaction {
                    val maxId = realm.where<AlarmData>().max("id")
                    val nextId = (maxId?.toLong() ?: 0L) + 1L
                    val alarmData = realm.createObject<AlarmData>(nextId)
                    alarmData.startHour = _startHour
                    alarmData.startMinute = _startMinute
                    alarmData.alarmTime = _alarmTime
                    alarmData.count = _count
                }
            }
            else -> {
                //データベースのアラームのデータを編集
                realm.executeTransaction {
                    val alarmData = realm.where<AlarmData>().equalTo("id", alarmDataId).findFirst()
                    alarmData?.startHour = _startHour
                    alarmData?.startMinute = _startMinute
                    alarmData?.alarmTime = _alarmTime
                    alarmData?.count = _count
                }
            }
        }
        //トーストの表示
        Toast.makeText(applicationContext, R.string.tv_alarm_set, Toast.LENGTH_LONG).show()
        finish()
    }

    //キャンセルボタンをクリック
    fun onBackButtonClick(view: View){
        //トーストの表示
        Toast.makeText(applicationContext, R.string.tv_cancel, Toast.LENGTH_LONG).show()
        finish()
    }

    //削除ボタンをクリック
    fun onDeleteButtonClick(view: View){
        //データベースから削除
        realm.executeTransaction{
            val alarmDataId = intent.getLongExtra("id", 0L)
            val alarmData = realm.where<AlarmData>()
                .equalTo("id", alarmDataId)
                ?.findFirst()
                ?.deleteFromRealm()
        }
        //トーストの表示
        Toast.makeText(applicationContext, R.string.tv_alarm_delete, Toast.LENGTH_LONG).show()
        finish()
    }

    //相対的に終了時間を変更する
    private fun updateEndTime(startHour: Int, startMinute: Int, alarmTime: Int): Unit{
        if(startMinute + alarmTime > 59){
            val adv = (startMinute + alarmTime) / 60
            _endHour = startHour + adv
            _endMinute = startMinute + alarmTime - 60 * adv
            if(_endHour > 23)_endHour -= 24
        }
        else{
            _endHour = startHour
            _endMinute = startMinute + alarmTime
        }
    }

    //レンダリング
    private fun renderTime(): Unit {
        val btStartButtonHour = findViewById<Button>(R.id.bt_start_time_hour)
        val btStartButtonMinute = findViewById<Button>(R.id.bt_start_time_minute)
        val tvEndHour = findViewById<TextView>(R.id.tv_end_hour)
        val tvEndMinute = findViewById<TextView>(R.id.tv_end_minute)
        btStartButtonHour.text = _startHour.toString()
        tvEndHour.text = _endHour.toString()
        if (_startMinute < 10) {
            btStartButtonMinute.text = "0${_startMinute}"
        } else {
            btStartButtonMinute.text = _startMinute.toString()
        }
        if (_endMinute < 10) {
            tvEndMinute.text = "0${_endMinute}"
        } else {
            tvEndMinute.text = _endMinute.toString()
        }
    }

    private fun renderAlarmTime(): Unit{
        val tvAlarmTime = findViewById<TextView>(R.id.tv_alarm_time)
        tvAlarmTime.text = _alarmTime.toString()
    }

    private fun renderCount(): Unit{
        val tvCount = findViewById<TextView>(R.id.tv_alarm_count)
        tvCount.text = _count.toString()
    }

    //時間が日をまたいでいるか判定
    private fun isDayChanges(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int): Boolean{
        val start = startHour * 60 + startMinute
        val end = endHour * 60 + endMinute
        if(start > end) return true
        return false
    }
}
