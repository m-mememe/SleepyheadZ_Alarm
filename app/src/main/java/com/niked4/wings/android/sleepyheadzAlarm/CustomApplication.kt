package com.niked4.wings.android.sleepyheadzAlarm

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration

class CustomApplication: Application(){

    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        val config = RealmConfiguration.Builder().build()
        Realm.setDefaultConfiguration(config)
    }
}