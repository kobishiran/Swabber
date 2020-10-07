package com.flyingcircus.swabber

import android.app.ProgressDialog.show
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_home_screen.*

class HomeScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)

        lateinit var difficulty: Difficulty
        buttonStart.setOnClickListener {
            val radioGroupMode: RadioGroup = findViewById(R.id.radioGroupMode)
            val radioID = radioGroupMode.checkedRadioButtonId // if not check return id = -1
            if (radioID != -1) {
                val selectedButton: RadioButton = findViewById(radioID)

                when (selectedButton.text) {
                    getString(R.string.outbreak)    -> difficulty = Difficulty.EASY
                    getString(R.string.epidemic)    -> difficulty = Difficulty.MEDIUM
                    getString(R.string.pandemic)    -> difficulty = Difficulty.HARD
                    getString(R.string.custom_game) -> {
                        difficulty = Difficulty.EASY
                        Toast.makeText(this, "סבלנות חחח עוד לא פיתחנו...\n תשחק בינתיים ב OUTBREAK", Toast.LENGTH_SHORT).show(); //difficulty = Difficulty.CUSTOM_GAME
                    }
                }
                startActivity(Intent(this, GameActivity::class.java).putExtra("Difficulty", difficulty))
            }
            else Toast.makeText(this, "Please select game mode", Toast.LENGTH_SHORT).show()
        }
    }
}