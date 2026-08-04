package com.anro.child.repository


import android.content.Context
import android.os.Build
import android.util.Log
import com.anro.child.network.WebSocketClient
import com.anro.child.util.DeviceIdManager
import com.anro.child.webrtc.WebRtcManager
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener



class SignalingRepository(
    private val context: Context,
    serverUrl:String,
    private val onScreenRequest:()->Unit
) {


    private val socket =
        WebSocketClient(serverUrl)



    fun connect(){


        socket.connect(

            object:WebSocketListener(){


                override fun onOpen(
                    webSocket:WebSocket,
                    response:Response
                ){


                    Log.i(
                        "ANRO",
                        "WebSocket Connected"
                    )


                    val id =
                    DeviceIdManager
                        .getDeviceId(context)



                    send(
                        """
                        {
                        "type":"register",
                        "deviceId":"$id",
                        "role":"child",
                        "manufacturer":"${Build.MANUFACTURER}",
                        "model":"${Build.MODEL}",
                        "androidVersion":"${Build.VERSION.RELEASE}"
                        }
                        """.trimIndent()
                    )


                }





                override fun onMessage(
                    webSocket:WebSocket,
                    text:String
                ){


                    Log.i(
                        "ANRO",
                        "RX: $text"
                    )



                    when{


                        text.contains(
                            "screen_request"
                        )->{


                            Log.i(
                                "ANRO",
                                "Screen request"
                            )


                            WebRtcManager
                                .setSignaling(
                                    this@SignalingRepository
                                )



                            onScreenRequest()


                        }




                        text.contains(
                            "webrtc_answer"
                        )->{


                            Log.i(
                                "ANRO",
                                "Answer received"
                            )


                            WebRtcManager
                                .setRemoteAnswer(
                                    text
                                )


                        }




                        text.contains(
                            "ice_candidate"
                        )->{


                            Log.i(
                                "ANRO",
                                "ICE received"
                            )


                            WebRtcManager
                                .addIceCandidate(
                                    text
                                )


                        }


                    }


                }




                override fun onFailure(
                    webSocket:WebSocket,
                    t:Throwable,
                    response:Response?
                ){

                    Log.e(
                        "ANRO",
                        "WS Error",
                        t
                    )

                }


            }

        )


    }



    fun send(
        message:String
    ){

        socket.send(message)

    }



    fun disconnect(){

        socket.disconnect()

    }


}
