package com.anro.child.webrtc

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.util.Log
import com.anro.child.repository.SignalingRepository
import org.json.JSONObject
import org.webrtc.*


object WebRtcManager {


    private lateinit var peerConnection: PeerConnection

    private lateinit var eglBase: EglBase

    private lateinit var peerConnectionFactory: PeerConnectionFactory

    private lateinit var videoSource: VideoSource

    private lateinit var videoTrack: VideoTrack

    private lateinit var surfaceTextureHelper: SurfaceTextureHelper


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


        surfaceTextureHelper =
            SurfaceTextureHelper.create(
                "ScreenCaptureThread",
                eglBase.eglBaseContext
            )



        videoSource =
            peerConnectionFactory
                .createVideoSource(false)



        val capturer =
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



        capturer.initialize(
            surfaceTextureHelper,
            context,
            videoSource.capturerObserver
        )



        capturer.startCapture(
            720,
            1280,
            30
        )



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








    fun createPeerConnection(){


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



        peerConnection.addTrack(
            videoTrack
        )



        Log.i(
            "ANRO",
            "Video track added"
        )

    }








    fun createOffer(
        target:String
    ){


        peerConnection.createOffer(

            object:SdpObserver{


                override fun onCreateSuccess(
                    sdp:SessionDescription
                ){


                    peerConnection.setLocalDescription(
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



                    signalingRepository
                        ?.send(
                            json.toString()
                        )



                    Log.i(
                        "ANRO",
                        "Offer sent"
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

        val json =
            JSONObject(text)



        peerConnection.setRemoteDescription(

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

        val json =
            JSONObject(text)



        val c =
            json.getJSONObject(
                "candidate"
            )



        peerConnection.addIceCandidate(

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
