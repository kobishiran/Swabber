package com.flyingcircus.swabber

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_database_test.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

class DatabaseTest : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        lateinit var scoresDb: ScoreDatabase

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_database_test)

        // Get database object. All database related actions must run in a coroutine
        runBlocking {
            scoresDb = GlobalScope.async { ScoreDatabase.getDatabase(applicationContext) }.await()
        }

        val newScore = Score("MEDIUM", -1, "Noam", 10000 , "9/10/20")

        // Insert a new score to the DB
        runBlocking {
            val newHighScoreFlag = insertScore(newScore, scoresDb)

            // Display the leaderboard
            displayHighScores(scoresDb, "MEDIUM", arrayOf(highScore1, highScore2, highScore3, highScore4, highScore5))

            // TODO: if (newHighScoreFlag) displayNewHighScoreMassage
        }
    }
}