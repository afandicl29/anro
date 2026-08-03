package com.anro.child.repository

import android.content.Context
import android.os.Build
import android.util.Log
import com.anro.child.network.WebSocketClient
import com.anro.child.util.DeviceIdManager
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class SignalingRepository(
    private val context: Context,
    serverUrl: String
) {

    private val socket = WebSocketClient(serverUrl)

    fun connect() {

        socket.connect(object : WebSocketListener() {

            override fun onOpen(
                webSocket: WebSocket,
                response: Response
            ) {

                Log.i("ANRO", "WebSocket Connected")

                val deviceId = DeviceIdManager.getDeviceId(context)

                send(
                    """
                    {
                        "type":"register",
                        "deviceId":"$deviceId",
                        "role":"child",
                        "manufacturer":"${Build.MANUFACTURER}",
                        "model":"${Build.MODEL}",
                        "androidVersion":"${Build.VERSION.RELEASE}"
                    }
                    """.trimIndent()
                )
            }

            override fun onMessage(
                webSocket: WebSocket,
                text: String
            ) {
                Log.i("ANRO", "RX: $text")
            }

            override fun onClosing(
                webSocket: WebSocket,
                code: Int,
                reason: String
            ) {
                Log.i("ANRO", "Closing")
            }

            override fun onClosed(
                webSocket: WebSocket,
                code: Int,
                reason: String
            ) {
                Log.i("ANRO", "Closed")
            }

            override fun onFailure(
                webSocket: WebSocket,
                t: Throwable,
                response: Response?
            ) {
                Log.e("ANRO", "WebSocket Error", t)
            }

        })
    }

    fun send(message: String) {
        socket.send(message)
    }

    fun disconnect() {
        socket.disconnect()
    }
}
