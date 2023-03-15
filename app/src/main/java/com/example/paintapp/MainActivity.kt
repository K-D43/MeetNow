package com.example.paintapp

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Surface
import android.view.SurfaceView
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.constraintlayout.widget.Constraints
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.paintapp.databinding.ActivityMainBinding
import io.agora.rtc2.*
import io.agora.rtc2.video.VideoCanvas
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val appId="78e7f2491b4942e9b962ad168cb5d9bd"
    private val channelName="DkayHub"
    private val token="007eJxTYLAuiHaYaJNc2rTBxTzi9qQZh2+3tpjO0A7eYxA15eyMZGsFBnOLVPM0IxNLwyQTSxOjVMskSzOjxBRDM4vkJNMUy6QUCVvBlIZARobnJj8ZGKEQxGdncMlOrPQoTWJgAABCJx8S"
    private val uid=0
    private var isJoined = false
    private var agoraEngine : RtcEngine?= null
    private var localSurfaceView : SurfaceView? = null
    private var remoteSurfaceView : SurfaceView? = null

    private val permission_ID = 12
    private val REQUESTEDPERMISSION=
        arrayOf(
        android.Manifest.permission.RECORD_AUDIO,
        android.Manifest.permission.CAMERA
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!checkSelfPermission()){
            ActivityCompat.requestPermissions(this,REQUESTEDPERMISSION,permission_ID)
        }

        setupVedioSDKEngine()

        binding.joinButton.setOnClickListener {
            joinCall()
        }
        binding.leaveButton.setOnClickListener {
            leaveCall()
        }

    }

    private fun leaveCall() {
        if (!isJoined){
            showMessage("Join our channel first")
        }else{
            agoraEngine!!.leaveChannel()
            showMessage("You left the channel")

            if (remoteSurfaceView!=null){
                remoteSurfaceView!!.visibility = GONE
            }
            if (localSurfaceView!=null){
                localSurfaceView!!.visibility=GONE
            }
        }
    }

    private fun joinCall() {
        if (checkSelfPermission()){
            val option = ChannelMediaOptions()
            option.channelProfile=Constants.CHANNEL_PROFILE_COMMUNICATION
            option.clientRoleType=Constants.CLIENT_ROLE_BROADCASTER
            setupLocalfaceView()
            localSurfaceView!!.visibility = VISIBLE
            agoraEngine!!.startPreview()
            agoraEngine!!.joinChannel(token,channelName,uid,option)
        }
        else{
            showMessage("Permission Not Granted")
        }
    }

    private fun checkSelfPermission():Boolean{
        return !(ContextCompat.checkSelfPermission(
            this,REQUESTEDPERMISSION[0]
        ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,REQUESTEDPERMISSION[1]
                )!=PackageManager.PERMISSION_GRANTED)
    }

    private fun showMessage(message:String){
        runOnUiThread{
            Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupVedioSDKEngine(){
        try {
            val config = RtcEngineConfig()
            config.mContext=baseContext
            config.mAppId=appId
            config.mEventHandler=mRtcEventHandler
            agoraEngine=RtcEngine.create(config)
            agoraEngine!!.enableVideo()
        }catch (e:Exception){
            e.message?.let { showMessage(it) }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        agoraEngine!!.stopPreview()
        agoraEngine!!.leaveChannel()

        Thread{
            RtcEngine.destroy()
            agoraEngine = null
        }.start()
    }
    
    private val mRtcEventHandler : IRtcEngineEventHandler= object : IRtcEngineEventHandler() {

        override fun onUserJoined(uid: Int, elapsed: Int) {
            showMessage("Remote User Joined $uid")
            runOnUiThread{
                setupRemotefaceView(uid)
            }
        }

        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            isJoined = true
            showMessage("Joined User $channel")
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            showMessage("User Offline")

            runOnUiThread {
                remoteSurfaceView!!.visibility=GONE
            }
        }


        
    }
    private fun setupRemotefaceView(uid:Int){
        remoteSurfaceView = SurfaceView(baseContext)
        remoteSurfaceView!!.setZOrderMediaOverlay(true)
        binding.RemoteUser.addView(remoteSurfaceView)

        agoraEngine!!.setupRemoteVideo(
            VideoCanvas(
                remoteSurfaceView,VideoCanvas.RENDER_MODE_FIT,uid
            )
        )
    }
    private fun setupLocalfaceView(){
        localSurfaceView = SurfaceView(baseContext)
        binding.localUser.addView(localSurfaceView)

        agoraEngine!!.setupLocalVideo(
            VideoCanvas(
                localSurfaceView,
                VideoCanvas.RENDER_MODE_FIT,
                0
            )
        )
    }
}