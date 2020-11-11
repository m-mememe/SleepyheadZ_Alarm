package com.niked4.wings.android.sleepyheadzAlarm

import android.content.Intent
import android.media.RingtoneManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.preference.PreferenceManager
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_menu_alarm.*
import java.util.*

class AlarmMenuActivity : AppCompatActivity() {
    private lateinit var realm: Realm
    private val maxAlarmTime = 180
    private val ma = MainActivity()

    //アラームのパラメータ
    private var _startHour = 0
    private var _startMinute = 0
    private var _endHour = 0
    private var _endMinute = 0
    private var _count = 5
    private var _media = "default"

    //アラーム新規作成時の一回目の時間変更のみconnectをONにするためのフラグ
    private var _firstFlag = false

    //リクエストコード
    private val MEDIA_SELECT = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_alarm)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        realm = Realm.getDefaultInstance()

        //Preferencesの値で初期設定
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        _count = prefs.getString("firstCount", "5")!!.toInt()
        val timeDiff = prefs.getString("firstTimeDiff", "20")!!.toInt()

        //アラームの新規作成か設定変更かで初期値を変更する
        val alarmDataId = intent.getLongExtra("id", 0L)
        findViewById<Switch>(R.id.sw_connect).isChecked = true
        if (alarmDataId > 0L) {
            //idが1以上 = データが存在する場合（設定変更）
            val alarmData = realm.where<AlarmData>().equalTo("id", alarmDataId).findFirst()!!
            _startHour = alarmData.startHour
            _startMinute = alarmData.startMinute
            _endHour = alarmData.endHour
            _endMinute = alarmData.endMinute
            _count = alarmData.count
            _media = alarmData.media
            findViewById<Button>(R.id.bt_delete).visibility = View.VISIBLE
        } else {
            //時間の取得と初期時間設定（新規作成）
            val c = Calendar.getInstance()
            _startHour = c.get(Calendar.HOUR_OF_DAY)
            _startMinute = c.get(Calendar.MINUTE)
            c.add(Calendar.MINUTE, timeDiff)
            _endHour = c.get(Calendar.HOUR_OF_DAY)
            _endMinute = c.get(Calendar.MINUTE)
            findViewById<Button>(R.id.bt_delete).visibility = View.INVISIBLE
            _firstFlag = true
        }
        //EndTimeの更新と描画
        val (startTime, endTime) = arrangeNumericString(_startHour, _startMinute, _endHour, _endMinute)
        val mediaTitle = ma.uriString2Title(this, _media)
        val btStartTime = findViewById<Button>(R.id.bt_start_time)
        val btEndTime = findViewById<Button>(R.id.bt_end_time)
        val btSetCount = findViewById<Button>(R.id.bt_set_count)
        val btMedia = findViewById<Button>(R.id.bt_media)
        btStartTime.text = " $startTime "
        btEndTime.text = " $endTime "
        btSetCount.text = _count.toString()
        btMedia.text = mediaTitle
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //再生するメディア取得時
        if(requestCode == MEDIA_SELECT){
            if(resultCode == -1){
                val uri = data?.getParcelableExtra<Parcelable>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                if(uri.toString() == "null")
                    return
                _media = uri.toString()
                val mediaTitle = ma.uriString2Title(this, _media)
                val btMedia = findViewById<Button>(R.id.bt_media)
                btMedia.text = mediaTitle
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home)
            finish()
        return super.onOptionsItemSelected(item)
    }

    //ボタン設定
    //開始時間（時間）を変更
    fun setStartTime(view: View) {
        showMyTimePickerFragment("start")
    }

    //終了時間を変更
    fun setEndTime(view: View){
        showMyTimePickerFragment("end")
    }

    fun setCountButton(view: View){
        val fragment = NumberPickerDialogFragment()
        val args = Bundle()
        val btSetCount = findViewById<Button>(R.id.bt_set_count)
        args.putInt("count", btSetCount.text.toString().toInt())
        fragment.arguments = args
        fragment.show(supportFragmentManager, "NumberPickerDialogFragment")
    }

    fun selectMusicButton(view: View){
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
        startActivityForResult(intent, MEDIA_SELECT)
    }

    //設定完了or設定中止をする場合
    //OKボタンをクリック
    fun onOKButtonClick(view: View){
        //ボタンテキストから時間を取得、パラメータを更新
        val startTimeText = this.bt_start_time.text.toString()
        val endTimeText = this.bt_end_time.text.toString()
        val startTimeArray = startTimeText.split(":")
        val endTimeArray = endTimeText.split(":")
        val countText = this.bt_set_count.text.toString()
        _startHour = startTimeArray[0].trim().toInt()
        _startMinute = startTimeArray[1].trim().toInt()
        _endHour = endTimeArray[0].trim().toInt()
        _endMinute = endTimeArray[1].trim().toInt()
        _count = countText.toInt()

        //1分あたり1アラームのため、カウントが（終了時間ー開始時間）よりも大きかったら小さくする
        val start = _startHour * 60 + _startMinute
        val end = _endHour * 60 + _endMinute
        val delta = if(end - start < 0) end - start + 24 * 60 else end - start
        if(delta + 1 <= _count){
            _count = delta + 1
        }

        //差が3時間以下ならアラームセット
        if(delta <= maxAlarmTime) {
            var alarmDataId = intent.getLongExtra("id", 0L)
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
                        alarmData.count = _count
                        alarmData.media = _media
                        alarmData.bool = true
                        alarmDataId = nextId
                    }
                }
                else -> {
                    //アラームが作成済みならアラームのキャンセルをする
                    val alarmData = realm.where<AlarmData>().equalTo("id", alarmDataId).findFirst()
                    ma.unregisterAlarmData(this, alarmData)
                    //データベースのアラームのデータを編集
                    realm.executeTransaction {
                        val alarmData = realm.where<AlarmData>().equalTo("id", alarmDataId).findFirst()
                        alarmData?.startHour = _startHour
                        alarmData?.startMinute = _startMinute
                        alarmData?.endHour = _endHour
                        alarmData?.endMinute = _endMinute
                        alarmData?.count = _count
                        alarmData?.media = _media
                        alarmData?.bool = true
                    }
                }
            }

            //アラームをセット
            val alarmData = realm.where<AlarmData>().equalTo("id", alarmDataId).findFirst()
            ma.registerAlarmData(this, alarmData)
            Toast.makeText(applicationContext, R.string.tv_alarm_set, Toast.LENGTH_LONG).show()
            finish()
        }
        else{
            Toast.makeText(applicationContext, R.string.tv_set_error, Toast.LENGTH_LONG).show()
        }
    }

    //キャンセルボタンをクリック
    fun onBackButtonClick(view: View){
        Toast.makeText(applicationContext, R.string.tv_cancel, Toast.LENGTH_LONG).show()
        finish()
    }

    //削除ボタンをクリック
    fun onDeleteButtonClick(view: View){
        //アラームのキャンセルとデータベースからの削除
        val alarmDataId = intent.getLongExtra("id", 0L)
        val alarmData = realm.where<AlarmData>().equalTo("id", alarmDataId).findFirst()
        ma.unregisterAlarmData(this, alarmData)
        ma.deleteAlarmData(realm, alarmData)
        Toast.makeText(applicationContext, R.string.tv_alarm_delete, Toast.LENGTH_LONG).show()
        finish()
    }

    //onClickではないメソッド
    //時間のダイアログ呼び出し、whichにはstartかendを入れる
    private fun showMyTimePickerFragment(which: String){
        val fragment = MyTimePickerDialogFragment()
        val args = Bundle()
        val connectSwitch = this.sw_connect.isChecked
        val startTimeText = this.bt_start_time.text.toString()
        val endTimeText = this.bt_end_time.text.toString()
        val startTimeArray = startTimeText.split(":")
        val endTimeArray = endTimeText.split(":")
        args.putString("which", which)
        args.putBoolean("connect", connectSwitch)
        args.putInt("startHour", startTimeArray[0].trim().toInt())
        args.putInt("startMinute", startTimeArray[1].trim().toInt())
        args.putInt("endHour", endTimeArray[0].trim().toInt())
        args.putInt("endMinute", endTimeArray[1].trim().toInt())
        fragment.arguments = args
        fragment.show(supportFragmentManager, "timePicker")

        //アラーム新規作成時で初回時間変更後にconnectをOFFにする
        if(_firstFlag){
            findViewById<Switch>(R.id.sw_connect).isChecked = false
            _firstFlag = false
        }
    }

    //補助的な関数
    //見やすいように文字列を調節、半角スペース2つで数字1文字分
    fun arrangeNumericString(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int): Pair<String, String>{
        var startTime = ""
        var endTime = ""

        if(startHour < 10)
            startTime += "  "
        startTime += "$startHour : "
        if(startMinute < 10)
            startTime += "0"
        startTime += startMinute
        if(endHour < 10)
            endTime += "  "
        endTime += "$endHour : "
        if(endMinute < 10)
            endTime += "0"
        endTime += endMinute

        return Pair(startTime, endTime)
    }
}
