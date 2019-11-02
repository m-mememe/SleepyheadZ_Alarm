package com.websarva.wings.android.sleepyhead

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import android.util.Log
import java.io.IOException

class PlayMusicService : Service(), MediaPlayer.OnCompletionListener {
    private var _player: MediaPlayer? = null

    override fun onCreate() {
        _player = MediaPlayer()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        playMusic()
        return Service.START_NOT_STICKY
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

    private fun playMusic(){
        val mediaFileUriStr = "android.resource://${packageName}/${R.raw.bgm_maoudamashii_orchestra02}"
        val mediaFileUri = Uri.parse(mediaFileUriStr)
        try{
            //メディア再生
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
            //メディアを再生
            mp.start()
        }
    }

    private inner class PlayerCompletionListener: MediaPlayer.OnCompletionListener{
        override fun onCompletion(mp: MediaPlayer) {
            //自分自身を終了
            stopSelf()
            mp.start()
        }
    }
}
