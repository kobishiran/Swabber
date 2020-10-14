package com.flyingcircus.swabber

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_win_screen.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

class WinScreen : AppCompatActivity() {
    lateinit var scoresDb: ScoreDatabase
    lateinit var tempScores: Array<Score>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_win_screen)

        // underline high scores
        fun TextView.underline() {
            paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
        }

        val high_scores: TextView = findViewById(R.id.text_high_scores)
        high_scores.underline()

        val difficulty = intent.getSerializableExtra("Difficulty") as Difficulty
        val score = intent.getSerializableExtra("Score") as Score

        // Get database object. All database related actions must run in a coroutine
        runBlocking {
            scoresDb = GlobalScope.async { ScoreDatabase.getDatabase(applicationContext) }.await()
        }

        // print your score
        yourScore.text = "YOUR SCORE IS: ${score.score}"

        // prepare high scores board to be visible
        val high_scores_board: LinearLayout = findViewById(R.id.high_scores_board)
        var newHighScoreFlag = false

        var numberOfScores = 0
        // check if the score is a new high score
        runBlocking {
            tempScores = GlobalScope.async { scoresDb.scoresDao().getScoresLowerThen(score.difficulty, score.score) }.await()
            numberOfScores = GlobalScope.async { scoresDb.scoresDao().getTopScores(score.difficulty).size }.await()
        }

        if (tempScores.size > 0 || numberOfScores < ScoresDao.topScoresNum) {

            val builder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            builder.setTitle("Enter your name")
            val dialogLayout = inflater.inflate(R.layout.alert_dialog_enter_name, null)
            val editText = dialogLayout.findViewById<EditText>(R.id.editText)
            builder.setView(dialogLayout)
            builder.setPositiveButton("OK", DialogInterface.OnClickListener() { dialogInterface: DialogInterface, i: Int ->
                score.player_name = editText.text.toString()
                // Insert a new score to the DB
                runBlocking {
                    newHighScoreFlag = insertScore(score, scoresDb)
                    // TODO: if (newHighScoreFlag) displayNewHighScoreMassage
                }
            })
            builder.setNegativeButton(
                "Cancel",
                DialogInterface.OnClickListener() { dialogInterface: DialogInterface, i: Int -> })
            builder.setOnDismissListener(DialogInterface.OnDismissListener {
                // show high scores title (visible)
                high_scores_board.visibility = View.VISIBLE

                // Display the leaderboard
                displayHighScores(scoresDb, score.difficulty, arrayOf(topScore1, topScore2, topScore3, topScore4, topScore5))
                if (!newHighScoreFlag) Toast.makeText(this, "Your result wasn't saved", Toast.LENGTH_SHORT).show()
            })


            builder.show()
        } else {
            // show high scores title (visible)
            high_scores_board.visibility = View.VISIBLE

            // Display the leaderboard
            displayHighScores(scoresDb, score.difficulty, arrayOf(topScore1, topScore2, topScore3, topScore4, topScore5))
            Toast.makeText(this, "Not a new high score... Better luck next time!", Toast.LENGTH_SHORT).show()
        }



        // Start same game
        buttonRetryWin.setOnClickListener() {
            startActivity(Intent(this, GameActivity::class.java).putExtra("Difficulty", difficulty))
            finish()
        }

        // Start new game
        buttonReturnHomeScreenWin.setOnClickListener() {
            finish()
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