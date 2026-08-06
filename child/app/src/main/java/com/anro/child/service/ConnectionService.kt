package com.anro.child.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.anro.child.repository.SignalingRepository
import com.anro.child.model.MediaMode
import com.anro.child.webrtc.WebRtcManager

class ConnectionService : Service() {

    companion object {

        private const val CHANNEL_ID = "anro_service"
        private const val NOTIFICATION_ID = 1

        private const val SERVER_WS =
            "ws://192.168.0.116:3000/ws"
    }


    private lateinit var signalingRepository: SignalingRepository


    override fun onCreate() {

        super.onCreate()


        createNotificationChannel()


        val notification = Notification.Builder(
            this,
            CHANNEL_ID
        )
            .setContentTitle("ANRO")
            .setContentText("Service is running")
            .setSmallIcon(
                android.R.drawable.stat_notify_sync
            )
            .build()


        startForeground(
            NOTIFICATION_ID,
            notification
        )


       signalingRepository = SignalingRepository(
            context = this,
            serverUrl = SERVER_WS,

            onScreenRequest = {
                startScreenCapture()
            },

            onMediaMode = { mode ->

                when (mode) {

                    MediaMode.SCREEN -> {

                        WebRtcManager.switchToScreen()

                    }

                   MediaMode.CAMERA -> {

                        WebRtcManager.switchToCamera(
                            this
                        )

                    }

                    MediaMode.MICROPHONE -> {
                        // nanti kita isi
                    }

                    MediaMode.STOP -> {

                        WebRtcManager.stopMedia()

                    }
                }

            }

        )

    }



    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {


        signalingRepository.connect()


        return START_STICKY

    }



    private fun startScreenCapture() {


        val intent = Intent(
            this,
            ScreenCaptureService::class.java
        )


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(intent)
        } else {
        startService(intent)
        }
    }



    override fun onDestroy() {


        signalingRepository.disconnect()


        super.onDestroy()

    }



    override fun onBind(
        intent: Intent?
    ): IBinder? = null




    private fun createNotificationChannel() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {


            val channel = NotificationChannel(
                CHANNEL_ID,
                "ANRO Service",
                NotificationManager.IMPORTANCE_LOW
            )


            val manager =
                getSystemService(
                    NotificationManager::class.java
                )


            manager.createNotificationChannel(channel)

        }

    }

}
