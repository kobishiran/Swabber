package com.flyingcircus.swabber

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.*
import kotlinx.android.synthetic.main.activity_custom_mode.*
import java.lang.Math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

class CustomMode : AppCompatActivity() {
    var toNewActivity = false  // a flag that is activated if transitioning to another activity
    var toGame = false

    // Music service variables
    private lateinit var swabberThemeService: SwabberMusicService  // nullable for the case that the service was destroyed and created again
    private var themeServiceBound: Boolean = false

    // Text Validation Flags
    private var heightOK = true
    private var widthOK = true
    private var initialSicktOK = true
    private var infectionRadiusOK = true
    private var infectionProbabilityOK = true
    private var deathProbabilityOK = true
    private var maxDeadOK = true

    // Create a service connection object
    private val themeConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as SwabberMusicService.SwabberMusicBinder  // cast the IBinder to the SwabberMusicBinder Class
            swabberThemeService = binder.getService()  // get the service instance
            themeServiceBound = true
            println("CUSTOM MODE: BIND TO SERVICE")
            // initialize music button according to music mute state
            when (swabberThemeService.musicMuted) {
                true -> buttonMusicCustom.setImageResource(R.drawable.music_off)
                else -> buttonMusicCustom.setImageResource(R.drawable.music_on)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            themeServiceBound = false
            println("CUSTOM MODE ON SERVICE DISCON: UNBIND TO SERVICE")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_mode)

        // Set music button listener
        buttonMusicCustom.setOnClickListener {
            println("BUTTON MUSIC CUSTOM PRESSED")
            if (!swabberThemeService.musicMuted) {
                println("BUTTON MUSIC CUSTOM: MUTING MUSIC")
                muteMusicButton()
            } else {
                println("BUTTON MUSIC CUSTOM: UNMUTING MUSIC")
                unmuteMusicButton()
            }
        }

        // Create Text Validators for min/max values
        editBoardHeight.addTextChangedListener(object : TextValidator(editBoardHeight, applicationContext) {
            override fun validate(editText: EditText, text: String) {
                val inputVal : Int
                if (text.isNotEmpty()) {
                    inputVal = text.toInt()
                } else {
                    return
                }
                heightOK = isInRange(3, 20, inputVal)
                when (heightOK) {
                    false -> editBoardHeight.setTextColor(Color.RED)
                    true -> editBoardHeight.setTextColor(Color.WHITE)
                }
            }
        })
        editBoardWidth.addTextChangedListener(object : TextValidator(editBoardWidth, applicationContext) {
            override fun validate(editText: EditText, text: String) {
                val inputVal : Int
                if (text.isNotEmpty()) {
                    inputVal = text.toInt()
                } else {
                    return
                }
                widthOK = isInRange(3, 20, inputVal)
                when (widthOK) {
                    false -> editBoardWidth.setTextColor(Color.RED)
                    true -> editBoardWidth.setTextColor(Color.WHITE)
                }

            }
        })
        editInitialSick.addTextChangedListener(object : TextValidator(editInitialSick, applicationContext) {
            override fun validate(editText: EditText, text: String) {
                val inputVal : Int
                if (text.isNotEmpty()) {
                    inputVal = text.toInt()
                } else {
                    return
                }
                initialSicktOK = isInRange(3, (2 * sqrt(editBoardHeight.text.toString().toFloat() * editBoardWidth.text.toString().toFloat())).roundToInt(), inputVal)
                when (initialSicktOK) {
                    false -> editInitialSick.setTextColor(Color.RED)
                    true -> editInitialSick.setTextColor(Color.WHITE)
                }
            }
        })
        editInfectionRadius.addTextChangedListener(object : TextValidator(editInfectionRadius, applicationContext) {
            override fun validate(editText: EditText, text: String) {
                val inputVal : Int
                if (text.isNotEmpty()) {
                    inputVal = text.toInt()
                } else {
                    return
                }
                infectionRadiusOK = isInRange(1, min(editBoardWidth.text.toString().toInt(), editBoardHeight.text.toString().toInt()), inputVal)
                when (infectionRadiusOK) {
                    false -> editInfectionRadius.setTextColor(Color.RED)
                    true -> editInfectionRadius.setTextColor(Color.WHITE)
                }
            }
        })
        editInfectionProbability.addTextChangedListener(object : TextValidator(editInfectionProbability, applicationContext) {
            override fun validate(editText: EditText, text: String) {
                val inputVal : Float
                if (text.isNotEmpty()) {
                    inputVal = text.toFloat()
                } else {
                    return
                }
                infectionProbabilityOK = isInRange(0.01F, 0.8F, inputVal)
                when (infectionProbabilityOK) {
                    false -> editInfectionProbability.setTextColor(Color.RED)
                    true -> editInfectionProbability.setTextColor(Color.WHITE)
                }
            }
        })
        editDeathProbability.addTextChangedListener(object : TextValidator(editDeathProbability, applicationContext) {
            override fun validate(editText: EditText, text: String) {
                val inputVal : Float
                if (text.isNotEmpty()) {
                    inputVal = text.toFloat()
                } else {
                    return
                }
                deathProbabilityOK = isInRange(0.01F, 0.8F, inputVal)
                when (deathProbabilityOK) {
                    false -> editDeathProbability.setTextColor(Color.RED)
                    true -> editDeathProbability.setTextColor(Color.WHITE)
                }
            }
        })
        editMaxDeadAllowed.addTextChangedListener(object : TextValidator(editMaxDeadAllowed, applicationContext) {
            override fun validate(editText: EditText, text: String) {
                val inputVal : Int
                if (text.isNotEmpty()) {
                    inputVal = text.toInt()
                } else {
                    return
                }
                maxDeadOK = isInRange(1, (0.5 * (editBoardHeight.text.toString().toInt() * editBoardWidth.text.toString().toInt())).roundToInt(), inputVal)
                when (maxDeadOK) {
                    false -> editMaxDeadAllowed.setTextColor(Color.RED)
                    true -> editMaxDeadAllowed.setTextColor(Color.WHITE)
                }
            }
        })

        // Set play music button listener
        buttonPlayCustom.setOnClickListener {
            if (heightOK && widthOK && initialSicktOK && infectionRadiusOK && infectionProbabilityOK && deathProbabilityOK && maxDeadOK) {
                val customDifficulty = Difficulty.EASY
                customDifficulty.difficultyName = "CUSTOM"
                customDifficulty.boardHeight =  editBoardHeight.text.toString().toInt()
                customDifficulty.boardWidth = editBoardWidth.text.toString().toInt()
                customDifficulty.initialSickNum = editInitialSick.text.toString().toInt()
                customDifficulty.dayLengthInMilli = 20_000L
                customDifficulty.infectionRadius = editInfectionRadius.text.toString().toInt()
                customDifficulty.Pdeath = editDeathProbability.text.toString().toFloat()
                customDifficulty.Pinfect = editInfectionProbability.text.toString().toFloat()
                customDifficulty.maxDeadAllowed = editMaxDeadAllowed.text.toString().toInt()
                customDifficulty.maxWrongMasks = 3
                customDifficulty.BMTime = 20F

                startGame(customDifficulty)
            } else {
                Toast.makeText(applicationContext, "Fix incorrect values to play!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        toGame = false  // reset to initial value
        toNewActivity = false  // reset to initial value
        if (!themeServiceBound) {  // bind to the service if not bounded already
            Intent(this, SwabberMusicService::class.java).putExtra("MusicFileName", "swabber_theme").also { intent ->
                bindService(intent, themeConnection, Context.BIND_AUTO_CREATE)
            }
        }
        // if the music is paused, resume it (when returning from paused state). Make sure that the service instance is initialized.
        if (this::swabberThemeService.isInitialized && !swabberThemeService.isRunning) {
            swabberThemeService.resumeMusic()
        }
    }

    override fun onStop() {
        if (toNewActivity) {
            themeServiceBound = false
            println("CUSTOM CHOICE ON STOP: UNBIND TO SERVICE")
            unbindService(themeConnection)
        }
        super.onStop()
    }

    override fun onPause() {
        if (!(toNewActivity or toGame)) swabberThemeService.pauseMusic()  // if leaving the app, pause the music
        super.onPause()
    }

    override fun onDestroy() {
        // Make sure that we unbind from the service when the activity is destroyed (maybe unnecessary, but just in case)
        if (themeServiceBound) {
            themeServiceBound = false
            println("CUSTOM CHOICE ON DESTROY: UNBIND TO SERVICE")
            unbindService(themeConnection)
        }
        super.onDestroy()
    }

    override fun onBackPressed() {
        toNewActivity = true
        super.onBackPressed()
    }

    private fun startGame(difficulty: Difficulty) {
        toGame = true

        // Start the music service to make sure it is not destroyed when this activity is destroyed (the fadeout overlaps
        // with the next activity)
        startService(Intent(this, SwabberMusicService::class.java))

        // unbind the activity from the service to make sure the service is destroyed at the end of the fadeout
        themeServiceBound = false
        println("CUSTOM START GAME: START SERVICE AND UNBIND")
        unbindService(themeConnection)

        // start the fadeout and transition to the game
        swabberThemeService.musicFadeOut()
        startActivity(Intent(this, GameActivity::class.java).putExtra("Difficulty", difficulty))
        finish()
    }

    private fun muteMusicButton() {
        // Update the music button to muted, and mute the music
        buttonMusicCustom.setImageResource(R.drawable.music_off)
        swabberThemeService.muteMusic()
    }

    private fun unmuteMusicButton() {
        // Update the music button to unmuted, and unmute the music
        buttonMusicCustom.setImageResource(R.drawable.music_on)
        swabberThemeService.unmuteMusic()
    }
}