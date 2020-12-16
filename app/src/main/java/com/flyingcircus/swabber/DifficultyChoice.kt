package com.flyingcircus.swabber

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_difficulty_choice.*

class DifficultyChoice : AppCompatActivity() {
    private var toNewActivity = false
    private var toGame = false

    // Music Service Variables
    private lateinit var swabberThemeService: SwabberMusicService
    private var themeServiceBound: Boolean = false

    // Create a service connection object
    private val themeConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as SwabberMusicService.SwabberMusicBinder  // cast the IBinder to the SwabberMusicBinder Class
            swabberThemeService = binder.getService()  // get the service object
            themeServiceBound = true
            println("DIFFICULTY CHOICE: BIND TO SERVICE")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            themeServiceBound = false
            println("DIFFICULTY CHOICE ON SERVICE DISCON: UNBIND TO SERVICE")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_difficulty_choice)

        // Set onClick Listeners
        buttonOutbreak.setOnClickListener {
            startGame(Difficulty.EASY)
        }
        buttonEpidemic.setOnClickListener {
            startGame(Difficulty.MEDIUM)
        }
        buttonPandemic.setOnClickListener {
            startGame(Difficulty.HARD)
        }
        buttonCustom.setOnClickListener {
            Toast.makeText(this, "סבלנות חחח עוד לא פיתחנו...\n תשחק בינתיים ב OUTBREAK", Toast.LENGTH_SHORT).show(); //difficulty = Difficulty.CUSTOM_GAME
            startGame(Difficulty.EASY)
        }

        // Set music button listener
        buttonMusic.setOnClickListener {
            if (swabberThemeService.musicMuted) {
                muteMusicButton()
            } else {
                unmuteMusicButton()
            }
        }
    }

    private fun startGame(difficulty: Difficulty) {
        toGame = true

        // Start the music service to make sure it is not destroyed when this activity is destroyed (the fadeout overlaps
        // with the next activity)
        startService(Intent(this, SwabberMusicService::class.java))

        // unbind the activity from the service to make sure the service is destroyed at the end of the fadeout
        themeServiceBound = false
        println("DIFFICULTY CHOICE START GAME: START SERVICE AND UNBIND")
        unbindService(themeConnection)

        // start the fadeout and transition to the game
        swabberThemeService.musicFadeOut()
        startActivity(Intent(this, GameActivity::class.java).putExtra("Difficulty", difficulty))
        finish()
    }

    override fun onStart() {
        super.onStart()
        toGame = false  // reset to initial value
        toNewActivity = false  // reset to initial value
        if (!themeServiceBound) {  // bind to the service if not bounded already
            Intent(this, SwabberMusicService::class.java).putExtra("MusicFileName", "swabber_theme").also { intent ->
                bindService(intent, themeConnection, Context.BIND_AUTO_CREATE)
            }
        }
        // if the music is paused, resume it (when returning from paused state). Make sure that the service instance is initialized.
        if (this::swabberThemeService.isInitialized && !swabberThemeService.isRunning) {
            swabberThemeService.resumeMusic()
        }
    }

    override fun onBackPressed() {
        toNewActivity = true
        super.onBackPressed()
    }

    override fun onPause() {
        if (!(toNewActivity or toGame)) swabberThemeService.pauseMusic()  // if leaving the app, pause the music
        super.onPause()
    }

    override fun onStop() {
        // If transitioning back to another activity, unbind from the service
        if (toNewActivity) {
            themeServiceBound = false
            println("DIFFICULTY CHOICE ON STOP: UNBIND TO SERVICE")
            unbindService(themeConnection)
        }
        super.onStop()
    }

    override fun onDestroy() {
        // Make sure that we unbind from the service when the activity is destroyed (maybe unnecessary, but just in case)
        if (themeServiceBound) {
            themeServiceBound = false
            println("DIFFICULTY CHOICE ON DESTROY: UNBIND TO SERVICE")
            unbindService(themeConnection)
        }
        super.onDestroy()
    }

    private fun muteMusicButton() {
        // Update the music button to muted, and mute the music
        buttonMusic.setImageResource(R.drawable.music_off)
        swabberThemeService.muteMusic()
    }

    private fun unmuteMusicButton() {
        // Update the music button to unmuted, and unmute the music
        buttonMusic.setImageResource(R.drawable.music_on)
        swabberThemeService.unmuteMusic()
    }
}