package com.flyingcircus.swabber

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_lose_screen.*

open class LoseScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lose_screen)

        val difficulty = intent.getSerializableExtra("Difficulty") as Difficulty

        // Start same game
        buttonRetryLose.setOnClickListener() {
            startActivity(Intent(this, GameActivity::class.java).putExtra("Difficulty", difficulty))
            finish()
        }

        // Start new game
        buttonReturnHomeScreenLose.setOnClickListener() {
            finish()
        }
    }
}