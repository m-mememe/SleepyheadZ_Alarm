package com.niked4.wings.android.sleepyheadzAlarm

import io.realm.DynamicRealm
import io.realm.FieldAttribute
import io.realm.RealmMigration

class AlarmDataMigration : RealmMigration{
    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        val realmSchema = realm.schema
        var oldVersion = oldVersion

        if(oldVersion == 0L){
            realmSchema.get("AlarmData")
                ?.removeField("alarmTime")
                ?.addField("media", String::class.java, FieldAttribute.REQUIRED)
            oldVersion++
        }
    }
}