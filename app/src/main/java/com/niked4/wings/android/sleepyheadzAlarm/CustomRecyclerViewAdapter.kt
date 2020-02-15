package com.niked4.wings.android.sleepyheadzAlarm

import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import io.realm.Realm
import io.realm.RealmResults

class CustomRecyclerViewAdapter(realmResults: RealmResults<AlarmData>): RecyclerView.Adapter<ViewHolder>() {
    private lateinit var realm: Realm
    private val rResults: RealmResults<AlarmData> = realmResults
    private val ma = MainActivity()
    private val tma = TimerMenuActivity()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.alarm_view, parent, false)
        val viewholder = ViewHolder(view)
        return viewholder
    }

    override fun getItemCount(): Int {
        return rResults.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //データベースをもとにRecyclerViewの生成
        val alarmData = rResults[position]
        val (startTime, endTime) = tma.arrangeNumericString(
            alarmData!!.startHour, alarmData.startMinute, alarmData.endHour, alarmData.endMinute)

        holder.startTimeText?.text = startTime
        holder.endTimeText?.text = endTime
        holder.alarmSwitch?.isChecked = alarmData.bool
        holder.alarmCountText?.text = "${alarmData.count}"
//        if(position % 2 == 0)holder.itemView.setBackgroundColor(Color.DKGRAY)

        //タップ時に設定メニューを表示
        holder.itemView.setOnClickListener{
            val intent = Intent(it.context, TimerMenuActivity::class.java)
            intent.putExtra("id", alarmData.id)
            it.context.startActivity(intent)
        }

        //スイッチ切り替え
        holder.alarmSwitch?.setOnCheckedChangeListener{_, isChecked ->
            realm = Realm.getDefaultInstance()
            realm.executeTransaction{
                alarmData.bool = isChecked
            }
            val context = holder.itemView.context
            //アラームのセットorリセット
            if(isChecked)
                ma.registerAlarmData(context, alarmData)
            else
                ma.unregisterAlarmData(context, alarmData)
            realm.close()
        }

        holder.itemView.setOnCreateContextMenuListener{menu, v, _ ->
            realm = Realm.getDefaultInstance()
            menu.add(R.string.bt_delete).setOnMenuItemClickListener {
                ma.unregisterAlarmData(v.context, alarmData)
                ma.deleteAlarmData(realm, alarmData)
                Toast.makeText(v.context, R.string.tv_alarm_delete, Toast.LENGTH_SHORT).show()
                notifyDataSetChanged()
                true
            }
            realm.close()
        }
    }
}