package com.flyingcircus.swabber

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.TextView


class MainActivity : AppCompatActivity() {

    // Initialise global variables
    val initialSickNum = 10
    val boardWidth = 10
    val boardHight = 10
    var gameBoard = Array(boardHight) { row -> Array<Person>(boardWidth) { col -> Person(row, col) } }
    var unknownCounter = boardHight * boardWidth
    var masksNum = initialSickNum
    val dayLengthMilli = 30_000L
    lateinit var countDownTimer: CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startTimer(dayLengthMilli)
    }

    override fun onDestroy() {
        countDownTimer.cancel()
        super.onDestroy()
    }

    fun initializeBoard() {
        // wipe board clean
        gameBoard = Array(boardHight) { row -> Array<Person>(boardWidth) { col -> Person(row, col) } }

        // reset counters
        masksNum = initialSickNum
        unknownCounter = boardHight * boardWidth

        // generate random sick people
        val randomIndexes = (0 until boardWidth * boardHight).shuffled().take(initialSickNum)
        randomIndexes.forEach { index -> gameBoard[index / boardWidth][index % boardWidth].isSick = true }
    }

    fun clickTile(row: Int, col: Int) {
        // Check if the tile is already exposed or has mask
        if (gameBoard[row][col].isExposed) {
            // TODO: show error: Already exposed!
        } else if (gameBoard[row][col].hasMask) {
            // TODO: show error: has a mask!
        } else {
            exposeTile(row, col)
            checkVictory()
        }

    }

    private fun exposeTile(row: Int, col: Int) {
        // if tile is already exposed, return
        if (gameBoard[row][col].isExposed) return

        // Check if the tile contains a sick person
        if (gameBoard[row][col].isSick) gameOver(false)

        // if not, expose the tile, and possibly it's neighbors
        gameBoard[row][col].isExposed = true
        unknownCounter--
        gameBoard[row][col].cantactNumber = countNeighbors(row, col)

        // if number of neighbors is zero, expose all the neighbors too
        if (gameBoard[row][col].cantactNumber == 0) {
            if (row + 1 < boardHight && col + 1 < boardWidth)   exposeTile(row + 1, col + 1)
            if (row + 1 < boardHight)                           exposeTile(row + 1, col)
            if (row + 1 < boardHight && col - 1 >= 0)           exposeTile(row + 1, col - 1)
            if (col + 1 < boardWidth)                           exposeTile(row, col + 1)
            if (col - 1 >= 0)                                   exposeTile(row, col - 1)
            if (row - 1 >= 0 && col + 1 < boardWidth)           exposeTile(row - 1, col + 1)
            if (row - 1 >= 0)                                   exposeTile(row - 1, col)
            if (row - 1 >= 0 && col - 1 >= 0)                   exposeTile(row - 1, col - 1)
        }

        // update the display of the tile
        updateDisplay(row, col)

        // change infectable status of self and all neighbors to false
        gameBoard[row][col].isInfectable = false
        if (row + 1 < boardHight && col + 1 < boardWidth)   gameBoard[row + 1][col + 1].isInfectable = false
        if (row + 1 < boardHight)                           gameBoard[row + 1][col].isInfectable = false
        if (row + 1 < boardHight && col - 1 >= 0)           gameBoard[row + 1][col - 1].isInfectable = false
        if (col + 1 < boardWidth)                           gameBoard[row][col + 1].isInfectable = false
        if (col - 1 >= 0)                                   gameBoard[row][col - 1].isInfectable = false
        if (row - 1 >= 0 && col + 1 < boardWidth)           gameBoard[row - 1][col + 1].isInfectable = false
        if (row - 1 >= 0)                                   gameBoard[row - 1][col].isInfectable = false
        if (row - 1 >= 0 && col - 1 >= 0)                   gameBoard[row - 1][col - 1].isInfectable = false
    }

    private fun countNeighbors(row: Int, col: Int): Int {
        var contactNumber = 0
        if (row + 1 < boardHight && col + 1 < boardWidth) contactNumber += gameBoard[row + 1][col + 1].isSick.toInt()
        if (row + 1 < boardHight) contactNumber                         += gameBoard[row + 1][col].isSick.toInt()
        if (row + 1 < boardHight && col - 1 >= 0) contactNumber         += gameBoard[row + 1][col - 1].isSick.toInt()
        if (col + 1 < boardWidth) contactNumber                         += gameBoard[row][col + 1].isSick.toInt()
        if (col - 1 >= 0) contactNumber                                 += gameBoard[row][col - 1].isSick.toInt()
        if (row - 1 >= 0 && col + 1 < boardWidth) contactNumber         += gameBoard[row - 1][col + 1].isSick.toInt()
        if (row - 1 >= 0) contactNumber                                 += gameBoard[row - 1][col].isSick.toInt()
        if (row - 1 >= 0 && col - 1 >= 0) contactNumber                 += gameBoard[row - 1][col - 1].isSick.toInt()
        return contactNumber
    }

    fun holdTile(row: Int, col: Int) {
        if (!gameBoard[row][col].isExposed) { // make sure the tile is not already exposed
            when (gameBoard[row][col].hasMask) {
                true -> {  // if already has mask, remove it and increment mask counter
                    gameBoard[row][col].hasMask = false
                    masksNum++
                    unknownCounter++
                }
                false -> {  // if not, put on a mask if masks are available or show error
                    if (masksNum > 0) {
                        gameBoard[row][col].hasMask = true
                        masksNum--
                        unknownCounter--
                    } else {
                        // TODO: Show error: Not enough masks!
                    }
                }
            }
            updateDisplay(row, col)

        } else {
            // TODO: Show error: Already exposed!
        }
    }

    private fun updateDisplay(row: Int, col: Int) {
        // TODO("Not yet implemented")
    }

    private fun gameOver(victory: Boolean) {
        // TODO("Not yet implemented")
    }

    fun checkVictory() {
        if (unknownCounter == 0) gameOver(true)
    }

    // extension function to turn bool to int
    fun Boolean.toInt() = if (this) 1 else 0

    fun startTimer(timeToCountInMili: Long) {
        countDownTimer = object : CountDownTimer(timeToCountInMili, 1000L) {

            // every second, update the timer textView
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 60_000
                val seconds = (millisUntilFinished / 1000) % 60
                val timer = findViewById<TextView>(R.id.timer)
                timer.text = "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
            }

            // when the timer finishes, start a night cycle, and then restart the timer
            override fun onFinish() {
                countDownTimer.cancel()
                // TODO: transition to night cycle
                startTimer(dayLengthMilli)
            }
        }
        countDownTimer.start()
    }
}

class Person(val row: Int, val col: Int) {
    var isSick = false
    var hasMask = false
    var isExposed = false
    var cantactNumber = -1
    var isInfectable = true
    var daysInfected = 0
    var isAlive = true
}