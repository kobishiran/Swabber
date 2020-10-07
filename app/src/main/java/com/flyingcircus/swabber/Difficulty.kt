package com.flyingcircus.swabber

import java.io.Serializable

enum class Difficulty(
    val boardHeight: Int, val boardWidth: Int, val initialSickNum: Int,
    val dayLengthInMilli: Long, val infectionRadius: Int, val Pdeath: Float,
    val Pinfect: Float, val maxDeadAllowed: Int, val maxWrongMasks: Int): Serializable {

    EASY(
        10, 10, 10, 30_000L,
        1, 0.01F, 0.07F, 5, 3
    ),

    MEDIUM(
        16, 10, 20, 20_000L,
        2, 0.03F, 0.05F, 5, 3
    ),

    HARD(
        16, 16, 30, 15_000L,
        3, 0.04F, 0.05F, 8, 3
    )
}
