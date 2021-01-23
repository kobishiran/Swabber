package com.flyingcircus.swabber

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SwabberMusicService : Service() {

    // Internal Parameters
    private var musicFile : String? = null
    private lateinit var swabberTheme : MediaPlayer
    var musicMuted = false
    var isRunning = false
    private var musicVolume = 1F
    private var isBound = false
    private val binder = SwabberMusicBinder()

    val isInitialized get() = this::swabberTheme.isInitialized

    // Binder Class
    inner class SwabberMusicBinder : Binder() {
        fun getService() : SwabberMusicService = this@SwabberMusicService
    }

    // Override Methods:
    override fun onBind(intent: Intent): IBinder {
        musicFile = intent.getStringExtra("MusicFileName")
        if (musicFile != null) {    // if a file name was indeed passed, start the music player with this file. If not, only bind to the service.
            swabberTheme = MediaPlayer.create(this, this.resources.getIdentifier(musicFile, "raw", this.packageName))
            swabberTheme.isLooping = true
            swabberTheme.setVolume(1F, 1F)
            swabberTheme.start()
            isRunning = true
        }
        isBound = true
        return binder
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        swabberTheme.stop()
        swabberTheme.release()
        isBound = false
        println("SWABBER MUSIC SERVICE DESTROYED")
        super.onDestroy()
    }

    // Public Methods
    fun muteMusic() {
        swabberTheme.setVolume(0F, 0F)
        musicMuted = true
    }

    fun unmuteMusic() {
        swabberTheme.setVolume(musicVolume, musicVolume)
        musicMuted = false
    }

    fun pauseMusic() {
        swabberTheme.pause()
        isRunning = false
    }

    fun resumeMusic() {
        swabberTheme.start()
        isRunning = true
    }

    fun musicFadeOut() {
        if (!musicMuted) {
            var tempVolume = musicVolume
            GlobalScope.launch {
                while (tempVolume > 0) {
                    tempVolume -= 0.01F
                    swabberTheme.setVolume(tempVolume, tempVolume)
                    delay(7)
                }
//                swabberTheme.pause()
//                swabberTheme.setVolume(musicVolume, musicVolume)  // reset volume to current level for the next time the music is played
//                swabberTheme.seekTo(0)  // reset music to start, so that next time it starts from the beginning
                stopSelf()
            }
        }
//        else {  // if the music is muted, simply stop the service
//            stopSelf()
//        }
    }

    fun musicFadeIn() {
        var tempVolume = 0F
        GlobalScope.launch {
            swabberTheme.start()
            while (tempVolume < musicVolume) {
                tempVolume += 0.01F
                swabberTheme.setVolume(tempVolume, tempVolume)
                delay(7)
            }
        }
    }
}
