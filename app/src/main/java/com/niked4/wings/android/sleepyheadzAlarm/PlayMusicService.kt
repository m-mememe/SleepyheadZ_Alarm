package com.niked4.wings.android.sleepyheadzAlarm

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import android.util.Log
import java.io.IOException

class PlayMusicService : Service(), MediaPlayer.OnCompletionListener {
    private var _player: MediaPlayer? = null
    private var _am: AudioManager? = null

    override fun onCreate() {
        _player = MediaPlayer()
        _am = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val media = intent?.getStringExtra("media")
        playMusic(media)
        return START_NOT_STICKY
    }

    override fun onCompletion(mp: MediaPlayer?) {
        mp?.start()
    }

    override fun onDestroy() {
        _player?.let{
            if(it.isPlaying){
                it.stop()
            }
            it.release()
            _player = null
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    private fun playMusic(media: String?){
        val mediaFileUriStr =
            if(media == "default") "android.resource://${packageName}/${R.raw.bgm_maoudamashii_orchestra02}" else media
        val mediaFileUri = Uri.parse(mediaFileUriStr)
        _am?.let {
            val volume = it.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            it.setStreamVolume(AudioManager.STREAM_ALARM, volume, 0)
        }
        try{
            //アラーム再生
            _player?.setDataSource(applicationContext, mediaFileUri)
            _player?.setOnPreparedListener(PlayerPreparedListener())
            _player?.setOnCompletionListener(PlayerCompletionListener())
            _player?.prepareAsync()
        }
        catch (ex: IllegalArgumentException){
            Log.e("ServiceSample", "メディアプレーヤー準備時の例外発生")
        }
        catch (ex: IOException){
            Log.e("ServiceSample", "メディアプレーヤー準備時の例外発生")
        }
    }

    private inner class PlayerPreparedListener: MediaPlayer.OnPreparedListener{
        override fun onPrepared(mp: MediaPlayer){
            mp.start()
        }
    }

    private inner class PlayerCompletionListener: MediaPlayer.OnCompletionListener{
        override fun onCompletion(mp: MediaPlayer) {
            mp.start()
        }
    }
}
