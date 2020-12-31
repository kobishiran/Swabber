package com.flyingcircus.swabber

import android.widget.TextView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking


fun insertScore(score: Score, scoreDB: ScoreDatabase): Int {
    lateinit var topScores: Array<Score>

    // Get the top scores for the relevant difficulty
    runBlocking { topScores = GlobalScope.async { scoreDB.scoresDao().getTopScores(score.difficulty) }.await() }

    // Check if the leaderboard is empty, and if so, insert the score
    if (topScores.isEmpty()) {
        score.position = 1
        runBlocking { GlobalScope.async { scoreDB.scoresDao().insertNewHighScore(score) } }
        return score.position
    }

    // Check if the given score is a new high score. If so, insert it to the DB
    var tempTopScore: Score
    for (position in 0 until ScoresDao.topScoresNum) {  // note: position here is a *zero-based index*, while the table index is *one-based*!

        // if the leaderboard is not yet full and we got to the end, insert the new score at the bottom of the list
        if (position !in topScores.indices) {
            score.position = position + 1
            runBlocking { GlobalScope.async { scoreDB.scoresDao().insertNewHighScore(score) } }
            return score.position
        }

        // check if the score is higher than the existing scores
        tempTopScore = topScores[position]
        if (score.score > tempTopScore.score) {
            score.position = position + 1  // map from array index to score table index
            runBlocking { GlobalScope.async {
                scoreDB.scoresDao().insertNewHighScore(score)  // insert the new high score to the DB
                // push all the next high scores down by 1
                var pushDownPosition = position
                while (pushDownPosition in topScores.indices && pushDownPosition < ScoresDao.topScoresNum) {
                    tempTopScore = topScores[pushDownPosition]
                    tempTopScore.position = tempTopScore.position + 1  // push the score 1 down
                    scoreDB.scoresDao().insertNewHighScore(tempTopScore)
                    pushDownPosition++
                }
            }.join() }  // added join() to force the coroutine to end before updating the display
            return score.position  // indicate that a new high score has been added
        }
    }
    return -1 // indicate that no high score was added
}

fun displayHighScores(scoreDB: ScoreDatabase, difficulty: String, textViews: Array<TextView>) {
    lateinit var topScores: Array<Score>
    // Get the top scores for the relevant difficulty
    runBlocking { topScores = GlobalScope.async { scoreDB.scoresDao().getTopScores(difficulty) }.await() }

    // clear the current view
    for (position in 0 until ScoresDao.topScoresNum) {
        textViews[position].text = ""
    }

    if (topScores.isEmpty()) {
        textViews[0].text = "I'm empty!\nPlay some games to fix this!"
        return
    }

    var tempTopScore: Score
    // Display each score in a different text box
    for (position in 0 until kotlin.math.min(ScoresDao.topScoresNum, topScores.size)) {
        tempTopScore = topScores[position]
        if (tempTopScore.score == 0) return  // if there are no more scores to show, return
        textViews[position].text = ""
        textViews[position].text = "${tempTopScore.position}. ${tempTopScore.player_name}: ${tempTopScore.score},   ${tempTopScore.date}"
        if (position == topScores.size - 1) {
            var emptyScoresPosition = position + 1
            while (emptyScoresPosition < ScoresDao.topScoresNum) {
                textViews[emptyScoresPosition].text = ""
                emptyScoresPosition++
            }
            return
        }

    }

}

// Clear a certain difficulty list from the DB
fun clearDatabase(scoreDB: ScoreDatabase, difficulty: String): Boolean {
    runBlocking { GlobalScope.async { scoreDB.scoresDao().deleteAll(difficulty) }.join() }
    return true
}