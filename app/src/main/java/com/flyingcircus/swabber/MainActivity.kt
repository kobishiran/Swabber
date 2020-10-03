package com.flyingcircus.swabber

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
    // Check
//    Hello Kobi
    // Hi there
    // Bye

    fun printWorld() {
        println("World")
    }
}