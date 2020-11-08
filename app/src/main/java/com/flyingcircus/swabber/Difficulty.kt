package com.flyingcircus.swabber

import java.io.Serializable
import kotlin.random.Random

enum class Difficulty(
    val difficultyName: String,
    val boardHeight: Int, val boardWidth: Int, val initialSickNum: Int,
    val dayLengthInMilli: Long, val infectionRadius: Int, val Pdeath: Float,
    val Pinfect: Float, val maxDeadAllowed: Int, val maxWrongMasks: Int,
    val BMTime: Float): Serializable {

    EASY(
        difficultyName = "EASY",
        boardHeight = 10, boardWidth = 10, initialSickNum = Random.nextInt(10, 14),
        dayLengthInMilli = 20_000L,
        infectionRadius = 1, Pdeath = 0.01F, Pinfect = 0.07F,
        maxDeadAllowed = 5, maxWrongMasks = 3, BMTime = 20F
    ),

    MEDIUM(
        difficultyName = "MEDIUM",
        boardHeight = 16, boardWidth = 10, initialSickNum = Random.nextInt(17, 22),
        dayLengthInMilli = 20_000L,
        infectionRadius = 2, Pdeath = 0.03F, Pinfect = 0.05F,
        maxDeadAllowed = 5, maxWrongMasks = 2, BMTime = 40F
    ),

    HARD(
        difficultyName = "HARD",
        boardHeight = 18, boardWidth = 13, initialSickNum = Random.nextInt(24, 29),
        dayLengthInMilli = 20_000L,
        infectionRadius = 3, Pdeath = 0.04F, Pinfect = 0.05F,
        maxDeadAllowed = 8, maxWrongMasks = 2, BMTime = 80F
    ),

/*    CUSTOM_GAME( // TODO not updated
    32, 16, 30, 15_000L,
    3, 0.04F, 0.05F, 8, 3
    ),*/
}
