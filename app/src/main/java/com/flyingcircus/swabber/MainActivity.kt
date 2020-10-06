package com.flyingcircus.swabber

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.concurrent.timer
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    // Initialise global variables
    val initialSickNum = 20         // number of "mines"
    val boardHeight = 16             // number of rows
    val boardWidth = 10             // number of columns
    var gameBoard = Array(boardHeight) { row -> Array<Person>(boardWidth) { col -> Person(row, col) } }
    var unknownCounter = boardHeight * boardWidth // number of "tiles" not "exposed" neither "flagged"
    var masksNum = initialSickNum   // number of "flags"
    val dayLengthInMilli = 20_000L    // number of (milli)seconds from day to day
    var timeLeftSecs = dayLengthInMilli.toInt() / 1000  // the current time of the timer, initialized to a full day
    val infectionRadius = 2  // the maximal infection radius
    val Pdeath = 0.05F  // base probability to die
    val Pinfect = 0.03F // base probability to get infected
    var deadNum = 0  // total number of people that died
    val maxDeadAllowed = 5  // maximal number of dead people allowed before you lose
    var wrongMasks = 0  // the number of masks placed on healthy people
    val maxWrongMasks = 3 // maximal number of wrong masks before you lose due to economic disaster
    lateinit var countDownTimer: Timer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initializeBoard()
        displayInitBoard(boardHeight,boardWidth)
        clickListener(boardHeight,boardWidth)
        startTimer()
    }

    override fun onDestroy() {
        countDownTimer.cancel()  // make sure the counter is stopped before exiting
        super.onDestroy()
    }

    private fun initializeBoard() {
        // wipe board clean
        gameBoard = Array(boardHeight) { row -> Array<Person>(boardWidth) { col -> Person(row, col) } }

        // reset counters
        masksNum = initialSickNum
        unknownCounter = boardHeight * boardWidth

        // generate random sick people
        val randomIndices = (0 until boardWidth * boardHeight).shuffled().take(initialSickNum)
        randomIndices.forEach { index -> gameBoard[index / boardWidth][index % boardWidth].isSick = true; gameBoard[index / boardWidth][index % boardWidth].isInfectable = false  }
    }

    private fun displayInitBoard(boardHeight: Int, boardWidth: Int){
        // The size of matrix
        val spaceX = 10   // spacing between elements in each row
        val spaceY = 10   // spacing between elements in each column


        for (row in 1..boardHeight) {

            // Create new horizontal LinearLayout, named "child", programmatically
            val child = LinearLayout(this)
            child.orientation = LinearLayout.HORIZONTAL

            // Set unique id to each child (100, 200, 300, ... , 100 * boardHeight)
            child.id = 100 * row

            // Create a LinearLayout.LayoutParams object for the new horizontal LinearLayout
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1F,
            )

            // Add margin to the horizontal LinearLayout
            layoutParams.setMargins(0,spaceY,0,0)

            // Now, specify the horizontal LinearLayout width and height (dimension)
            child.layoutParams = layoutParams


            // Now in the new horizontal layout create n-text views
            for (col in 1..boardWidth) {

                // Create a new TextView instance programmatically
                val imageview = ImageView(this)

                // Set unique id to each imageview (101, 102, ..., 100+width, 201, 202, ... ... , 100* height + width)
                imageview.id = 100 * row + col

                // Set Image Resource
                imageview.setImageResource(R.drawable.unexposed)


                // Create a LinearLayout.LayoutParams object for text view
                val imageParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, // This will define text view width
                    LinearLayout.LayoutParams.WRAP_CONTENT, // This will define text view height
                    1F,
                )

                // Add margin to the text view
                imageParams.setMargins(0, 0, spaceX, 0)

                // Now, specify the text view width and height (dimension)
                imageview.layoutParams = imageParams

                // Change the image view background color
                imageview.setBackgroundColor(Color.TRANSPARENT)

                // Put some padding on image view
                imageview.setPadding(0, 0, 0, 0)


                // Add the text view to the view group (horizontal LinearLayout, "child")
                child.addView(imageview)


                // Increment col
            }

            // Add the "child" view to the view group (vertical LinearLayout "root_layout")
            root_layout.addView(child)

            // Increment row
        }
    }

    private fun clickListener(boardHeight: Int, boardWidth: Int){
        for (row in 1..boardHeight) {
            for (col in 1..boardWidth) {
                val temp: ImageView = findViewById(100 * row + col)
                  temp.setOnClickListener() {
                      clickTile(row - 1, col - 1)
                  }
                temp.setOnLongClickListener() {
                    holdTile(row - 1, col - 1)
                    return@setOnLongClickListener true
                }
            }
        }
    }

    private fun clickTile(row: Int, col: Int) {
        // Check if the tile is already exposed or has mask
        if (gameBoard[row][col].isExposed) {
            // TODO: show error: Already exposed!
            Toast.makeText(this,"Already exposed!", Toast.LENGTH_SHORT).show()
        } else if (gameBoard[row][col].hasMask) {
            // TODO: show error: has a mask!
            Toast.makeText(this,"Has a mask!", Toast.LENGTH_SHORT).show()
        } else {
            exposeTile(row, col)
            checkVictory()
        }

    }

    private fun exposeTile(row: Int, col: Int) {

        // if tile is already exposed, return
        if (gameBoard[row][col].isExposed) return   // Kobi: It is redundant

        // Check if the tile contains a sick person
        if (gameBoard[row][col].isSick) gameOver(false, "Corona")

        // if not, expose the tile, and possibly it's neighbors
        gameBoard[row][col].isExposed = true
        unknownCounter--
        gameBoard[row][col].contactNumber = countNeighbors(row, col)

        // if number of neighbors is zero, expose all the neighbors too
        if (gameBoard[row][col].contactNumber == 0) {
            if (row + 1 < boardHeight && col + 1 < boardWidth && !gameBoard[row+1][col+1].hasMask)   exposeTile(row + 1, col + 1)
            if (row + 1 < boardHeight && !gameBoard[row+1][col].hasMask)                           exposeTile(row + 1, col)
            if (row + 1 < boardHeight && col - 1 >= 0 && !gameBoard[row+1][col-1].hasMask)           exposeTile(row + 1, col - 1)
            if (col + 1 < boardWidth && !gameBoard[row][col+1].hasMask)                           exposeTile(row, col + 1)
            if (col - 1 >= 0 && !gameBoard[row][col-1].hasMask)                                   exposeTile(row, col - 1)
            if (row - 1 >= 0 && col + 1 < boardWidth && !gameBoard[row-1][col+1].hasMask)           exposeTile(row - 1, col + 1)
            if (row - 1 >= 0 && !gameBoard[row-1][col].hasMask)                                   exposeTile(row - 1, col)
            if (row - 1 >= 0 && col - 1 >= 0 && !gameBoard[row-1][col-1].hasMask)                   exposeTile(row - 1, col - 1)
        }

        // update the display of the tile
        updateDisplay(row, col)

        // change infectable status of self and all neighbors to false
        gameBoard[row][col].isInfectable = false
        if (row + 1 < boardHeight && col + 1 < boardWidth)   gameBoard[row + 1][col + 1].isInfectable = false
        if (row + 1 < boardHeight)                           gameBoard[row + 1][col].isInfectable = false
        if (row + 1 < boardHeight && col - 1 >= 0)           gameBoard[row + 1][col - 1].isInfectable = false
        if (col + 1 < boardWidth)                           gameBoard[row][col + 1].isInfectable = false
        if (col - 1 >= 0)                                   gameBoard[row][col - 1].isInfectable = false
        if (row - 1 >= 0 && col + 1 < boardWidth)           gameBoard[row - 1][col + 1].isInfectable = false
        if (row - 1 >= 0)                                   gameBoard[row - 1][col].isInfectable = false
        if (row - 1 >= 0 && col - 1 >= 0)                   gameBoard[row - 1][col - 1].isInfectable = false
    }

    private  fun countNeighbors(row: Int, col: Int): Int {
        var contactNumber = 0
        if (row + 1 < boardHeight && col + 1 < boardWidth) contactNumber += gameBoard[row + 1][col + 1].isSick.toInt()
        if (row + 1 < boardHeight) contactNumber                         += gameBoard[row + 1][col].isSick.toInt()
        if (row + 1 < boardHeight && col - 1 >= 0) contactNumber         += gameBoard[row + 1][col - 1].isSick.toInt()
        if (col + 1 < boardWidth) contactNumber                         += gameBoard[row][col + 1].isSick.toInt()
        if (col - 1 >= 0) contactNumber                                 += gameBoard[row][col - 1].isSick.toInt()
        if (row - 1 >= 0 && col + 1 < boardWidth) contactNumber         += gameBoard[row - 1][col + 1].isSick.toInt()
        if (row - 1 >= 0) contactNumber                                 += gameBoard[row - 1][col].isSick.toInt()
        if (row - 1 >= 0 && col - 1 >= 0) contactNumber                 += gameBoard[row - 1][col - 1].isSick.toInt()
        return contactNumber
    }

    private fun holdTile(row: Int, col: Int){
        if (!gameBoard[row][col].isExposed) { // make sure the tile is not already exposed
            when (gameBoard[row][col].hasMask) {
                true -> {  // if already has mask, remove it and increment mask counter
                    gameBoard[row][col].hasMask = false
                    masksNum++
                    unknownCounter++
                    if (!gameBoard[row][col].isSick) wrongMasks--
                }
                false -> {  // if not, put on a mask if masks are available or show error
                    if (masksNum > 0) {
                        gameBoard[row][col].hasMask = true
                        masksNum--
                        unknownCounter--
                        if (!gameBoard[row][col].isSick) wrongMasks++
                        checkLosingByEconomy()
                    } else {
                        // TODO: Show error: Not enough masks!
                        Toast.makeText(this,"Not enough masks!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            updateDisplay(row, col)

        } else {
            // TODO: Show error: Already exposed!
            Toast.makeText(this,"Already exposed!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun nightCycle() {
        // Display a night starting massage (must be called from main UI thread)
        this@MainActivity.runOnUiThread {Toast.makeText(this,">>> A night has begun...! <<<", Toast.LENGTH_SHORT).show()}

        // Start an infections cycle!
        infectionsCycle()

        // Check if too many people died during the night
        this@MainActivity.runOnUiThread { checkLosingByDeath() }

        // Display a night ending massage (must be called from main UI thread)
        this@MainActivity.runOnUiThread {Toast.makeText(this,">>> A new day has risen! <<<", Toast.LENGTH_SHORT).show()}
    }

    private fun infectionsCycle() {
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

    private fun infectNeighbors(row: Int, col: Int, r: Int): Int {
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

    private fun infectionChance(row: Int, col: Int, r: Int): Int {
        // TODO: change infection probability mechanism here
        val gotInfected = Random.nextFloat() <= Pinfect / r
        if (gotInfected) {
            gameBoard[row][col].isSick = true
            gameBoard[row][col].isInfectable = false
        }
        return gotInfected.toInt()
    }

    private fun updateDisplay(row: Int, col: Int) {

        // go to the unique id of that element
        val temp: ImageView = findViewById(100 * (row+1) + (col+1) )

        if(gameBoard[row][col].hasMask){
            temp.setImageResource(R.drawable.unexposedflagged)
        } else if (!gameBoard[row][col].isExposed)
            temp.setImageResource(R.drawable.unexposed)
          else if (gameBoard[row][col].isExposed) {
            when (gameBoard[row][col].contactNumber){
                0 -> temp.setImageResource(R.drawable.exposed0)
                1 -> temp.setImageResource(R.drawable.exposed1)
                2 -> temp.setImageResource(R.drawable.exposed2)
                3 -> temp.setImageResource(R.drawable.exposed3)
                4 -> temp.setImageResource(R.drawable.exposed4)
                5 -> temp.setImageResource(R.drawable.exposed5)
                6 -> temp.setImageResource(R.drawable.exposed6)
                7 -> temp.setImageResource(R.drawable.exposed7)
                8 -> temp.setImageResource(R.drawable.exposed8)
                9 -> temp.setImageResource(R.drawable.exposed9)
            }
            if(gameBoard[row][col].isSick)
                if (gameBoard[row][col].isAlive)
                temp.setImageResource(R.drawable.exposedbomb)
                else
                temp.setImageResource((R.drawable.exposeddead))
        }
    }

    private fun gameOver(victory: Boolean, reason: String) {
        // TODO: add transitions to end activities
        if(victory)
            Toast.makeText(this,"Winner!", Toast.LENGTH_SHORT).show()
        else when (reason) {
            "Death" -> Toast.makeText(this,"You let too many people die! You LOSE!!", Toast.LENGTH_SHORT).show()
            "Economy" -> Toast.makeText(this,"The economy collapsed! you LOSE!", Toast.LENGTH_SHORT).show()
            "Corona" -> Toast.makeText(this,"You got infected with Corona! Loser!", Toast.LENGTH_SHORT).show()
            else -> Toast.makeText(this,"Loser!", Toast.LENGTH_SHORT).show()
        }

    }

    private fun checkLosingByDeath() {
        if (deadNum >= maxDeadAllowed) gameOver(victory = false, reason = "Death")
    }

    private fun checkLosingByEconomy() {
        if (wrongMasks >= maxWrongMasks) gameOver(victory = false, reason = "Economy")
    }

    private fun checkVictory() {
        if (unknownCounter == 0) gameOver(true, "Yay!") // the reason argument is not needed here
    }

    // extension function to turn bool to int
    fun Boolean.toInt() = if (this) 1 else 0

    private fun startTimer() {
        Toast.makeText(this,"Starting Timer", Toast.LENGTH_SHORT).show()
        countDownTimer = timer("Day Counter", false, initialDelay = 0, 1000L) {
            displayTime(timeLeftSecs)
            if (timeLeftSecs == 0) {
                nightCycle()
                timeLeftSecs = dayLengthInMilli.toInt() / 1000
                displayTime(timeLeftSecs)
                timeLeftSecs--
            } else {
                timeLeftSecs--
            }
        }
    }

    private fun displayTime(timeInSecs: Int) {
        val minutes = timeInSecs / 60
        val seconds = timeInSecs % 60
        val timer = findViewById<TextView>(R.id.timer)
        // UI updates must be called from the main UI thread:
        this@MainActivity.runOnUiThread { timer.text = "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}" }
    }

}
// fuck this shit
class Person(val row: Int, val col: Int) {
    var isSick = false
    var hasMask = false
    var isExposed = false
    var contactNumber = -1
    var isInfectable = true
    var daysInfected = 0
    var isAlive = true
}