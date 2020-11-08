package com.flyingcircus.swabber

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Runnable
import java.lang.Math.abs
import java.lang.Math.min
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timer
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.random.Random


class GameActivity : AppCompatActivity() {

    // Initialise global variables
    lateinit var difficulty: Difficulty
    lateinit var gameBoard: Array<Array<Person>>
    var unknownCounter = 0 // number of "tiles" not "exposed" neither "flagged"
    var masksNum = 0   // number of "flags"
    var timeLeftSecs = 0  // the current time of the timer, initialized to a full day
    var deadNum = 0  // total number of people that died
    var wrongMasks = 0  // the number of masks placed on healthy people
    var gameIsRunning = true
    var daysCounter = 1
    var playerClicks = 0
    lateinit var countDownTimer: Timer
    var board3BVList = ArrayList<Int>()
    val playerName = "???"  // temp placeholder
    var SkipPause: Boolean = true // skip the first pause screen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // get difficulty level
        difficulty = intent.getSerializableExtra("Difficulty") as Difficulty

        initializeBoard()
        displayInitBoard(difficulty.boardHeight, difficulty.boardWidth)
        boardClickListener(difficulty.boardHeight, difficulty.boardWidth)
        pauseButton.setOnClickListener {
                showPause()
        }
        newGameButton.setOnClickListener {
            countDownTimer.cancel()
            pauseButton.text = "Pause"
            initializeBoard()
            gameBoard.forEach { arrayOfPersons ->
                arrayOfPersons.forEach { person ->
                    updateDisplay(person.row, person.col)
                }
            }
            timeLeftSecs = difficulty.dayLengthInMilli.toInt() / 1000
            displayTime(timeLeftSecs)
            startTimer()
        }
        startTimer()
        //   pauseButton.performClick()
    }

    override fun onDestroy() {
        countDownTimer.cancel()  // make sure the counter is stopped before exiting
        super.onDestroy()
    }

    override fun onBackPressed() {
        countDownTimer.cancel()
        gameIsRunning = false
        AlertDialog.Builder(this)
            .setTitle("Really Exit?")
            .setMessage("Are you sure you want to exit?")
            .setNegativeButton(R.string.no, object : DialogInterface.OnClickListener {
                override fun onClick(arg0: DialogInterface?, arg1: Int) {
                    startTimer()
                    gameIsRunning = true
                }
            })
            .setPositiveButton(R.string.yes, object : DialogInterface.OnClickListener {
                override fun onClick(arg0: DialogInterface?, arg1: Int) {
                    super@GameActivity.onBackPressed()
                    finish()  // make sure the activity is destroyed when leaving
                }
            }).create().show()
    }

    override fun onPause() {
        countDownTimer.cancel()
        gameIsRunning = false
        super.onPause()
    }

    override fun onResume() {
        if (!SkipPause) {
            showPause()
        } else SkipPause = false
        super.onResume()
        //   pauseButton.performClick() // Add if you want automatic resume, but it pauses on first launch
    }

    private fun initializeBoard() {

        // wipe board clean, random gender 50%-50%
        gameBoard = Array(difficulty.boardHeight) { row ->
            Array(difficulty.boardWidth) { col ->
                Person(row, col, Random.nextFloat() <= 0.5)
            }
        }

        // reset counters
        masksNum = difficulty.initialSickNum  // number of "flags"
        unknownCounter = difficulty.boardHeight * difficulty.boardWidth
        wrongMasks = 0
        deadNum = 0
        playerClicks = 0
        timeLeftSecs = difficulty.dayLengthInMilli.toInt() / 1000
        displayTime(timeLeftSecs)
        gameIsRunning = true
        daysCounter = 1
        board3BVList.clear()

        // update Days Counter display
        textDayCounter.text = "Day $daysCounter"

        // generate random sick people
        val randomIndices =
            (0 until difficulty.boardWidth * difficulty.boardHeight).shuffled().take(difficulty.initialSickNum)
        randomIndices.forEach { index ->
            gameBoard[index / difficulty.boardWidth][index % difficulty.boardWidth].isSick = true
            gameBoard[index / difficulty.boardWidth][index % difficulty.boardWidth].isInfectable = false
        }

        // Calculate board 3BV Score
        board3BVList.add(getBoard3BV(gameBoard))
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

            // Add margin to the horizontal LinearLayout - making "rows"
            layoutParams.setMargins(0, spaceY, 0, 0)

            // Now, specify the horizontal LinearLayout width and height (dimension)
            child.layoutParams = layoutParams


            // Now in the new horizontal layout create n-text views
            for (col in 1..boardWidth) {

                // Create a new TextView instance programmatically
                val imageview = ImageView(this)

                // Set unique id to each imageview (101, 102, ..., 100+width, 201, 202, ... ... , 100* height + width)
                imageview.id = 100 * row + col

                // Set Image Resource
                if (gameBoard[row - 1][col - 1].gender == Gender.FEMALE)
                    imageview.setImageResource(R.drawable.unexposed_female)
                else imageview.setImageResource(R.drawable.unexposed_male)


                // Create a LinearLayout.LayoutParams object for text view
                val imageParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, // This will define text view width
                    LinearLayout.LayoutParams.WRAP_CONTENT, // This will define text view height
                    1F,
                )

                // Add margin to the text view - making "columns"
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

    private fun boardClickListener(boardHeight: Int, boardWidth: Int) {
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
            Toast.makeText(this, "Already exposed!", Toast.LENGTH_SHORT).show()
        } else if (gameBoard[row][col].hasMask) {
            // TODO: show error: has a mask!
            Toast.makeText(this, "Has a mask!", Toast.LENGTH_SHORT).show()
        } else {
            playerClicks++
            exposeTile(row, col)
            checkVictory()
        }
    }

    private fun exposeTile(row: Int, col: Int) {

        // if tile is already exposed, return
        if (gameBoard[row][col].isExposed) return   // Kobi: It is redundant

        // if not, expose the tile, and possibly it's neighbors
        gameBoard[row][col].isExposed = true
        gameBoard[row][col].contactNumber = countNeighbors(row, col)
        gameBoard[row][col].isInfectable = false

        // update the display of the tile
        updateDisplay(row, col)

        // Check if the tile contains a sick person
        if (gameBoard[row][col].isSick) {
            gameOver(false, "Corona")
            return
        }

        // isolated for the case that the last tile is bomb
        unknownCounter--

        // if number of neighbors is zero, expose all the neighbors too
        if (gameBoard[row][col].contactNumber == 0) {
            if (row + 1 < difficulty.boardHeight && col + 1 < difficulty.boardWidth && !gameBoard[row + 1][col + 1].hasMask)
                exposeTile(row + 1, col + 1)
            if (row + 1 < difficulty.boardHeight && !gameBoard[row + 1][col].hasMask)
                exposeTile(row + 1, col)
            if (row + 1 < difficulty.boardHeight && col - 1 >= 0 && !gameBoard[row + 1][col - 1].hasMask)
                exposeTile(row + 1, col - 1)
            if (col + 1 < difficulty.boardWidth && !gameBoard[row][col + 1].hasMask)
                exposeTile(row, col + 1)
            if (col - 1 >= 0 && !gameBoard[row][col - 1].hasMask)
                exposeTile(row, col - 1)
            if (row - 1 >= 0 && col + 1 < difficulty.boardWidth && !gameBoard[row - 1][col + 1].hasMask)
                exposeTile(row - 1, col + 1)
            if (row - 1 >= 0 && !gameBoard[row - 1][col].hasMask)
                exposeTile(row - 1, col)
            if (row - 1 >= 0 && col - 1 >= 0 && !gameBoard[row - 1][col - 1].hasMask)
                exposeTile(row - 1, col - 1)
        }


        // change infectable status of self and all neighbors to false
        if (row + 1 < difficulty.boardHeight && col + 1 < difficulty.boardWidth)
            gameBoard[row + 1][col + 1].isInfectable = false
        if (row + 1 < difficulty.boardHeight)
            gameBoard[row + 1][col].isInfectable = false
        if (row + 1 < difficulty.boardHeight && col - 1 >= 0)
            gameBoard[row + 1][col - 1].isInfectable = false
        if (col + 1 < difficulty.boardWidth)
            gameBoard[row][col + 1].isInfectable = false
        if (col - 1 >= 0)
            gameBoard[row][col - 1].isInfectable = false
        if (row - 1 >= 0 && col + 1 < difficulty.boardWidth)
            gameBoard[row - 1][col + 1].isInfectable = false
        if (row - 1 >= 0)
            gameBoard[row - 1][col].isInfectable = false
        if (row - 1 >= 0 && col - 1 >= 0)
            gameBoard[row - 1][col - 1].isInfectable = false
    }

    private fun countNeighbors(row: Int, col: Int): Int {
        var contactNumber = 0
        if (row + 1 < difficulty.boardHeight && col + 1 < difficulty.boardWidth)
            contactNumber += gameBoard[row + 1][col + 1].isSick.toInt()
        if (row + 1 < difficulty.boardHeight)
            contactNumber += gameBoard[row + 1][col].isSick.toInt()
        if (row + 1 < difficulty.boardHeight && col - 1 >= 0)
            contactNumber += gameBoard[row + 1][col - 1].isSick.toInt()
        if (col + 1 < difficulty.boardWidth)
            contactNumber += gameBoard[row][col + 1].isSick.toInt()
        if (col - 1 >= 0)
            contactNumber += gameBoard[row][col - 1].isSick.toInt()
        if (row - 1 >= 0 && col + 1 < difficulty.boardWidth)
            contactNumber += gameBoard[row - 1][col + 1].isSick.toInt()
        if (row - 1 >= 0)
            contactNumber += gameBoard[row - 1][col].isSick.toInt()
        if (row - 1 >= 0 && col - 1 >= 0)
            contactNumber += gameBoard[row - 1][col - 1].isSick.toInt()
        return contactNumber
    }

    private fun holdTile(row: Int, col: Int){
        if (!gameBoard[row][col].isExposed) { // make sure the tile is not already exposed
            when (gameBoard[row][col].hasMask) {
                true -> {  // if already has mask, remove it
                    gameBoard[row][col].hasMask = false
                    unknownCounter++
                    if (!gameBoard[row][col].isSick) {wrongMasks-- ; playerClicks--}
                }
                false -> {  // if not, put on a mask
                    gameBoard[row][col].hasMask = true
                    unknownCounter--
                    if (!gameBoard[row][col].isSick) {wrongMasks++ ; playerClicks++}
                    checkLosingByEconomy()
                }
            }
            updateDisplay(row, col)
        } else {
            Toast.makeText(this, "Already exposed!", Toast.LENGTH_SHORT).show()
        }
        checkVictory()
    }

    private fun nightCycle() {

        // Display a night starting massage (must be called from main UI thread)
        this@GameActivity.runOnUiThread {
            Toast.makeText(this, ">>> A night has begun...! <<<", Toast.LENGTH_SHORT).show()
        }

        // Start an infections cycle!
        infectionsCycle()

        // Check if too many people died during the night
        this@GameActivity.runOnUiThread { checkLosingByDeath() }

        // if not, check if victory is reached due to people dying
        this@GameActivity.runOnUiThread { checkVictory() }

        daysCounter++
        textDayCounter.text = "Day $daysCounter"

        // calculate the new board's 3BV
        board3BVList.add(getBoard3BV(gameBoard))

        // Display a night ending massage (must be called from main UI thread)
        this@GameActivity.runOnUiThread {
            Toast.makeText(this, ">>> A new day has risen! <<<", Toast.LENGTH_SHORT).show()
        }
    }

    private fun infectionsCycle() {
        var infectedNum = 0

        // increment the number of days each sick person was infected
        gameBoard.forEach { arrayOfPersons ->
            arrayOfPersons.forEach { person ->
                if (person.isSick &&
                    !person.hasMask &&
                    person.isAlive
                )
                    person.daysInfected++
            }
        }
        // TODO: remove the hasMask condition to allow people to grow sicker even with a mask? (only matters if the mask is removed at a later time)

        // find every sick person that has no mask and was infected at least 1 full day
        gameBoard.forEach { arrayOfPersons ->
            arrayOfPersons.forEach { person ->
                if (person.isSick &&
                    !person.hasMask &&
                    person.isAlive &&
                    person.daysInfected >= 1
                ) {  // people who get sick during this night will have daysInfected = 0, and so will not infect others yet

                    // the sick person might infect his neighbors, depending on their distance from him
                    infectedNum += infectNeighbors(person.row, person.col, difficulty.infectionRadius)
                    // TODO: if infectedNum > N, break?


                    // also, the sick person might die, depending on how long he has been sick
                    val heDies =
                        Random.nextFloat() <= (difficulty.Pdeath * person.daysInfected) // TODO: change death probability mechanism here
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
            for (colDiff in r downTo -r) {
                if (row + rowDiff < 0 || row + rowDiff >= difficulty.boardHeight ||
                    col + colDiff < 0 || col + colDiff >= difficulty.boardWidth
                )
                    continue  // boundary condition
                if (rowDiff == 0 && colDiff == 0)
                    continue // infectious person
                if (gameBoard[row + rowDiff][col + colDiff].isInfectable &&
                    !gameBoard[row + rowDiff][col + colDiff].hasMask
                )
                    infected += infectionChance(row + rowDiff, col + colDiff, max(abs(rowDiff), abs(colDiff)))
            }
        }
        return infected
    }

    private fun infectionChance(row: Int, col: Int, r: Int): Int {
        // TODO: change infection probability mechanism here
        val gotInfected = Random.nextFloat() <= (difficulty.Pinfect / r)
        if (gotInfected) {
            gameBoard[row][col].isSick = true
            gameBoard[row][col].isInfectable = false
        }
        return gotInfected.toInt()
    }

    private fun updateDisplay(row: Int, col: Int) {

        // go to the unique id of that element. shift by 1 because array starts at 0
        val temp: ImageView = findViewById(100 * (row + 1) + (col + 1))

        if (gameBoard[row][col].hasMask)
            if (gameBoard[row][col].gender == Gender.MALE)
                temp.setImageResource(R.drawable.unexposedflagged_male)
            else
                temp.setImageResource(R.drawable.unexposedflagged_female)
        else
            if (gameBoard[row][col].isExposed) {
                when (gameBoard[row][col].contactNumber) {
                    0 -> temp.setImageResource(R.drawable.exposed0)
                    1 -> temp.setImageResource(R.drawable.exposed1)
                    2 -> temp.setImageResource(R.drawable.exposed2)
                    3 -> temp.setImageResource(R.drawable.exposed3)
                    4 -> temp.setImageResource(R.drawable.exposed4)
                    5 -> temp.setImageResource(R.drawable.exposed5)
                    6 -> temp.setImageResource(R.drawable.exposed6)
                    7 -> temp.setImageResource(R.drawable.exposed7)
                    8 -> temp.setImageResource(R.drawable.exposed8)
                }
                if (gameBoard[row][col].isSick)
                    if (gameBoard[row][col].isAlive)
                        temp.setImageResource(R.drawable.exposedbomb)
                    else {
                        if (gameBoard[row][col].gender == Gender.MALE)
                            temp.setImageResource((R.drawable.exposeddead_male))
                        else
                            temp.setImageResource((R.drawable.exposeddead_female))
                    }
            } else {
                if (gameBoard[row][col].gender == Gender.MALE)
                    temp.setImageResource(R.drawable.unexposed_male)
                else
                    temp.setImageResource(R.drawable.unexposed_female)
            }
    }

    private fun exposeBoard() {
        gameBoard.forEach { arrayOfPersons ->
            arrayOfPersons.forEach { person ->
                person.contactNumber = countNeighbors(person.row, person.col)
                person.isExposed = true
                updateDisplay(person.row, person.col)
            }
        }
    }

    private fun gameOver(victory: Boolean, reason: String) {
        // Expose the entire board
        exposeBoard()

        // calculate mean 3BV score
        var mean3BV = 0F
        val scoreWeights = (board3BVList.size downTo 1).toList()
        val totalTime =
            (difficulty.dayLengthInMilli.toInt() / 1000 - timeLeftSecs) + (daysCounter - 1) * difficulty.dayLengthInMilli.toInt() / 1000
        for (day in 0 until board3BVList.size) mean3BV += scoreWeights[day] * board3BVList[day]
        mean3BV /= (scoreWeights.sum().toFloat())  // Normalize by the total weight
        println(board3BVList.toString())
        println("Mean 3BV: $mean3BV")
        println("Player Clicks: $playerClicks")
        println("Total time: $totalTime")
        println("Dead: $deadNum, Wrong Masks: $wrongMasks, Days: $daysCounter")
        val playerScore =
            max(((mean3BV / playerClicks) * 5_000).roundToInt() + ((difficulty.BMTime / totalTime) * 5_000).roundToInt() - 100 * deadNum - 100 * wrongMasks - 100 * (daysCounter - 1), 0)

        // Get the current date
        val date = getCurrentDate()

        // Create score object
        val scoreObject = Score(difficulty.difficultyName, -1, playerName, playerScore, date)

          countDownTimer.cancel() // TODO: Add this on actual app
        val nextActivity : Intent
        if (victory) {
            Toast.makeText(this, "Winner!", Toast.LENGTH_SHORT).show()
            nextActivity = Intent(this, WinScreen::class.java).putExtra("Score", scoreObject)  // add score Extra if game is won
        } else when (reason) { // TODO: different lose screens \\\ we need to adjust code for custom game (doesnt have difficulty)
            "Death" -> {
                Toast.makeText(this, "You let too many people die! You LOSE!!", Toast.LENGTH_SHORT).show()
                nextActivity = Intent(this, LoseDeath::class.java)
            };
            "Economy" -> {
                Toast.makeText(this, "The economy collapsed! you LOSE!", Toast.LENGTH_SHORT).show()
                nextActivity = Intent(this, LoseEconomy::class.java)
            }
            "Corona" -> {
                Toast.makeText(this, "You got infected with Corona! Loser!", Toast.LENGTH_SHORT).show()
                nextActivity = Intent(this, LoseCorona::class.java)
            }
            else -> {
                Toast.makeText(this, "Loser!", Toast.LENGTH_SHORT).show()
                nextActivity = Intent(this, LoseScreen::class.java)
            }
        }

        // Delay for 2 seconds, then go to next activity
        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            startActivity(nextActivity.putExtra("Difficulty", difficulty))
            finish()
        }, 2000)
    }

    private fun checkLosingByDeath() {
        if (deadNum >= difficulty.maxDeadAllowed) gameOver(victory = false, reason = "Death")
    }

    private fun checkLosingByEconomy() {
        if (wrongMasks >= difficulty.maxWrongMasks) gameOver(victory = false, reason = "Economy")
    }

    private fun checkVictory() {
        if (unknownCounter == 0) gameOver(true, "Yay!") // the reason argument is not needed here
    }

    private fun startTimer() {
//        Toast.makeText(this,"Starting Timer", Toast.LENGTH_SHORT).show()
        countDownTimer = timer("Day Counter", false, initialDelay = 1000L, 1000L) {
            displayTime(timeLeftSecs)
            if (timeLeftSecs == 0) {
                nightCycle()
                timeLeftSecs = difficulty.dayLengthInMilli.toInt() / 1000
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
        this@GameActivity.runOnUiThread {
            timer.text = minutes.toString().padStart(2, '0') + ":" +
                    seconds.toString().padStart(2, '0')
        }
    }

    // extension function to turn bool to int
    fun Boolean.toInt() = if (this) 1 else 0

    fun showPause() {
        countDownTimer.cancel()
        gameIsRunning = false

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.pause_popup)

        val pauseday = dialog.findViewById(R.id.pauseday) as TextView
        pauseday.text = "Day $daysCounter"

        val pausetime = dialog.findViewById(R.id.pausetime) as TextView
        val remainingtime =
            min(difficulty.dayLengthInMilli.toInt() / 1000, timeLeftSecs + 1) // timeLeftSecs has a delay of 1
        pausetime.text = "Remaining Time $remainingtime"

        val pausex = dialog.findViewById(R.id.pausex) as TextView
        val returngame = dialog.findViewById(R.id.returngame) as Button
        val returnmenu = dialog.findViewById(R.id.returnmenu) as TextView

        pausex.setOnClickListener {
            countDownTimer.cancel()
            startTimer()
            gameIsRunning = true
            dialog.dismiss()
        }

        returngame.setOnClickListener {
            countDownTimer.cancel()
            startTimer()
            gameIsRunning = true
            dialog.dismiss()
        }

        returnmenu.setOnClickListener {
            countDownTimer.cancel()
            startActivity(Intent(this@GameActivity, HomeScreen::class.java))
        }

        dialog.setOnDismissListener {
            countDownTimer.cancel()
            startTimer()
            gameIsRunning = true
            dialog.dismiss()
        }

        dialog.show()

        // Set the dialog to percentage of the screen
        val metrics = resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        dialog.window!!.setLayout(((0.9 * width).toInt()), ((0.9 * height).toInt()))

    }
}



class Person(val row: Int, val col: Int, genderDecision: Boolean) {
    var isSick = false
    var hasMask = false
    var isExposed = false
    var contactNumber = -1
    var isInfectable = true
    var daysInfected = 0
    var isAlive = true
    val gender = if (genderDecision) Gender.FEMALE else Gender.MALE
}

enum class Gender {MALE, FEMALE}