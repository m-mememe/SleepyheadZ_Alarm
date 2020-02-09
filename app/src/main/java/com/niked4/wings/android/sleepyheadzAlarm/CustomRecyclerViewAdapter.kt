package com.niked4.wings.android.sleepyheadzAlarm

import android.content.Intent
import android.graphics.Color
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
        if(alarmData!!.startMinute < 10) {
            holder.startTimeText?.text = "${alarmData.startHour} : 0${alarmData.startMinute}"
        }
        else {
            holder.startTimeText?.text = "${alarmData.startHour} : ${alarmData.startMinute}"
        }
        if(alarmData.endMinute < 10) {
            holder.endTimeText?.text = "${alarmData.endHour} : 0${alarmData.endMinute}"
        }
        else {
            holder.endTimeText?.text = "${alarmData.endHour} : ${alarmData.endMinute}"
        }
        holder.alarmSwitch?.isChecked = alarmData.bool
        holder.alarmCountText?.text = "${alarmData.count}"
        if(position % 2 == 0)holder.itemView.setBackgroundColor(Color.DKGRAY)

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
            if(isChecked){
                //セット
                ma.registerAlarmData(context, alarmData)
            }else{
                //リセット
                ma.unregisterAlarmData(context, alarmData)
            }
            realm.close()
        }

        holder.itemView.setOnCreateContextMenuListener{menu, v, _ ->
            realm = Realm.getDefaultInstance()
            menu.add(R.string.bt_delete).setOnMenuItemClickListener {
                Toast.makeText(v.context, position.toString(), Toast.LENGTH_LONG).show()
                ma.unregisterAlarmData(v.context, alarmData)
                ma.deleteAlarmData(realm, alarmData)
                Toast.makeText(v.context, R.string.tv_alarm_delete, Toast.LENGTH_SHORT).show()
                this.notifyDataSetChanged()
                true
            }
        }
    }
}