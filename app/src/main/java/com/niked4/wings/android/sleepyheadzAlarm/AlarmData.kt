package com.niked4.wings.android.sleepyheadzAlarm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class AlarmData : RealmObject(){
    @PrimaryKey
    var id : Long = 0
    var startHour : Int = 0
    var startMinute : Int = 0
    var endHour : Int = 0
    var endMinute : Int = 0
    var count : Int = 0
    var media : String = "default"
    var bool : Boolean = true
}