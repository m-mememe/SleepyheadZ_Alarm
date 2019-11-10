package com.niked4.wings.android.sleepyheadzAlarm

import android.app.AlertDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.activity_menu_timer.*
import java.util.*

class TimePickerDialogFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener{
    private var alert: AlertDialog.Builder? = null
    private var alarmTime = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        //デフォルト値を設定されている時間に、新規設定の場合は現在時刻をセット
        val which = arguments?.getString("which")
        val c = Calendar.getInstance()
        var hour = 0
        var minute = 0
        if(which == "start") {
            hour = arguments?.getInt("startHour") ?: c.get(Calendar.HOUR_OF_DAY)
            minute = arguments?.getInt("startMinute") ?: c.get(Calendar.MINUTE)
            val subhour = arguments?.getInt("endHour") ?: c.get(Calendar.HOUR_OF_DAY)
            val subminute = arguments?.getInt("endMinute") ?: c.get(Calendar.HOUR_OF_DAY)
            alarmTime = (subhour * 60 + subminute - (hour * 60 + minute) + 24 * 60) % (24 * 60)
        }
        else if(which == "end") {
            hour = arguments?.getInt("endHour") ?: c.get(Calendar.HOUR_OF_DAY)
            minute = arguments?.getInt("endMinute") ?: c.get(Calendar.MINUTE)
            val subhour = arguments?.getInt("startHour") ?: c.get(Calendar.HOUR_OF_DAY)
            val subminute = arguments?.getInt("startMinute") ?: c.get(Calendar.HOUR_OF_DAY)
            alarmTime = (hour * 60 + minute - (subhour * 60 + subminute) + 24 * 60) % (24 * 60)
        }
        return TimePickerDialog(activity as TimerMenuActivity?, android.R.style.Theme_Holo_Dialog, this, hour, minute, DateFormat.is24HourFormat(activity))
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int){
        //キーを元に開始時間の変更か終了時間の変更かを決定
        val timerMenuActivity = activity as TimerMenuActivity
        val which =arguments?.getString("which")
        val connect = arguments?.getBoolean("connect")
        //start選択時
        if(which == "start") {
            if (minute < 10) {
                timerMenuActivity.bt_start_time.text = " ${hourOfDay} : 0${minute} "
            } else {
                timerMenuActivity.bt_start_time.text = " ${hourOfDay} : ${minute} "
            }
            //コネクトスイッチがONだったらもう片方も修正
            if(connect == true){
                val endHour = ((hourOfDay * 60 + minute + alarmTime) / 60) % 24
                val endMinute = (hourOfDay * 60 + minute + alarmTime) % 60
                if(endMinute < 10){
                    timerMenuActivity.bt_end_time.text = " ${endHour} : 0${endMinute} "
                }
                else{
                    timerMenuActivity.bt_end_time.text = " ${endHour} : ${endMinute} "
                }
            }
        }
        //end選択時
        else if(which == "end") {
            if (minute < 10) {
                timerMenuActivity.bt_end_time.text = " ${hourOfDay} : 0${minute} "
            } else {
                timerMenuActivity.bt_end_time.text = " ${hourOfDay} : ${minute} "
            }
            //コネクトスイッチがONだったらもう片方も修正
            if(connect == true){
                val startHour = ((hourOfDay * 60 + minute - alarmTime + 24 * 60) / 60) % 24
                val startMinute = (hourOfDay * 60 + minute - alarmTime + 60) % 60
                if(startMinute < 10){
                    timerMenuActivity.bt_start_time.text = " ${startHour} : 0${startMinute} "
                }
                else{
                    timerMenuActivity.bt_start_time.text = " ${startHour} : ${startMinute} "
                }
            }
        }
    }
}