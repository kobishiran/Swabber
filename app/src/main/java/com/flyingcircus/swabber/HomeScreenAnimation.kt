package com.flyingcircus.swabber

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity


class HomeScreenAnimation : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen_animation)

        // Show "animation" for 1 second
        Handler().postDelayed(Runnable {
            val mainIntent = Intent(this, HomeScreen::class.java)
            this.startActivity(mainIntent)
            this.finish()
        }, 1000)
    }

    fun displayAnimation() {
        // TODO make opening animation - virus coughing
    }

}
