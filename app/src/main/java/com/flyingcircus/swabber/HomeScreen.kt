package com.flyingcircus.swabber

import android.app.ProgressDialog.show
import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_home_screen.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class HomeScreen : AppCompatActivity() {
    // TODO: Add musicPlayer and musicRunning to HomeScreen Companion Object?
    lateinit var musicPlayer : MediaPlayer
    var musicRunning = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)

        lateinit var difficulty: Difficulty

        // Start Background Music
        musicPlayer = MediaPlayer.create(this@HomeScreen, R.raw.swabber_theme)
        musicPlayer.isLooping = true
        musicPlayer.setVolume(100F, 100F)
        musicPlayer.start()

        // Set music button listener
        buttonMusic.setOnClickListener {
            if (musicRunning) {
                pauseMusic()
            } else {
                startMusic()
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
                startActivity(Intent(this, GameActivity::class.java).putExtra("Difficulty", difficulty))
            }
            else Toast.makeText(this, "Please select game mode", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        musicPlayer.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (musicRunning) startMusic()
    }

    override fun onDestroy() {
        musicPlayer.release()
        super.onDestroy()
    }

    fun pauseMusic() {
        buttonMusic.setImageResource(R.drawable.music_off)
        musicPlayer.pause()
        musicRunning = false
    }

    fun startMusic() {
        buttonMusic.setImageResource(R.drawable.music_on)
        musicPlayer.start()
        musicRunning = true
    }
}