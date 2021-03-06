package com.niked4.wings.android.sleepyheadzAlarm

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.niked4.wings.android.sleepyheadzAlarm.AlarmMenuActivity.Companion.arrangeNumericString
import com.niked4.wings.android.sleepyheadzAlarm.MainActivity.Companion.deleteAlarmData
import com.niked4.wings.android.sleepyheadzAlarm.MainActivity.Companion.registerAlarmData
import com.niked4.wings.android.sleepyheadzAlarm.MainActivity.Companion.unregisterAlarmData
import com.niked4.wings.android.sleepyheadzAlarm.MainActivity.Companion.uriString2Title
import io.realm.Realm
import io.realm.RealmResults

class CustomRecyclerViewAdapter(realmResults: RealmResults<AlarmData>): RecyclerView.Adapter<ViewHolder>() {
    private lateinit var realm: Realm
    private val rResults: RealmResults<AlarmData> = realmResults

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
        val alarmData = rResults[position]!!
        val (startTime, endTime) = arrangeNumericString(
            alarmData.startHour, alarmData.startMinute, alarmData.endHour, alarmData.endMinute)
        val context = holder.itemView.context
        val mediaTitle = uriString2Title(context, alarmData.media)

        holder.startTimeText?.text = startTime
        holder.endTimeText?.text = endTime
        holder.alarmSwitch?.isChecked = alarmData.bool
        holder.alarmCountText?.text = "${alarmData.count}"
        holder.alarmMedia?.text = mediaTitle

        //タップ時に設定メニューを表示
        holder.itemView.setOnClickListener{
            val intent = Intent(it.context, AlarmMenuActivity::class.java)
            intent.putExtra("id", alarmData.id)
            it.context.startActivity(intent)
        }

        //スイッチ切り替え
        holder.alarmSwitch?.setOnCheckedChangeListener{_, isChecked ->
            realm = Realm.getDefaultInstance()
            realm.executeTransaction{
                alarmData.bool = isChecked
            }
            //アラームのセットorリセット
            if(isChecked)
                registerAlarmData(context, alarmData)
            else
                unregisterAlarmData(context, alarmData)
            realm.close()
        }

        //アラームのコンテキストメニュー
        holder.itemView.setOnCreateContextMenuListener{menu, view, _ ->
            realm = Realm.getDefaultInstance()
            menu.add(R.string.bt_delete).setOnMenuItemClickListener {
                unregisterAlarmData(view.context, alarmData)
                deleteAlarmData(realm, alarmData)
                Toast.makeText(view.context, R.string.tv_alarm_delete, Toast.LENGTH_SHORT).show()
                notifyDataSetChanged()
                true
            }
            realm.close()
        }
    }
}