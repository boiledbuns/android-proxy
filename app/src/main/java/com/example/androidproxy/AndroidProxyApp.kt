package com.example.androidproxy

import android.app.Application
import timber.log.Timber

import timber.log.Timber.DebugTree

class AndroidProxyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        } else {
            Timber.plant(CrashReportTree())
        }
    }

    class CrashReportTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            TODO("Not yet implemented")
        }
    }
}