package io.firekast.demo

import android.app.Application
import android.util.Log
import io.firekast.Firekast

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Firekast.setLogLevel(Log.VERBOSE)
        Firekast.initialize(this, "YOUR_CLIENT_KEY", "YOUR_APPLICATION_ID")
    }

}