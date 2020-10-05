package com.flyingcircus.swabber

import java.util.*
import kotlin.concurrent.timer
import kotlin.random.Random
import kotlin.system.exitProcess

var scanner = Scanner(System.`in`)
lateinit var countdownTimer: Timer

fun main(args: Array<String>) {
    initializeBoard()
    printBoard()
//    printExposedBoard()
    startTimer()
    var row: Int
    var col: Int
    while (true) {
        row = getIndex("row")
        col = getIndex("column")
        when (getClickOrHold()) {
            false -> clickTile(row, col)
            true -> holdTile(row, col)
        }
        println("Regular Board:")
        printBoard()
//        println("Exposed Board:")
//        printExposedBoard()
        println("Masks Counter: $masksNum, Unknown Counter: $unknownCounter")
    }
}

fun startTimer() {
    countdownTimer = timer("Day Counter", false, initialDelay = 0, 1000L) {
//        displayTime(timeLeftSecs)
        if (timeLeftSecs == 0) {
            nightCycle()
            timeLeftSecs = dayLengthInMillis.toInt() / 1000
//            displayTime(timeLeftSecs)
            timeLeftSecs--
        }
        else {
            timeLeftSecs--
        }
    }
}

fun displayTime(timeInSecs: Int) {
    val minutes = timeInSecs / 60
    val seconds = timeInSecs % 60
    println("${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}")
}

// Initialise global variables
val initialSickNum = 20
val boardWidth = 15
val boardHeight = 15
var gameBoard = Array(boardHeight) { row -> Array<Person>(boardWidth) { col -> Person(row, col) } }
var unknownCounter = boardHeight * boardWidth
var masksNum = initialSickNum * 5
val dayLengthInMillis = 15_000L
var timeLeftSecs = dayLengthInMillis.toInt() / 1000
val infectionRadius = 2
val Pdeath = 0.02F
val Pinfect = 0.1F
//    var matMines = Array(boardHeight) {Array(boardWidth) {0} }
//    var matMask = Array(boardHeight) {Array(boardWidth) {0} }


fun initializeBoard() {
    // wipe board clean
    gameBoard = Array(boardHeight) { row -> Array<Person>(boardWidth) { col -> Person(row, col) } }

    // reset counters
    masksNum = initialSickNum
    unknownCounter = boardHeight * boardWidth

    // generate random sick people
    val randomIndices = (0 until boardWidth * boardHeight).shuffled().take(initialSickNum)
    randomIndices.forEach { index -> gameBoard[index / boardWidth][index % boardWidth].isSick = true; gameBoard[index / boardWidth][index % boardWidth].isInfectable = false }
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

    // Check if the tile contains a sick person
    if (gameBoard[row][col].isSick) gameOver(false)

    // if not, expose the tile, and possibly it's neighbors
    gameBoard[row][col].isExposed = true
    unknownCounter--
    gameBoard[row][col].cantactNumber = countNeighbors(row, col)

    // if number of neighbors is zero, expose all the neighbors too
    if (gameBoard[row][col].cantactNumber == 0) {
        if (row + 1 < boardHeight && col + 1 < boardWidth) exposeTile(row + 1, col + 1)
        if (row + 1 < boardHeight) exposeTile(row + 1, col)
        if (row + 1 < boardHeight && col - 1 >= 0) exposeTile(row + 1, col - 1)
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
    if (row + 1 < boardHeight && col + 1 < boardWidth) gameBoard[row + 1][col + 1].isInfectable = false
    if (row + 1 < boardHeight) gameBoard[row + 1][col].isInfectable = false
    if (row + 1 < boardHeight && col - 1 >= 0) gameBoard[row + 1][col - 1].isInfectable = false
    if (col + 1 < boardWidth) gameBoard[row][col + 1].isInfectable = false
    if (col - 1 >= 0) gameBoard[row][col - 1].isInfectable = false
    if (row - 1 >= 0 && col + 1 < boardWidth) gameBoard[row - 1][col + 1].isInfectable = false
    if (row - 1 >= 0) gameBoard[row - 1][col].isInfectable = false
    if (row - 1 >= 0 && col - 1 >= 0) gameBoard[row - 1][col - 1].isInfectable = false
}

fun countNeighbors(row: Int, col: Int): Int {
    var contactNumber = 0
    if (row + 1 < boardHeight && col + 1 < boardWidth) contactNumber += gameBoard[row + 1][col + 1].isSick.toInt()
    if (row + 1 < boardHeight) contactNumber += gameBoard[row + 1][col].isSick.toInt()
    if (row + 1 < boardHeight && col - 1 >= 0) contactNumber += gameBoard[row + 1][col - 1].isSick.toInt()
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

    } else {
        println("Error: Already exposed!")
    }
}

fun nightCycle() {
    // Display a night starting massage
    println("\n------- The Night has begun...! -------\n")

    // Start an infections cycle!
    infectionsCycle()

    // Display a night ending massage
    println("\n------- A new day begins! -------\n")
    println("Regular Board:")
    printBoard()
//    println("Exposed Board:")
//    printExposedBoard()
}

fun infectionsCycle() {
    var deadNum = 0
    var infectedNum = 0

    // increment the number of days each sick person was infected
    gameBoard.forEach { arrayOfPersons -> arrayOfPersons.forEach { person -> if (person.isSick && !person.hasMask && person.isAlive) person.daysInfected++ } }
    // TODO: remove the hasMask condition to allow people to grow sicker even with a mask? (only matters if the mask is removed at a later time)

    // find every sick person that has no mask and was infected at least 1 full day
    gameBoard.forEach { arrayOfPersons ->
        arrayOfPersons.forEach { person ->
            if (person.isSick && !person.hasMask && person.isAlive && person.daysInfected >= 1) {  // people who get sick during this night will have daysInfected = 0, and so will not infect others yet

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
                    unknownCounter--

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
        if (row + rowDiff >= boardHeight || row + rowDiff < 0) continue  // boundary condition

        if (col + r in 0 until boardWidth && gameBoard[row + rowDiff][col + r].isInfectable && !gameBoard[row + rowDiff][col + r].hasMask) infected += infectionChance(row + rowDiff, col + r, r)
        if (col - r in 0 until boardWidth && gameBoard[row + rowDiff][col - r].isInfectable && !gameBoard[row + rowDiff][col - r].hasMask) infected += infectionChance(row + rowDiff, col - r, r)
        if (rowDiff == r || rowDiff == -r) for (colDiff in r - 1 downTo (1 - r)) {
            if (col + colDiff >= boardWidth || col + colDiff < 0) continue  // boundary condition
            if (gameBoard[row + rowDiff][col + colDiff].isInfectable && !gameBoard[row + rowDiff][col + colDiff].hasMask) infected += infectionChance(row + rowDiff, col + colDiff, r)
        }
    }
    return infected
}

fun infectionChance(row: Int, col: Int, r: Int): Int {
    // TODO: change infection probability mechanism here
    val gotInfected = Random.nextFloat() <= Pinfect / r
    if (gotInfected) {
        gameBoard[row][col].isSick = true
        gameBoard[row][col].isInfectable = false
    }
    return gotInfected.toInt()
}


//////////// Aux Functions ////////////

fun updateDisplay(row: Int, col: Int) {
    // TODO("Not yet implemented")
}

fun gameOver(victory: Boolean) {
    for (row in 0 until boardHeight) {
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
    return scanner.nextInt() - 1
}

fun getClickOrHold(): Boolean {
    println("Click or Hold? 0 / 1")
    return scanner.nextInt() == 1
}

fun printBoard() {
    var currPerson: Person
    for (row in 0 until boardHeight) {
        for (col in 0 until boardWidth) {
            currPerson = gameBoard[row][col]
            var toPrint = ""
            if (currPerson.isExposed) {
                if (currPerson.isAlive) {
                    toPrint = when (currPerson.isSick) {
                        true -> "*"
                        false -> currPerson.cantactNumber.toString()
                    }

                } else toPrint = "X"
            } else toPrint = when (currPerson.hasMask) {
                true -> "#"
                false -> "-"
            }
            print(toPrint + "\t")
        }
        println("")
    }
}

fun printExposedBoard() {
    var currPerson: Person
    for (row in 0 until boardHeight) {
        for (col in 0 until boardWidth) {
            currPerson = gameBoard[row][col]
            var toPrint = ""

            if (currPerson.isAlive) {
                toPrint = when (currPerson.isSick) {
                    true -> "*"
                    false ->  countNeighbors(row, col).toString()
                }

            } else toPrint = "X"

            print(toPrint + "\t")
        }
        println("")
    }
}