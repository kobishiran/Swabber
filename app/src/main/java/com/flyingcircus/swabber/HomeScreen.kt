package com.flyingcircus.swabber

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import kotlinx.android.synthetic.main.activity_difficulty_choice.*
import kotlinx.android.synthetic.main.activity_home_screen.*

class HomeScreen : AppCompatActivity() {
    var toNewActivity = false  // a flag that is activated if transitioning to another activity

    // Music service variables
    private var swabberThemeService: SwabberMusicService? = null  // nullable for the case that the service was destroyed and created again
    private var themeServiceBound: Boolean = false
    // Create a service connection object
    private val themeConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as SwabberMusicService.SwabberMusicBinder  // cast the IBinder to the SwabberMusicBinder Class
            swabberThemeService = binder.getService()  // get the service instance
            themeServiceBound = true
            println("HOME SCREEN: BIND TO SERVICE")
            // initialize music button according to music mute state
            if (swabberThemeService != null) {
                when (swabberThemeService?.musicMuted!!) {
                    true -> buttonMusicHome.setImageResource(R.drawable.music_off)
                    else -> buttonMusicHome.setImageResource(R.drawable.music_on)
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            themeServiceBound = false
            swabberThemeService = null
            println("HOME SCREEN: UNBIND FROM SERVICE")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)

        // Set music button listener
        buttonMusicHome.setOnClickListener {
            if (themeServiceBound) {
                if (!swabberThemeService?.musicMuted!!) {
                    muteMusicButton()
                } else {
                    unmuteMusicButton()
                }
            }
        }

        // Set Start button listener
        buttonStart.setOnClickListener {
//            val radioGroupMode: RadioGroup = findViewById(R.id.radioGroupMode)
//            val radioID = radioGroupMode.checkedRadioButtonId // if not check return id = -1
//            if (radioID != -1) {
//                val selectedButton: RadioButton = findViewById(radioID)
//
//                when (selectedButton.text) {
//                    getString(R.string.outbreak)    -> difficulty = Difficulty.EASY
//                    getString(R.string.epidemic)    -> difficulty = Difficulty.MEDIUM
//                    getString(R.string.pandemic)    -> difficulty = Difficulty.HARD
//                    getString(R.string.custom_game) -> {
//                        difficulty = Difficulty.EASY
//                        Toast.makeText(this, "סבלנות חחח עוד לא פיתחנו...\n תשחק בינתיים ב OUTBREAK", Toast.LENGTH_SHORT).show(); //difficulty = Difficulty.CUSTOM_GAME
//                    }
//                }
                toNewActivity = true
                startActivity(Intent(this, DifficultyChoice::class.java))
//            }
//            else Toast.makeText(this, "Please select game mode", Toast.LENGTH_SHORT).show()
        }

        buttonHighScores.setOnClickListener {
            toNewActivity = true
            startActivity(Intent(this, LeaderboardsActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        toNewActivity = false  // reset to initial value

        if (swabberThemeService == null) {  // if the service instance is null, bind to the service again
            println("HOME SCREEN ONSTART: SERVICE IS NULL")
            // Start Background Music (bind to service)
            Intent(this, SwabberMusicService::class.java).putExtra("MusicFileName", "swabber_theme").also { intent ->
                bindService(intent, themeConnection, Context.BIND_AUTO_CREATE)
            }
        }
        // if the music is paused, resume it (when returning from paused state). Make sure that the service instance is not null.
        if (swabberThemeService != null && !swabberThemeService?.isRunning!!) {
            println("HOME SCREEN ONSTART: RESUMING MUSIC")
            swabberThemeService!!.resumeMusic()
        }
    }

    override fun onStop() {
        if (toNewActivity) {  // if going to another activity, unbind from the service
            themeServiceBound = false
            swabberThemeService = null
            println("HOME SCREEN: UNBIND FROM SERVICE")
            unbindService(themeConnection)
        }
        super.onStop()
    }

    override fun onPause() {
        if (!toNewActivity) swabberThemeService?.pauseMusic()  // if not going to a new activity, pause the music
        super.onPause()
    }

    override fun onDestroy() {
        // Make sure that we unbind from the service if the activity is destroyed
        if (swabberThemeService != null) {
            themeServiceBound = false
            swabberThemeService = null
            println("HOME SCREEN: UNBIND FROM SERVICE")
            unbindService(themeConnection)
        }
        super.onDestroy()
    }

    private fun muteMusicButton() {
        // Update the music button to muted, and mute the music
        buttonMusicHome.setImageResource(R.drawable.music_off)
        swabberThemeService?.muteMusic()
    }

    private fun unmuteMusicButton() {
        // Update the music button to unmuted, and unmute the music
        buttonMusicHome.setImageResource(R.drawable.music_on)
        swabberThemeService?.unmuteMusic()
    }
}