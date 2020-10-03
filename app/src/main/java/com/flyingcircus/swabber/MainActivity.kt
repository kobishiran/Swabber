package com.flyingcircus.swabber

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    // Initialise global variables
    val minesNum = 10
    val boardWidth = 10
    val boardHight = 10
    var gameBoard = Array(boardHight) { Array<Tile>(boardWidth) { Tile() } }
    var unknownCounter = boardHight * boardWidth
    var masksNum = minesNum
    //    var matMines = Array(boardHight) {Array(boardWidth) {0} }
    //    var matMask = Array(boardHight) {Array(boardWidth) {0} }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    fun initializeBoard() {
        // wipe board clean
        gameBoard = Array(boardHight) { Array<Tile>(boardWidth) { Tile() } }

        // reset counters
        masksNum = minesNum
        unknownCounter = boardHight * boardWidth

        // generate random mines
        val randomIndexes = (0 until boardWidth * boardHight).shuffled().take(minesNum)
        randomIndexes.forEach { index -> gameBoard[index / boardWidth][index % boardWidth].isBomb = true }
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

        // Check if the tile contains a mine
        if (gameBoard[row][col].isBomb) gameOver(false)

        // if not, expose the tile, and possibly it's neighbors
        gameBoard[row][col].isExposed = true
        unknownCounter--
        gameBoard[row][col].neighborMines = countNeighbors(row, col)

        // if number of neighbors is zero, expose all the neighbors too
        if (gameBoard[row][col].neighborMines == 0) {
            // TODO: add boundary conditions
            exposeTile(row + 1, col + 1)
            exposeTile(row + 1, col)
            exposeTile(row + 1, col - 1)
            exposeTile(row, col + 1)
            exposeTile(row, col - 1)
            exposeTile(row - 1, col + 1)
            exposeTile(row - 1, col)
            exposeTile(row - 1, col - 1)
        }

        // update the display of the tile
        updateDisplay(row, col)
    }

    private fun countNeighbors(row: Int, col: Int): Int {
        // TODO: add boundary conditions
        return (gameBoard[row + 1][col + 1].isBomb.toInt() +
                gameBoard[row + 1][col].isBomb.toInt() +
                gameBoard[row + 1][col - 1].isBomb.toInt() +
                gameBoard[row][col + 1].isBomb.toInt() +
                gameBoard[row][col - 1].isBomb.toInt() +
                gameBoard[row - 1][col + 1].isBomb.toInt() +
                gameBoard[row - 1][col].isBomb.toInt() +
                gameBoard[row - 1][col - 1].isBomb.toInt())
    }

    fun holdTile(row: Int, col: Int) {
        if (!gameBoard[row][col].isExposed) { // make sure the tile is not already exposed
            when (gameBoard[row][col].hasMask) {
                true -> {  // if already has mask, remove it and increment mask counter
                    gameBoard[row][col].hasMask = false
                    masksNum++
                }
                false -> {  // if not, put on a mask if masks are available or show error
                    if (masksNum > 0) {
                        gameBoard[row][col].hasMask = true
                        masksNum--
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
        TODO("Not yet implemented")
    }

    private fun gameOver(victory: Boolean) {
        TODO("Not yet implemented")
    }

    fun checkVictory() {
        if (unknownCounter == 0) gameOver(true)
    }

    // extension function to turn bool to int
    fun Boolean.toInt() = if (this) 1 else 0
}

class Tile() {
    var isBomb = false
    var hasMask = false
    var isExposed = false
    var neighborMines = -1
}