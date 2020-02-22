package com.niked4.wings.android.sleepyheadzAlarm

import android.view.View
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.alarm_view.view.*

class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
    var alarmSwitch: Switch? = null
    var startTimeText: TextView? = null
    var endTimeText: TextView? = null
    var alarmCountText: TextView? = null
    var alarmMedia: TextView? = null

    init{
        alarmSwitch = itemView.sw_on_off
        startTimeText = itemView.tv_start_time_para
        endTimeText = itemView.tv_end_time_para
        alarmCountText = itemView.tv_alarm_count_para
        alarmMedia = itemView.tv_media
    }
}