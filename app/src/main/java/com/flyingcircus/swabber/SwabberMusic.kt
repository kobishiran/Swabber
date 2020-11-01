package com.flyingcircus.swabber

import android.media.MediaPlayer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SwabberMusic {
    companion object {
        // Public Variables
        lateinit var swabberTheme : MediaPlayer
        var musicUnmuted = true
        var musicVolume = 1F


        // Public Methods
        fun muteMusic() {
            swabberTheme.setVolume(0F, 0F)
            musicUnmuted = false
        }

        fun unmuteMusic() {
            swabberTheme.setVolume(musicVolume, musicVolume)
            musicUnmuted = true
        }

        fun musicFadeOut() {
            if (musicUnmuted) {
                var tempVolume = musicVolume
                GlobalScope.launch {
                    while (tempVolume > 0) {
                        tempVolume -= 0.01F
                        swabberTheme.setVolume(tempVolume, tempVolume)
                        delay(7)
                    }
                    swabberTheme.pause()
                    swabberTheme.setVolume(musicVolume, musicVolume)  // reset volume to current level for the next time the music is played
                }
            }
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


}