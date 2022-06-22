package com.cst2335.finalproject

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

/**
 * @author Abdullah Ilgun
 * @since 22.03.2021
 * Simple activity to show user his score as well as require his name to pass it to the
 * final activity.
 */
class TriviaScoreActivity : AppCompatActivity() {

    lateinit var playerName: String
    //GUI components
    lateinit var submitButton: Button
    lateinit var nameText: EditText
    lateinit var stats: TextView

    var score = 0

    /**
     * The correct and wrong answers are gotten from the gameplay activity, score is
     * calculated and displayed to the user. Also, there's an input field for user to
     * put player name, and it will be passed to the final activity for the high score
     * board.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trivia_score)

        //Getting the values from the previous activity

        var intent = intent
        var correctAnswers = intent.getStringExtra("Correct Answers")
        var correctAnswerAmount = correctAnswers?.toInt()

        var wrongAnswers = intent.getStringExtra("Wrong Answers")
        var wrongAnswerAmount = wrongAnswers?.toInt()

        var difficultyPlayed = intent.getStringExtra("Difficulty")

        //Calculating the player's score according to the difficulty played
        when(difficultyPlayed) {
            "easy" -> {
                score = correctAnswerAmount!! * 100 - wrongAnswerAmount!! * 25
            }
            "medium" -> {
                score = correctAnswerAmount!! * 200 - wrongAnswerAmount!! * 25
            }
            "hard" -> {
                score = correctAnswerAmount!! * 300 - wrongAnswerAmount!! * 25
            }
        }

        stats = findViewById<TextView>(R.id.stats)
        submitButton = findViewById<Button>(R.id.submitButton)
        nameText = findViewById<EditText>(R.id.nameText)

        val scoreText = getString(R.string.score)
        val correctText = getString(R.string.correct)
        val wrongText = getString(R.string.wrong)

        stats.text = "$scoreText $score\n$correctText $correctAnswerAmount\n$wrongText $wrongAnswerAmount"

        loadData() //Loading the saved username in the shared preference at the last login

        submitButton.setOnClickListener {
            saveData() //Saves the username to the shared preference
            val goToFinalScreen = Intent(this@TriviaScoreActivity, TriviaFinalActivity::class.java)
            goToFinalScreen.putExtra("Player name", playerName)
            goToFinalScreen.putExtra("Score", score.toString())
            goToFinalScreen.putExtra("Difficulty", difficultyPlayed)
            startActivity(goToFinalScreen)
        }
    }

    /**
     * This method is to save the player name data to a shared preference to display it for the next time
     * when the user opens the app. It's called when the user clicks on the submit button.
     */
    private fun saveData() {
        playerName = nameText.text.toString()
        val sharedPreferences = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.apply {
            putString("STRING_KEY", playerName)
        }.apply()
    }

    /**
     * This method simply to load the player name value from the shared preference which is filled in the save data
     * method. It's called at the beginning of on create method when the app launches.
     */
    private fun loadData() {
        val sharedPreferences = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val savedName = sharedPreferences.getString("STRING_KEY", null)

        nameText.setText(savedName)
    }
}