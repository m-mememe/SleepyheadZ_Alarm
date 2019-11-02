package com.websarva.wings.android.sleepyhead

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.alarm_setting.view.*

class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
    var startTimeText: TextView? = null
    var endTimeText: TextView? = null
    var alarmCountText: TextView? = null

    init{
        startTimeText = itemView.tv_start_time_para
        endTimeText = itemView.tv_end_time_para
        alarmCountText = itemView.tv_alarm_count_para
    }
}