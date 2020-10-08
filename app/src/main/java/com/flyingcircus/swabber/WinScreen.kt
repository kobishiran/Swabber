package com.flyingcircus.swabber

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_win_screen.*

class WinScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_win_screen)

        val difficulty = intent.getSerializableExtra("Difficulty") as Difficulty

        // Start same game
        buttonRetryWin.setOnClickListener() {
            startActivity(Intent(this, GameActivity::class.java).putExtra("Difficulty", difficulty))
        }

        // Start new game
        buttonReturnHomeScreenWin.setOnClickListener() {
            startActivity(Intent(this, HomeScreen::class.java))
        }
    }
}