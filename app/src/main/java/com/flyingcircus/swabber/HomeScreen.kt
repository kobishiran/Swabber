package com.flyingcircus.swabber

import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_home_screen.*

class HomeScreen : AppCompatActivity() {
    var restartMusic = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)

        lateinit var difficulty: Difficulty

        // Start Background Music
        SwabberMusic.swabberTheme = MediaPlayer.create(this@HomeScreen, R.raw.swabber_theme)
        SwabberMusic.swabberTheme.isLooping = true
        SwabberMusic.swabberTheme.setVolume(1F, 1F)
        SwabberMusic.swabberTheme.start()

        // Set music button listener
        buttonMusic.setOnClickListener {
            if (SwabberMusic.musicUnmuted) {
                muteMusic()
            } else {
                unmuteMusic()
            }
        }

        // Set Start button listener
        buttonStart.setOnClickListener {
            val radioGroupMode: RadioGroup = findViewById(R.id.radioGroupMode)
            val radioID = radioGroupMode.checkedRadioButtonId // if not check return id = -1
            if (radioID != -1) {
                val selectedButton: RadioButton = findViewById(radioID)

                when (selectedButton.text) {
                    getString(R.string.outbreak)    -> difficulty = Difficulty.EASY
                    getString(R.string.epidemic)    -> difficulty = Difficulty.MEDIUM
                    getString(R.string.pandemic)    -> difficulty = Difficulty.HARD
                    getString(R.string.custom_game) -> {
                        difficulty = Difficulty.EASY
                        Toast.makeText(this, "סבלנות חחח עוד לא פיתחנו...\n תשחק בינתיים ב OUTBREAK", Toast.LENGTH_SHORT).show(); //difficulty = Difficulty.CUSTOM_GAME
                    }
                }
                restartMusic = true
                startActivity(Intent(this, GameActivity::class.java).putExtra("Difficulty", difficulty))
            }
            else Toast.makeText(this, "Please select game mode", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        if (restartMusic) SwabberMusic.musicFadeOut()
        else SwabberMusic.swabberTheme.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        // if returning from the game, start the music from the beginning. Otherwise (if left the app for example), continue
        // from the same spot.
        if (restartMusic) { SwabberMusic.swabberTheme.seekTo(0) ; restartMusic = false } // reset music to beginning
        SwabberMusic.swabberTheme.start()
    }

    override fun onDestroy() {
        SwabberMusic.swabberTheme.release()
        super.onDestroy()
    }

    private fun muteMusic() {
        // Update the music button to muted, and mute the music
        buttonMusic.setImageResource(R.drawable.music_off)
        SwabberMusic.muteMusic()
    }

    private fun unmuteMusic() {
        // Update the music button to unmuted, and unmute the music
        buttonMusic.setImageResource(R.drawable.music_on)
        SwabberMusic.unmuteMusic()
    }
}