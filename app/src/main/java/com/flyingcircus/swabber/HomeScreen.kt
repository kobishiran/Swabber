package com.flyingcircus.swabber

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_home_screen.*

class HomeScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)

        val difficulty = Difficulty.MEDIUM

        buttonStart.setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java).putExtra("Difficulty", difficulty))
        }
    }


}