package com.flyingcircus.swabber

import java.io.Serializable

enum class Difficulty(
    val difficultyName: String,
    val boardHeight: Int, val boardWidth: Int, val initialSickNum: Int,
    val dayLengthInMilli: Long, val infectionRadius: Int, val Pdeath: Float,
    val Pinfect: Float, val maxDeadAllowed: Int, val maxWrongMasks: Int,
    val BMTime: Float): Serializable {

    EASY(
        "EASY",
        10, 10, 1, 20_000L,
        1, 0.01F, 0.07F, 5, 3, 20F
    ),

    MEDIUM(
        "MEDIUM",
        16, 10, 18, 20_000L,
        2, 0.03F, 0.05F, 5, 3, 40F
    ),

    HARD(
        "HARD",
        18, 13, 27, 20_000L,
        3, 0.04F, 0.05F, 8, 3, 80F
    ),

/*    CUSTOM_GAME( // TODO not updated
    32, 16, 30, 15_000L,
    3, 0.04F, 0.05F, 8, 3
    ),*/
}
