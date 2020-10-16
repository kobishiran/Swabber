package com.flyingcircus.swabber

import android.icu.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.random.Random

fun getBoard3BV(gameBoard: Array<Array<Person>>): Int {
    val boardHeight = gameBoard.size
    val boardWidth = gameBoard[0].size

    // Create a helper array of PersonForScore
    val helperArray = Array(boardHeight) { row ->
        Array(boardWidth) { col ->
            PersonForScore(row, col)
        }
    }

    // Initialize the helper array according to the game board
    gameBoard.forEach { arrayOfPersons -> arrayOfPersons.forEach { person ->
         helperArray[person.row][person.col].isZeroContacts = (!person.isSick && countNeighbors3BV(person.row, person.col, gameBoard) == 0)
    } }

    // Calculate the 3BV:
//    println("Calculating Score...")
    // initialize unknown counter and 3BV ranking
    var unknownCounter = boardHeight * boardWidth
    var score3BV = 0
    lateinit var zeroContactsIndices: Array<Int>
    var row = 0
    var col = 0

    // while the counter is not zero:
    while (unknownCounter > 0) {
//        println("Unknown Counter: $unknownCounter")
        // find a zeroContact Person
        zeroContactsIndices = findZeroContactPerson(helperArray)
        row = zeroContactsIndices[0]
        col = zeroContactsIndices[1]
//        println("found zero contancts at row $row and column $col")


        // if found, press it and flood fill its neighbors, then continue
        if (row != -1 && col != -1) { unknownCounter -= pressTile3BV(row, col, helperArray) ; score3BV++; continue }

        // if none are found, run over the array and count unpressed healthy people
        helperArray.forEach { arrayOfPersonForScores -> arrayOfPersonForScores.forEach { person ->
            if (!person.isPressed && !gameBoard[person.row][person.col].isSick) {score3BV++ ; person.isPressed = true ; unknownCounter--}
            else if (gameBoard[person.row][person.col].isSick) unknownCounter--
        } }

    }
    println("3BV Score: $score3BV")
    return score3BV

}

fun pressTile3BV(row: Int, col: Int, helperArray: Array<Array<PersonForScore>>): Int {
    if (helperArray[row][col].isPressed) return 0  // if this person is already pressed, return
    var pressedCounter = 1  // a counter that counts how many people were pressed in this flood fill
    helperArray[row][col].isPressed = true

    val boardHeight = helperArray.size
    val boardWidth = helperArray[0].size

    // if the person is zero contacts, press all of its neighbors
    if (helperArray[row][col].isZeroContacts) {
        if (row + 1 < boardHeight && col + 1 < boardWidth && !helperArray[row + 1][col + 1].isPressed)
            pressedCounter += pressTile3BV(row + 1, col + 1, helperArray)

        if (row + 1 < boardHeight && !helperArray[row + 1][col].isPressed)
            pressedCounter += pressTile3BV(row + 1, col, helperArray)

        if (row + 1 < boardHeight && col - 1 >= 0 && !helperArray[row + 1][col - 1].isPressed)
            pressedCounter += pressTile3BV(row + 1, col - 1, helperArray)

        if (col + 1 < boardWidth && !helperArray[row][col + 1].isPressed)
            pressedCounter += pressTile3BV(row, col + 1, helperArray)

        if (col - 1 >= 0 && !helperArray[row][col - 1].isPressed)
            pressedCounter += pressTile3BV(row, col - 1, helperArray)

        if (row - 1 >= 0 && col + 1 < boardWidth && !helperArray[row - 1][col + 1].isPressed)
            pressedCounter += pressTile3BV(row - 1, col + 1, helperArray)

        if (row - 1 >= 0 && !helperArray[row - 1][col].isPressed)
            pressedCounter += pressTile3BV(row - 1, col, helperArray)

        if (row - 1 >= 0 && col - 1 >= 0 && !helperArray[row - 1][col - 1].isPressed)
            pressedCounter += pressTile3BV(row - 1, col - 1, helperArray)
    }
    return pressedCounter
}

fun findZeroContactPerson(helperArray: Array<Array<PersonForScore>>): Array<Int> {
    helperArray.forEach { it.forEach { person -> if (!person.isPressed && person.isZeroContacts) return arrayOf(person.row, person.col) } }
    return arrayOf(-1, -1)
}


fun countNeighbors3BV(row: Int, col: Int, gameBoard: Array<Array<Person>>): Int {
    var contactNumber = 0
    val boardHeight = gameBoard.size
    val boardWidth = gameBoard[0].size
    if (row + 1 < boardHeight && col + 1 < boardWidth)
        contactNumber += gameBoard[row + 1][col + 1].isSick.toInt()
    if (row + 1 < boardHeight)
        contactNumber += gameBoard[row + 1][col].isSick.toInt()
    if (row + 1 < boardHeight && col - 1 >= 0)
        contactNumber += gameBoard[row + 1][col - 1].isSick.toInt()
    if (col + 1 < boardWidth)
        contactNumber += gameBoard[row][col + 1].isSick.toInt()
    if (col - 1 >= 0)
        contactNumber += gameBoard[row][col - 1].isSick.toInt()
    if (row - 1 >= 0 && col + 1 < boardWidth)
        contactNumber += gameBoard[row - 1][col + 1].isSick.toInt()
    if (row - 1 >= 0)
        contactNumber += gameBoard[row - 1][col].isSick.toInt()
    if (row - 1 >= 0 && col - 1 >= 0)
        contactNumber += gameBoard[row - 1][col - 1].isSick.toInt()
    return contactNumber
}

// extension function to turn bool to int
fun Boolean.toInt() = if (this) 1 else 0

class PersonForScore(val row: Int, val col: Int) {
    var isPressed = false
    var isZeroContacts = false
}

fun printExposedBoard(gameBoard: Array<Array<Person>>, boardHeight: Int, boardWidth: Int) {
    var currPerson: Person
    for (row in 0 until boardHeight) {
        for (col in 0 until boardWidth) {
            currPerson = gameBoard[row][col]
            var toPrint = ""

            if (currPerson.isAlive) {
                toPrint = when (currPerson.isSick) {
                    true -> "*"
                    false ->  countNeighbors3BV(row, col, gameBoard).toString()
                }

            } else toPrint = "X"

            print(toPrint + "\t")
        }
        println("")
    }
}

fun getCurrentDate(): String {
    val date = Date()
    val formatter = java.text.SimpleDateFormat("dd/MM/yy")
    return formatter.format(date).toString()
}

val initialSickNum = 10
fun main(args: Array<String>) {
    val boardHeight = 10
    val boardWidth = 10
    val gameBoard = Array(boardHeight) { row ->
        Array(boardWidth) { col ->
            Person(row, col, Random.nextFloat() <= 0.5)
        }
    }

    // generate random sick people
    val randomIndices =
        (0 until boardWidth * boardHeight).shuffled().take(initialSickNum)
    randomIndices.forEach { index ->
        gameBoard[index / boardWidth][index % boardWidth].isSick = true
        gameBoard[index / boardWidth][index % boardWidth].isInfectable = false
    }

    println("Generated Board:")
    printExposedBoard(gameBoard, boardHeight, boardWidth)

    val score3BV = getBoard3BV(gameBoard)
//    println("Score Calculated")
//    printExposedBoard(gameBoard, boardHeight, boardWidth)
    println("3BV Score: $score3BV")
}