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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        //デフォルト値を今の時間にする
        val c = Calendar.getInstance()
        val hour = arguments?.getInt("hour") ?: c.get(Calendar.HOUR_OF_DAY)
        val minute = arguments?.getInt("minute") ?: c.get(Calendar.MINUTE)

        return TimePickerDialog(activity as TimerMenuActivity?, android.R.style.Theme_Holo_Dialog, this, hour, minute, DateFormat.is24HourFormat(activity))
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int){
        //キーを元に開始時間の変更か終了時間の変更かを決定
        val timerMenuActivity = activity as TimerMenuActivity
        if(arguments?.getString("which") == "start") {
            if (minute < 10) {
                timerMenuActivity.bt_start_time.text = " ${hourOfDay} : 0${minute} "
            } else {
                timerMenuActivity.bt_start_time.text = " ${hourOfDay} : ${minute} "
            }
        }
        else if(arguments?.getString("which") == "end") {
            if (minute < 10) {
                timerMenuActivity.bt_end_time.text = " ${hourOfDay} : 0${minute} "
            } else {
                timerMenuActivity.bt_end_time.text = " ${hourOfDay} : ${minute} "
            }
        }
    }
}