package com.anro.child

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.anro.child.service.ConnectionService
import com.anro.child.util.Actions
import com.anro.child.webrtc.WebRtcManager
import android.Manifest

class MainActivity : ComponentActivity() {

    private lateinit var mediaProjectionManager: MediaProjectionManager

    private val mediaPermissionLauncher =
    registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->

        val camera =
            permissions[Manifest.permission.CAMERA] ?: false

        val mic =
            permissions[Manifest.permission.RECORD_AUDIO] ?: false


        if (camera && mic) {

            Log.i(
                "ANRO",
                "Camera & Microphone permission granted"
            )

        } else {

            Log.i(
                "ANRO",
                "Camera/Microphone permission denied"
            )
        }

    }

    private val projectionLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->

            if (result.resultCode == RESULT_OK) {

                Log.i(
                    "ANRO",
                    "MediaProjection permission granted"
                )

                val intent = Intent(
                    Actions.ACTION_MEDIA_PROJECTION_GRANTED
                )

                intent.putExtra(
                    "resultCode",
                    result.resultCode
                )

                intent.putExtra(
                    "data",
                    result.data
                )

                LocalBroadcastManager
                    .getInstance(this)
                    .sendBroadcast(intent)

            } else {

                Log.i(
                    "ANRO",
                    "MediaProjection permission denied"
                )
            }
        }

    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(
            context: Context?,
            intent: Intent?
        ) {

            Log.i(
                "ANRO",
                "Received ACTION_REQUEST_SCREEN_CAPTURE"
            )

            projectionLauncher.launch(
                mediaProjectionManager.createScreenCaptureIntent()
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mediaPermissionLauncher.launch(
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
)

        WebRtcManager.initialize(this)

        mediaProjectionManager =
            getSystemService(
                MediaProjectionManager::class.java
            )

        LocalBroadcastManager
            .getInstance(this)
            .registerReceiver(
                receiver,
                IntentFilter(
                    Actions.ACTION_REQUEST_SCREEN_CAPTURE
                )
            )

        val serviceIntent =
            Intent(
                this,
                ConnectionService::class.java
            )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        setContent {
            Text("ANRO Child")
        }
    }

    override fun onDestroy() {

        LocalBroadcastManager
            .getInstance(this)
            .unregisterReceiver(receiver)

        super.onDestroy()
    }
}
