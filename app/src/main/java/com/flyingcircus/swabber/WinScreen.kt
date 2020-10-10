package com.flyingcircus.swabber

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_database_test.*
import kotlinx.android.synthetic.main.activity_win_screen.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

class WinScreen : AppCompatActivity() {
    lateinit var scoresDb: ScoreDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_win_screen)

        val difficulty = intent.getSerializableExtra("Difficulty") as Difficulty
        val score = intent.getSerializableExtra("Score") as Score

        // Get database object. All database related actions must run in a coroutine
        runBlocking {
            scoresDb = GlobalScope.async { ScoreDatabase.getDatabase(applicationContext) }.await()
        }

        yourScore.text = "YOUR SCORE IS: ${score.score}"

        // Insert a new score to the DB
        runBlocking {
            val newHighScoreFlag = insertScore(score, scoresDb)

            // Display the leaderboard
            displayHighScores(scoresDb, score.difficulty, arrayOf(topScore1, topScore2, topScore3, topScore4, topScore5))

            // TODO: if (newHighScoreFlag) displayNewHighScoreMassage
        }

        // Start same game
        buttonRetryWin.setOnClickListener() {
            startActivity(Intent(this, GameActivity::class.java).putExtra("Difficulty", difficulty))
        }

        // Start new game
        buttonReturnHomeScreenWin.setOnClickListener() {
            startActivity(Intent(this, HomeScreen::class.java))
        }

        // Clear Leaderboards
        buttonClearLeaderboard.setOnClickListener {

            // Insert a new score to the DB
            runBlocking {
                val doneClearing = clearDatabase(scoresDb, score.difficulty)

                val newHighScoreFlag = insertScore(score, scoresDb)

                // Display the leaderboard
                displayHighScores(scoresDb, score.difficulty, arrayOf(topScore1, topScore2, topScore3, topScore4, topScore5))

                // TODO: if (newHighScoreFlag) displayNewHighScoreMassage
            }
        }
    }
}