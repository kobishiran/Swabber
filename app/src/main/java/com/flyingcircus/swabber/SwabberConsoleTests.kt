package com.flyingcircus.swabber

import android.os.CountDownTimer
import java.util.*
import kotlin.random.Random
import kotlin.system.exitProcess

var scanner = Scanner(System.`in`)
lateinit var countDownTimer: CountDownTimer

fun main(args: Array<String>) {
    initializeBoard()
    printBoard()
    startTimer(oneMinuteInMili)
    var row: Int
    var col: Int
    while (true) {
        row = getIndex("row")
        col = getIndex("column")
        when (getClickOrHold()) {
            false -> clickTile(row, col)
            true -> holdTile(row, col)
        }
        printBoard()
        println("Masks Counter: $masksNum, Unknown Counter: $unknownCounter")
    }
}

fun startTimer(timeToCountInMili: Long) {
    val countDownTimer = object : CountDownTimer(timeToCountInMili, 1000L) {
        override fun onTick(millisUntilFinished: Long) {
            println(millisUntilFinished / 1000)
        }

        override fun onFinish() {
            println("Night has come! A new day starts!")
            countDownTimer.cancel()
            startTimer(oneMinuteInMili)
        }
    }
    countDownTimer.start()
}

// Initialise global variables
val initialSickNum = 10
val boardWidth = 10
val boardHight = 10
var gameBoard = Array(boardHight) { row -> Array<Person>(boardWidth) { col -> Person(row, col) } }
var unknownCounter = boardHight * boardWidth
var masksNum = initialSickNum
val oneMinuteInMili = 60_000L
val infectionRadius = 2
val Pdeath = 0.1F
val Pinfect = 0.2F
//    var matMines = Array(boardHight) {Array(boardWidth) {0} }
//    var matMask = Array(boardHight) {Array(boardWidth) {0} }


fun initializeBoard() {
    // wipe board clean
    gameBoard = Array(boardHight) { row -> Array<Person>(boardWidth) { col -> Person(row, col) } }

    // reset counters
    masksNum = initialSickNum
    unknownCounter = boardHight * boardWidth

    // generate random mines
    val randomIndexes = (0 until boardWidth * boardHight).shuffled().take(initialSickNum)
    randomIndexes.forEach { index -> gameBoard[index / boardWidth][index % boardWidth].isSick = true }
}

fun clickTile(row: Int, col: Int) {
    // Check if the tile is already exposed or has mask
    if (gameBoard[row][col].isExposed) {
        println("Error: Already exposed!")
    } else if (gameBoard[row][col].hasMask) {
        println("Error: has a mask!")
    } else {
        exposeTile(row, col)
        checkVictory()
    }

}

fun exposeTile(row: Int, col: Int) {
    // if tile is already exposed, return
    if (gameBoard[row][col].isExposed) return

    // Check if the tile contains a mine
    if (gameBoard[row][col].isSick) gameOver(false)

    // if not, expose the tile, and possibly it's neighbors
    gameBoard[row][col].isExposed = true
    unknownCounter--
    gameBoard[row][col].cantactNumber = countNeighbors(row, col)

    // if number of neighbors is zero, expose all the neighbors too
    if (gameBoard[row][col].cantactNumber == 0) {
        if (row + 1 < boardHight && col + 1 < boardWidth) exposeTile(row + 1, col + 1)
        if (row + 1 < boardHight) exposeTile(row + 1, col)
        if (row + 1 < boardHight && col - 1 >= 0) exposeTile(row + 1, col - 1)
        if (col + 1 < boardWidth) exposeTile(row, col + 1)
        if (col - 1 >= 0) exposeTile(row, col - 1)
        if (row - 1 >= 0 && col + 1 < boardWidth) exposeTile(row - 1, col + 1)
        if (row - 1 >= 0) exposeTile(row - 1, col)
        if (row - 1 >= 0 && col - 1 >= 0) exposeTile(row - 1, col - 1)
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

fun countNeighbors(row: Int, col: Int): Int {
    var contactNumber = 0
    if (row + 1 < boardHight && col + 1 < boardWidth) contactNumber += gameBoard[row + 1][col + 1].isSick.toInt()
    if (row + 1 < boardHight) contactNumber += gameBoard[row + 1][col].isSick.toInt()
    if (row + 1 < boardHight && col - 1 >= 0) contactNumber += gameBoard[row + 1][col - 1].isSick.toInt()
    if (col + 1 < boardWidth) contactNumber += gameBoard[row][col + 1].isSick.toInt()
    if (col - 1 >= 0) contactNumber += gameBoard[row][col - 1].isSick.toInt()
    if (row - 1 >= 0 && col + 1 < boardWidth) contactNumber += gameBoard[row - 1][col + 1].isSick.toInt()
    if (row - 1 >= 0) contactNumber += gameBoard[row - 1][col].isSick.toInt()
    if (row - 1 >= 0 && col - 1 >= 0) contactNumber += gameBoard[row - 1][col - 1].isSick.toInt()
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
                    println("Error: Not enough masks!")
                }
            }
        }
        updateDisplay(row, col)

        // update infectable status to false
        // TODO: update infectable status to false
    } else {
        println("Error: Already exposed!")
    }
}

fun nightCycle() {
    // Display a night starting massage
    println("\n------- The Night has begun...! -------")

    // Start an infections cycle!
    infectionsCycle()

    // Display a night ending massage
    println("\n------- A new day begins! -------")
}

fun infectionsCycle() {
    var deadNum = 0
    var infectedNum = 0
    // find every sick person that has no mask
    gameBoard.forEach { arrayOfPersons ->
        arrayOfPersons.forEach { person ->
            if (person.isSick && !person.hasMask) {

                // the sick person might infect his neighbors, depending on their distance from him
                for (r in 1..infectionRadius) {
                    infectedNum += infectNeighbors(person.row, person.col, r)
                    // TODO: if infectedNum > N, break?
                }

                // also, the sick person might die, depending on how long he has been sick
                val heDies = Random.nextFloat() <= Pdeath * person.daysInfected // TODO: change death probability mechanism here
                if (heDies) {
                    person.isAlive = false
                    person.isExposed = true
                    person.isInfectable = false
                    deadNum++

                    updateDisplay(person.row, person.col)
                    // TODO: Update the graphic dead counter
                }
            }
        }
    }
}

fun infectNeighbors(row: Int, col: Int, r: Int): Int {
    var infected = 0
    for (rowDiff in r downTo -r) {
        if (row + rowDiff >= boardHight || row + rowDiff < 0) continue  // boundary condition

        // TODO: change infection probability mechanism here
        if (col + r in 0 until boardWidth && gameBoard[row + rowDiff][col + r].isInfectable) {gameBoard[row + rowDiff][col + r].isSick = Random.nextFloat() <= Pinfect / r ; infected++}
        if (col - r in 0 until boardWidth && gameBoard[row + rowDiff][col - r].isInfectable) {gameBoard[row + rowDiff][col - r].isSick = Random.nextFloat() <= Pinfect / r ; infected++}
        if (rowDiff == r || rowDiff == -r) for (colDiff in r-1 downTo (1 - r)) {
            if (col + colDiff >= boardWidth || col + colDiff < 0) continue  // boundary condition
            if (gameBoard[row + rowDiff][col + colDiff].isInfectable) {gameBoard[row + rowDiff][col + colDiff].isSick = Random.nextFloat() <= Pinfect / r ; infected++}
        }
    }
    return infected
}


//////////// Aux Functions ////////////

fun updateDisplay(row: Int, col: Int) {
    // TODO("Not yet implemented")
}

fun gameOver(victory: Boolean) {
    for (row in 0 until boardHight) {
        for (col in 0 until boardWidth) {
            gameBoard[row][col].cantactNumber = countNeighbors(row, col)
            gameBoard[row][col].isExposed = true
        }
    }
    printBoard()
    val massage = when (victory) {
        true -> "Congratulations! You won!"
        false -> "Loser! You lost!"
    }
    println(massage)
    exitProcess(1)
}

fun checkVictory() {
    if (unknownCounter == 0) gameOver(true)
}

// extension function to turn bool to int
fun Boolean.toInt() = if (this) 1 else 0


///////// Added functions for console interaction /////////
fun getIndex(dim: String): Int {
    println("Insert $dim:")
    return scanner.nextInt()
}

fun getClickOrHold(): Boolean {
    println("Click or Hold? 0 / 1")
    return scanner.nextInt() == 1
}

fun printBoard() {
    var currPerson: Person
    for (row in 0 until boardHight) {
        for (col in 0 until boardWidth) {
            currPerson = gameBoard[row][col]
            var toPrint = ""
            if (currPerson.isExposed) {
                toPrint = when (currPerson.isSick) {
                    true -> "X"
                    false -> currPerson.cantactNumber.toString()
                }
            } else toPrint = when (currPerson.hasMask) {
                true -> "#"
                false -> "-"
            }
            print(toPrint + "\t")
        }
        println("")
    }
}