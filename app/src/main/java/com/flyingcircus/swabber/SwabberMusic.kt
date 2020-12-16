package com.flyingcircus.swabber

import android.app.Service
import android.content.Context
import android.content.Intent

class SwabberMusic(appContext: Context) {
    val context = appContext
    companion object {
        // Public Variables
//        val swabberThemeService = SwabberMusicService("swabber_theme")
//        lateinit var thisContext: Context

        fun startTheme() {
            // TODO: create the musicPlayer object and start it
//            val bla = Intent(appContext, SwabberMusicService::class.java)
        }

        fun pauseTheme() {
            // TODO: pause the music player
        }

        fun resumeMusic() {
            // TODO: Only resume the music player
        }

        fun stopTheme() {
            // TODO: stop and release the music player (destroy the service)
        }
    }


}