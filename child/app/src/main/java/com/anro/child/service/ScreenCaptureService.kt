package com.anro.child.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.anro.child.util.Actions
import com.anro.child.webrtc.WebRtcManager


class ScreenCaptureService : Service() {


    companion object {

        private const val CHANNEL_ID =
            "anro_screen_capture"

        private const val NOTIFICATION_ID =
            2
    }



    private val projectionReceiver =
        object : BroadcastReceiver() {


            override fun onReceive(
                context: Context?,
                intent: Intent?
            ) {


                if (intent == null)
                    return


                val resultCode =
                    intent.getIntExtra(
                        "resultCode",
                        0
                    )


                val data =
                    intent.getParcelableExtra<Intent>(
                        "data"
                    )
                    ?: return



WebRtcManager.startCapture(
    this@ScreenCaptureService,
    resultCode,
    data
)


WebRtcManager.createPeerConnection()


WebRtcManager.createOffer(
    "parent-web-001"
)

                Log.i(
                    "ANRO",
                    "ScreenCapturerAndroid started"
                )

            }

        }




    override fun onCreate() {

        super.onCreate()


        createNotificationChannel()


        val notification =
            Notification.Builder(
                this,
                CHANNEL_ID
            )
            .setContentTitle(
                "ANRO Screen Share"
            )
            .setContentText(
                "Sharing screen"
            )
            .setSmallIcon(
                android.R.drawable.ic_menu_camera
            )
            .build()



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            startForeground(
                NOTIFICATION_ID,
                notification
            )

        } else {

            startForeground(
                NOTIFICATION_ID,
                notification
            )

        }



        LocalBroadcastManager
            .getInstance(this)
            .registerReceiver(
                projectionReceiver,
                IntentFilter(
                    Actions.ACTION_MEDIA_PROJECTION_GRANTED
                )
            )



        Log.i(
            "ANRO",
            "ScreenCaptureService foreground started"
        )

    }




    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {


        val requestIntent =
            Intent(
                Actions.ACTION_REQUEST_SCREEN_CAPTURE
            )


        LocalBroadcastManager
            .getInstance(this)
            .sendBroadcast(
                requestIntent
            )


        return START_NOT_STICKY

    }




    override fun onDestroy() {


        LocalBroadcastManager
            .getInstance(this)
            .unregisterReceiver(
                projectionReceiver
            )


        super.onDestroy()

    }




    override fun onBind(
        intent: Intent?
    ): IBinder? = null




    private fun createNotificationChannel() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {


            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    "ANRO Screen Capture",
                    NotificationManager.IMPORTANCE_LOW
                )


            val manager =
                getSystemService(
                    NotificationManager::class.java
                )


            manager.createNotificationChannel(
                channel
            )

        }

    }

}
