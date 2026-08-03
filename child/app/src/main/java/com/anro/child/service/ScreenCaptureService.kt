package com.anro.child.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class ScreenCaptureService : Service() {


    override fun onCreate() {

        super.onCreate()

        Log.i(
            "ANRO",
            "ScreenCaptureService started"
        )

    }



    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {


        Log.i(
            "ANRO",
            "Screen capture request received"
        )


        return START_NOT_STICKY

    }



    override fun onBind(
        intent: Intent?
    ): IBinder? = null

}
