package com.niked4.wings.android.sleepyheadzAlarm


import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.activity_menu_alarm.*
import java.util.*

class MyTimePickerDialogFragment : DialogFragment() {
    private var alarmTime = 0
    private lateinit var hour: NumberPicker
    private lateinit var minute: NumberPicker

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(activity)
        val view = inflater.inflate(R.layout.fragment_my_time_picker_dialog, null)
        val c = Calendar.getInstance()
        val which = arguments?.getString("which")
        val startHour = arguments?.getInt("startHour") ?: c.get(Calendar.HOUR_OF_DAY)
        val startMinute = arguments?.getInt("startMinute") ?: c.get(Calendar.MINUTE)
        val endHour = arguments?.getInt("endHour") ?: c.get(Calendar.HOUR_OF_DAY)
        val endMinute = arguments?.getInt("endMinute") ?: c.get(Calendar.MINUTE)

        //時間の設定
        hour = view.findViewById(R.id.np_hour)
        minute = view.findViewById(R.id.np_minute)
        hour.minValue = 0
        hour.maxValue = 23
        minute.minValue = 0
        minute.maxValue = 59
        minute.setOnValueChangedListener(MinuteValueChangedListener())
        alarmTime = (endHour * 60 + endMinute - (startHour * 60 + startMinute) + 24 * 60) % (24 * 60)
        if(which == "start") {
            hour.value = startHour
            minute.value = startMinute
        }
        else if(which == "end") {
            hour.value = endHour
            minute.value = endMinute
            alarmTime = (endHour * 60 + endMinute - (startHour * 60 + startMinute) + 24 * 60) % (24 * 60)
        }

        //ボタンの設定
        val btMinus10 = view.findViewById<Button>(R.id.bt_minus10)
        val btMinus5 = view.findViewById<Button>(R.id.bt_minus5)
        val btPlus5 = view.findViewById<Button>(R.id.bt_plus5)
        val btPlus10 = view.findViewById<Button>(R.id.bt_plus10)
        btMinus10.setOnClickListener(OnMinus10ClickListener())
        btMinus5.setOnClickListener(OnMinus5ClickListener())
        btPlus5.setOnClickListener(OnPlus5ClickListener())
        btPlus10.setOnClickListener(OnPlus10ClickListener())

        //ダイアログ表示
        val builder = AlertDialog.Builder(activity, R.style.DialogTheme)
            .setTitle("${which?.capitalize()} time")
            .setView(view)
            .setPositiveButton(R.string.np_dialog_ok, DialogClickListener())
            .setNegativeButton(R.string.np_dialog_ng , DialogClickListener())
        val dialog = builder.create()
        return dialog
    }

    private inner class MinuteValueChangedListener : NumberPicker.OnValueChangeListener{
        override fun onValueChange(picker: NumberPicker?, oldVal: Int, newVal: Int) {
            //0分よりも時間を減らした場合
            if((oldVal == 0) and (newVal == 59)){
                hour.value -= 1
                if(hour.value == -1){
                    hour.value = 23
                }
            }
            //59分よりも時間を増やした場合
            else if((oldVal == 59) and (newVal == 0)){
                hour.value += 1
                if(hour.value == 24){
                    hour.value = 0
                }
            }
        }
    }

    private inner class OnMinus10ClickListener : View.OnClickListener{
        override fun onClick(v: View?) {
            //0分よりも時間を減らした場合は1時間戻す
            if(minute.value < 10){
                hour.value -= 1
            }
            minute.value -= 10
        }
    }

    private inner class OnMinus5ClickListener : View.OnClickListener{
        override fun onClick(v: View?) {
            //0分よりも時間を減らした場合は1時間戻す
            if(minute.value < 5){
                hour.value -= 1
            }
            minute.value -= 5
        }
    }

    private inner class OnPlus5ClickListener : View.OnClickListener{
        override fun onClick(v: View?) {
            //59分よりも時間を増やした場合は1時間進める
            if(minute.value >= 55){
                hour.value += 1
            }
            minute.value += 5
        }
    }

    private inner class OnPlus10ClickListener : View.OnClickListener{
        override fun onClick(v: View?) {
            //59分よりも時間を増やした場合は1時間進める
            if(minute.value >= 50){
                hour.value += 1
            }
            minute.value += 10
        }
    }

    private inner class DialogClickListener : DialogInterface.OnClickListener{
        override fun onClick(dialog: DialogInterface?, whichButton: Int) {
            when(whichButton){
                DialogInterface.BUTTON_POSITIVE ->{
                    //キーを元に開始時間の変更か終了時間の変更かを決定
                    val alarmMenuActivity = activity as AlarmMenuActivity
                    val which = arguments?.getString("which")
                    val connect = arguments?.getBoolean("connect")
                    val _hour = hour.value
                    val _minute = minute.value
                    //start選択時
                    if(which == "start") {
                        val endHour = ((_hour * 60 + _minute + alarmTime) / 60) % 24
                        val endMinute = (_hour * 60 + _minute + alarmTime) % 60
                        val (startTime, endTime) = alarmMenuActivity.arrangeNumericString(_hour, _minute, endHour, endMinute)
                        alarmMenuActivity.bt_start_time.text = " $startTime "
                        //コネクトスイッチがONだったらもう片方も修正
                        if(connect == true){
                            alarmMenuActivity.bt_end_time.text = " $endTime "
                        }
                    }
                    //end選択時
                    else if(which == "end") {
                        val startHour = ((_hour * 60 + _minute - alarmTime + 24 * 60) / 60) % 24
                        val startMinute = (_hour * 60 + _minute - alarmTime + 60) % 60
                        val (startTime, endTime) = alarmMenuActivity.arrangeNumericString(startHour, startMinute, _hour, _minute)
                        alarmMenuActivity.bt_end_time.text = " $endTime "
                        //コネクトスイッチがONだったらもう片方も修正
                        if(connect == true){
                            alarmMenuActivity.bt_start_time.text = " $startTime "
                        }
                    }
                }
            }
        }
    }
}
