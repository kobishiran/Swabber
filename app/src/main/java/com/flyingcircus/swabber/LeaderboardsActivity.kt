package com.flyingcircus.swabber

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_leaderboards.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

class LeaderboardsActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    var toNewActivity = false  // a flag that is activated if transitioning to another activity
    lateinit var scoresDb: ScoreDatabase
    var difficultyToView = Difficulty.EASY.difficultyName
    lateinit var highScoreTexts : Array<TextView>

    // Music service variables
    private var swabberThemeService: SwabberMusicService? = null  // nullable for the case that the service was destroyed and created again
    private var themeServiceBound: Boolean = false

    // Create a service connection object
    private val themeConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as SwabberMusicService.SwabberMusicBinder  // cast the IBinder to the SwabberMusicBinder Class
            swabberThemeService = binder.getService()  // get the service instance
            themeServiceBound = true
            println("LEADERBOARDS: BIND TO SERVICE")
            // initialize music button according to music mute state
            if (swabberThemeService != null) {
                when (swabberThemeService?.musicMuted!!) {
                    true -> buttonMusicLeaderboards.setImageResource(R.drawable.music_off)
                    else -> buttonMusicLeaderboards.setImageResource(R.drawable.music_on)
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            themeServiceBound = false
            swabberThemeService = null
            println("LEADERBOARDS: UNBIND FROM SERVICE")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboards)

        // Set music button listener
        buttonMusicLeaderboards.setOnClickListener {
            if (themeServiceBound) {
                if (!swabberThemeService?.musicMuted!!) {
                    muteMusicButton()
                } else {
                    unmuteMusicButton()
                }
            }
        }
        
        highScoreTexts = arrayOf(topScore1_leaderboards, topScore2_leaderboards, topScore3_leaderboards, topScore4_leaderboards, topScore5_leaderboards)

        // Get database object. All database related actions must run in a coroutine
        runBlocking {
            scoresDb = GlobalScope.async { ScoreDatabase.getDatabase(applicationContext) }.await()
        }

        val high_scores_board: LinearLayout = findViewById(R.id.high_scores_board_leaderboards)

        // show high scores title (visible)
        high_scores_board.visibility = View.VISIBLE

        // Display the leaderboard
        displayHighScores(scoresDb, difficultyToView, highScoreTexts)

        val difficultySpinner: Spinner = findViewById(R.id.spinnerDifficulty)
        difficultySpinner.onItemSelectedListener = this
    }

    override fun onStart() {
        super.onStart()
        toNewActivity = false  // reset to initial value

        if (swabberThemeService == null) {  // if the service instance is null, bind to the service again
            println("Leaderboards SCREEN ONSTART: SERVICE IS NULL")
            // Start Background Music (bind to service)
            Intent(this, SwabberMusicService::class.java).putExtra("MusicFileName", "swabber_theme").also { intent ->
                bindService(intent, themeConnection, BIND_AUTO_CREATE)
            }
        }
        // if the music is paused, resume it (when returning from paused state). Make sure that the service instance is not null.
        if (swabberThemeService != null && !swabberThemeService?.isRunning!!) {
            println("Leaderboards SCREEN ONSTART: RESUMING MUSIC")
            swabberThemeService!!.resumeMusic()
        }
    }

    override fun onStop() {
        if (toNewActivity) {  // if going to another activity, unbind from the service
            themeServiceBound = false
            swabberThemeService = null
            println("Leaderboards SCREEN: UNBIND FROM SERVICE")
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
            println("Leaderboards SCREEN: UNBIND FROM SERVICE")
            unbindService(themeConnection)
        }
        super.onDestroy()
    }

    override fun onBackPressed() {
        toNewActivity = true
        super.onBackPressed()
    }

    private fun muteMusicButton() {
        // Update the music button to muted, and mute the music
        buttonMusicLeaderboards.setImageResource(R.drawable.music_off)
        swabberThemeService?.muteMusic()
    }

    private fun unmuteMusicButton() {
        // Update the music button to unmuted, and unmute the music
        buttonMusicLeaderboards.setImageResource(R.drawable.music_on)
        swabberThemeService?.unmuteMusic()
    }


    // Difficulty Spinner Functions:
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val selectedDifficulty = parent?.getItemAtPosition(position).toString()
        difficultyToView = when (selectedDifficulty) {
            "Outbreak" -> Difficulty.EASY.difficultyName
            "Epidemic" -> Difficulty.MEDIUM.difficultyName
            else -> Difficulty.HARD.difficultyName
        }

        // update the leaderboard title to the chosen difficulty
        text_high_scores_leaderboards.text = selectedDifficulty
        // Display the leaderboard
        displayHighScores(scoresDb, difficultyToView, highScoreTexts)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }
}