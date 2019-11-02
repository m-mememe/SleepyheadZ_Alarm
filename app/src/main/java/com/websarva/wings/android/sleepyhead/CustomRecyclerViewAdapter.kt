package com.websarva.wings.android.sleepyhead

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.realm.RealmResults

class CustomRecyclerViewAdapter(realmResults: RealmResults<AlarmData>): RecyclerView.Adapter<ViewHolder>() {
    private val rResults: RealmResults<AlarmData> = realmResults

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.alarm_setting, parent, false)
        val viewholder = ViewHolder(view)
        return viewholder
    }

    override fun getItemCount(): Int {
        return rResults.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //データベースをもとにRecyclerViewの生成
        val alarmData = rResults[position]
        val adv: Int = (alarmData!!.startMinute + alarmData.alarmTime) / 60
        val endHour: Int = if(alarmData.startHour + adv < 24)alarmData.startHour + adv else alarmData.startHour + adv - 24
        val endMinute: Int = alarmData.startMinute + alarmData.alarmTime - adv * 60
        if(alarmData.startMinute < 10) {
            holder.startTimeText?.text = "${alarmData.startHour} : 0${alarmData.startMinute}"
        }
        else {
            holder.startTimeText?.text = "${alarmData.startHour} : ${alarmData.startMinute}"
        }
        if(endMinute < 10) {
            holder.endTimeText?.text = "${endHour} : 0${endMinute}"
        }
        else {
            holder.endTimeText?.text = "${endHour} : ${endMinute}"
        }
        holder.alarmCountText?.text = "${alarmData.count}"
        if(position % 2 == 0)holder.itemView.setBackgroundColor(Color.DKGRAY)

        //タップ時に設定メニューを表示
        holder.itemView.setOnClickListener{
            val intent = Intent(it.context, TimerMenuActivity::class.java)
            intent.putExtra("id", alarmData.id)
            it.context.startActivity(intent)
        }
    }
}