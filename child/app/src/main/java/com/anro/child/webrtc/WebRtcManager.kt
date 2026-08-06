package com.anro.child.webrtc

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.util.Log
import com.anro.child.repository.SignalingRepository
import org.json.JSONObject
import org.webrtc.*
import android.util.DisplayMetrics


object WebRtcManager {


private var peerConnection: PeerConnection? = null

private lateinit var eglBase: EglBase

private lateinit var peerConnectionFactory: PeerConnectionFactory

private lateinit var videoSource: VideoSource

private lateinit var videoTrack: VideoTrack

private lateinit var surfaceTextureHelper: SurfaceTextureHelper

private var screenCapturer: ScreenCapturerAndroid? = null

private var captureStarted = false

private var signalingRepository: SignalingRepository? = null
    fun initialize(context: Context) {


        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions
                .builder(context)
                .createInitializationOptions()
        )


        eglBase = EglBase.create()


        peerConnectionFactory =
            PeerConnectionFactory.builder()
                .setVideoEncoderFactory(
                    DefaultVideoEncoderFactory(
                        eglBase.eglBaseContext,
                        true,
                        true
                    )
                )
                .setVideoDecoderFactory(
                    DefaultVideoDecoderFactory(
                        eglBase.eglBaseContext
                    )
                )
                .createPeerConnectionFactory()



        Log.i(
            "ANRO",
            "WebRTC initialized"
        )

    }





    fun setSignaling(
        repository: SignalingRepository
    ){

        signalingRepository = repository

    }






    fun startCapture(
        context: Context,
        resultCode:Int,
        data:Intent
    ){
      if (captureStarted) {

        Log.i(
        "ANRO",
        "Screen capture already running"
        )

        return
        }
       
        surfaceTextureHelper =
            SurfaceTextureHelper.create(
                "ScreenCaptureThread",
                eglBase.eglBaseContext
            )



        videoSource =
            peerConnectionFactory
                .createVideoSource(true)



        screenCapturer =
            ScreenCapturerAndroid(
                data,
                object : MediaProjection.Callback(){

                    override fun onStop(){

                        Log.i(
                            "ANRO",
                            "MediaProjection stopped"
                        )

                    }

                }
            )



        screenCapturer!!.initialize(
            surfaceTextureHelper,
            context,
            videoSource.capturerObserver
        )

        val metrics = DisplayMetrics()

        @Suppress("DEPRECATION")
        val display =
            context.getSystemService(Context.WINDOW_SERVICE)
                as android.view.WindowManager

        @Suppress("DEPRECATION")
        display.defaultDisplay.getRealMetrics(metrics)

        val width = metrics.widthPixels
        val height = metrics.heightPixels

        Log.i(
            "ANRO",
            "Display Resolution : ${width}x${height}"
        )

        screenCapturer!!.startCapture(
            width,
            height,
            30
        )   

        captureStarted = true



        videoTrack =
            peerConnectionFactory
                .createVideoTrack(
                    "SCREEN_TRACK",
                    videoSource
                )



        videoTrack.setEnabled(true)



        videoTrack.addSink(
            object : VideoSink {

                override fun onFrame(
                    frame:VideoFrame
                ){

                    Log.i(
                        "ANRO",
                        "VIDEO FRAME ${frame.buffer.width}x${frame.buffer.height}"
                    )

                }

            }
        )



        Log.i(
            "ANRO",
            "Screen capture started"
        )

    }


    fun stopCapture() {

    try {

        screenCapturer?.stopCapture()

    } catch (e: Exception) {

        Log.e(
            "ANRO",
            "stopCapture failed",
            e
        )

    }

    screenCapturer?.dispose()
    screenCapturer = null

    surfaceTextureHelper.dispose()

    videoSource.dispose()

    captureStarted = false

    Log.i(
        "ANRO",
        "Screen capture stopped"
    )

}
fun closePeerConnection() {

    peerConnection?.close()
    peerConnection = null

    Log.i(
        "ANRO",
        "PeerConnection closed"
    )

}




    fun createPeerConnection(){
        Log.i(
                "ANRO",
                "========== CREATE PEER =========="
                )

        closePeerConnection()
        val rtcConfig =
            PeerConnection.RTCConfiguration(
                listOf(
                    PeerConnection.IceServer
                        .builder(
                            "stun:stun.l.google.com:19302"
                        )
                        .createIceServer()
                )
                
            )



        peerConnection =
            peerConnectionFactory
                .createPeerConnection(
                    rtcConfig,
                    object : PeerConnection.Observer {


                        override fun onIceCandidate(
                            candidate:IceCandidate
                        ){


                            val json =
                                JSONObject()



                            json.put(
                                "type",
                                "ice_candidate"
                            )


                            json.put(
                                "target",
                                "parent-web-001"
                            )



                            val c =
                                JSONObject()



                            c.put(
                                "sdpMid",
                                candidate.sdpMid
                            )


                            c.put(
                                "sdpMLineIndex",
                                candidate.sdpMLineIndex
                            )


                            c.put(
                                "candidate",
                                candidate.sdp
                            )



                            json.put(
                                "candidate",
                                c
                            )



                            signalingRepository
                                ?.send(
                                    json.toString()
                                )


                            Log.i(
                                "ANRO",
                                "ICE sent"
                            )

                        }





                        override fun onConnectionChange(
                            newState:PeerConnection.PeerConnectionState
                        ){

                            Log.i(
                                "ANRO",
                                "PC STATE $newState"
                            )

                        }





                        override fun onIceConnectionChange(
                            state:PeerConnection.IceConnectionState
                        ){

                            Log.i(
                                "ANRO",
                                "ICE STATE $state"
                            )

                        }





                        override fun onIceGatheringChange(
                            state:PeerConnection.IceGatheringState
                        ){}



                        override fun onSignalingChange(
                            state:PeerConnection.SignalingState
                        ){}



                        override fun onIceConnectionReceivingChange(
                            receiving:Boolean
                        ){}



                        override fun onIceCandidatesRemoved(
                            candidates:Array<out IceCandidate>
                        ){}



                        override fun onAddStream(
                            stream:MediaStream
                        ){}



                        override fun onRemoveStream(
                            stream:MediaStream
                        ){}



                        override fun onDataChannel(
                            dataChannel:DataChannel
                        ){}



                        override fun onRenegotiationNeeded(){}



                        override fun onAddTrack(
                            receiver:RtpReceiver,
                            streams:Array<out MediaStream>
                        ){}



                        override fun onTrack(
                            transceiver:RtpTransceiver
                        ){}



                        override fun onStandardizedIceConnectionChange(
                            newState:PeerConnection.IceConnectionState
                        ){}



                        override fun onSelectedCandidatePairChanged(
                            event:CandidatePairChangeEvent
                        ){}


                    }
                )!!
                Log.i(
                "ANRO",
                "PeerConnection CREATED"
                )



        peerConnection?.addTrack(
        videoTrack
        )


        Log.i(
        "ANRO",
        "VideoTrack ADDED"
        )

    }








    fun createOffer(
        target:String
    ){

        Log.i(
        "ANRO",
        "========== CREATE OFFER =========="
        )
        peerConnection?.createOffer(

            object:SdpObserver{


                override fun onCreateSuccess(
                    sdp:SessionDescription
                ){
                    Log.i(
                        "ANRO",
                        "LocalDescription SET"
                        )


                    peerConnection?.setLocalDescription(
                        this,
                        sdp
                        
                    )


                    val json =
                        JSONObject()



                    json.put(
                        "type",
                        "webrtc_offer"
                    )


                    json.put(
                        "target",
                        target
                    )


                    json.put(
                        "sdp",
                        sdp.description
                    )

                    Log.i(
                    "ANRO",
                    "Sending OFFER"
                    )

                    signalingRepository
                        ?.send(
                            json.toString()
                        )



                    Log.i(
                        "ANRO",
                        "Offer CREATED"
                    )

                }



                override fun onSetSuccess(){}



                override fun onCreateFailure(
                    error:String
                ){

                    Log.e(
                        "ANRO",
                        error
                    )

                }



                override fun onSetFailure(
                    error:String
                ){

                    Log.e(
                        "ANRO",
                        error
                    )

                }


            },

            MediaConstraints()

        )


    }








    fun setRemoteAnswer(
        text:String
    ){
        Log.i(
        "ANRO",
        "========== REMOTE ANSWER =========="
        )

        val json =
            JSONObject(text)



        peerConnection?.setRemoteDescription(

            object:SdpObserver{


                override fun onSetSuccess(){

                    Log.i(
                        "ANRO",
                        "Remote answer set"
                    )

                }


                override fun onSetFailure(
                    error:String
                ){

                    Log.e(
                        "ANRO",
                        error
                    )

                }


                override fun onCreateSuccess(
                    sdp:SessionDescription
                ){}



                override fun onCreateFailure(
                    error:String
                ){}


            },


            SessionDescription(
                SessionDescription.Type.ANSWER,
                json.getString("sdp")
            )

        )

    }








    fun addIceCandidate(
        text:String
    ){
        Log.i(
        "ANRO",
        "========== REMOTE ICE =========="
        )

        val json =
            JSONObject(text)



        val c =
            json.getJSONObject(
                "candidate"
            )



        peerConnection?.addIceCandidate(

            IceCandidate(

                c.getString("sdpMid"),

                c.getInt("sdpMLineIndex"),

                c.getString("candidate")

            )

        )


        Log.i(
            "ANRO",
            "ICE received"
        )

    }


}
