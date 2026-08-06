package com.anro.child.webrtc

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.util.Log
import com.anro.child.repository.SignalingRepository
import org.json.JSONObject
import org.webrtc.*
import android.util.DisplayMetrics
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraVideoCapturer


object WebRtcManager {


private var peerConnection: PeerConnection? = null

private var videoSender: RtpSender? = null

private lateinit var eglBase: EglBase

private lateinit var peerConnectionFactory: PeerConnectionFactory

private lateinit var screenVideoSource: VideoSource

private lateinit var screenVideoTrack: VideoTrack

private lateinit var cameraVideoSource: VideoSource

private lateinit var cameraVideoTrack: VideoTrack

private var cameraCapturer: CameraVideoCapturer? = null

private lateinit var surfaceTextureHelper: SurfaceTextureHelper

private lateinit var cameraSurfaceTextureHelper: SurfaceTextureHelper

private var screenCapturer: ScreenCapturerAndroid? = null

private var captureStarted = false

fun isCaptureStarted(): Boolean {
    return captureStarted
}

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



       screenVideoSource =
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
            screenVideoSource.capturerObserver
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



        screenVideoTrack =
            peerConnectionFactory
                .createVideoTrack(
                    "SCREEN_TRACK",
                    screenVideoSource
                )



        screenVideoTrack.setEnabled(true)



        screenVideoTrack.addSink(
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

    fun startCamera(context: Context) {

    if (cameraCapturer != null) {

        Log.i(
            "ANRO",
            "Camera already running"
        )

        return
    }


    val enumerator =
        Camera2Enumerator(context)


    var cameraName: String? = null


    for (name in enumerator.deviceNames) {

        if (enumerator.isFrontFacing(name)) {

            cameraName = name
            break
        }
    }


    if (cameraName == null) {

        for (name in enumerator.deviceNames) {

            if (enumerator.isBackFacing(name)) {

                cameraName = name
                break
            }
        }
    }


    if (cameraName == null) {

        Log.e(
            "ANRO",
            "Camera not found"
        )

        return
    }


    cameraCapturer =
        enumerator.createCapturer(
            cameraName,
            null
        )


    cameraSurfaceTextureHelper =
        SurfaceTextureHelper.create(
            "CameraThread",
            eglBase.eglBaseContext
        )


    cameraVideoSource =
        peerConnectionFactory
            .createVideoSource(false)


    cameraCapturer!!.initialize(
        cameraSurfaceTextureHelper,
        context,
        cameraVideoSource.capturerObserver
    )


    cameraCapturer!!.startCapture(
        1280,
        720,
        30
    )


    cameraVideoTrack =
        peerConnectionFactory
            .createVideoTrack(
                "CAMERA_TRACK",
                cameraVideoSource
            )


    cameraVideoTrack.setEnabled(true)


    Log.i(
        "ANRO",
        "Camera started"
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

    screenVideoSource.dispose()

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



      videoSender =
    peerConnection?.addTrack(
        screenVideoTrack
    )


    Log.i(
        "ANRO",
        "ScreenTrack ADDED"
    )


    }

    fun addCameraTrack() {

        if (!::cameraVideoTrack.isInitialized) {

            Log.e(
                "ANRO",
                "Camera track belum siap"
            )

            return
        }


        peerConnection?.addTrack(
            cameraVideoTrack
        )


        Log.i(
            "ANRO",
            "CameraTrack ADDED"
        )

    }

    fun switchToCamera(
        context: Context
    ) {

        startCamera(context)


        if (!::cameraVideoTrack.isInitialized) {

            Log.e(
                "ANRO",
                "Camera track not ready"
            )

            return
        }


        videoSender?.setTrack(
            cameraVideoTrack,
            true
        )


        Log.i(
            "ANRO",
            "Switched to camera"
        )

    }

    fun switchToScreen() {

        if (!::screenVideoTrack.isInitialized) {

            Log.e(
                "ANRO",
                "Screen track belum siap"
            )

            return
        }


        videoSender?.setTrack(
            screenVideoTrack,
            true
        )


        Log.i(
            "ANRO",
            "Switched to screen"
        )

    }


    fun stopMedia() {

        videoSender?.setTrack(
            null,
            false
        )


        Log.i(
            "ANRO",
            "Media stopped"
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
