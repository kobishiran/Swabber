package com.flyingcircus.swabber

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_lose_screen.*

class LoseDeath : LoseScreen() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lose_background.setBackgroundResource(R.drawable.lose_death_screen)
    }
}