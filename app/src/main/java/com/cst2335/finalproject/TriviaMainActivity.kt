package com.cst2335.finalproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.widget.Toolbar
import com.google.android.material.snackbar.Snackbar
import kotlin.system.exitProcess

/**
 * @author Abdullah Ilgun
 * @since 22.03.2021
 * Main activity class to give user a choice to select the number, type and difficulty
 * of questions. One of two gameplay layouts will be loaded according to the type of questions,
 * and number of questions will be passed to the TriviaGameplayActivity to generate a listview
 * accordingly.
 */
class TriviaMainActivity : AppCompatActivity() {

    //Defauly values
    var questionType = "multiple"
    var difficulty = "easy"

    lateinit var questionAmount: EditText

    /**
     * Main function of the activity. Includes spinners, buttons, number input field,
     * as well as a toast message is generated if the number of questions is out of limit,
     * snackbar is generated when the difficulty is changed, and an alert dialog pops
     * up when the app is tried to be closed.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trivia_main)

        //String files to be used
        val typeBoolean: String = getString(R.string.typeBoolean)
        val typeMultiple: String = getString(R.string.typeMultiple)
        val diffEasy: String = getString(R.string.easy)
        val diffMedium: String = getString(R.string.medium)
        val diffHard: String = getString(R.string.hard)
        val validationWarning: String = getString(R.string.validationError)
        val closeAppText: String = getString(R.string.closeAppText)
        val noText: String = getString(R.string.no)
        val yesText: String = getString(R.string.yes)

        //Creation of the toolbar for help menu
        val tBar = findViewById<View>(R.id.myToolbar) as Toolbar
        setSupportActionBar(tBar)

        val startButton = findViewById<Button>(R.id.startButton)
        val exitButton = findViewById<Button>(R.id.exitButton)

        questionAmount = findViewById(R.id.numSpinner)

        //Question type spinner
        val typeOption = findViewById<Spinner>(R.id.typeSpinner)
        val typeOptions = arrayOf(typeBoolean, typeMultiple)
        val typeAdapter = ArrayAdapter(this@TriviaMainActivity, android.R.layout.simple_dropdown_item_1line, typeOptions)
        typeOption.adapter = typeAdapter

        typeOption.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                questionType = "multiple"
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                questionType = if(typeOptions[position] == typeMultiple) "multiple"
                else "boolean"
            }
        }

        //Question difficulty spinner
        val diffOption = findViewById<Spinner>(R.id.diffSpinner)
        val diffOptions = arrayOf(diffEasy, diffMedium, diffHard)
        val diffAdapter = ArrayAdapter(this@TriviaMainActivity, android.R.layout.simple_dropdown_item_1line, diffOptions)
        diffOption.adapter = diffAdapter

        diffOption.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                difficulty = "easy"
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when(diffOptions[position]) {
                    diffEasy -> difficulty = "easy"
                    diffMedium -> difficulty = "medium"
                    diffHard -> difficulty = "hard"
                }
                var snackbar = Snackbar.make(diffOption, "Difficulty is changed to $difficulty", Snackbar.LENGTH_LONG).show()
            }
        }

        startButton?.setOnClickListener {

            var numberOfQuestions = findViewById<EditText>(R.id.numSpinner).text.toString().toInt()

            if(numberOfQuestions <= 0 || numberOfQuestions > 10){
                val toast = Toast.makeText(applicationContext, validationWarning, Toast.LENGTH_LONG).show()
            } else {
                //Passing all the necessary values to the next activity
                val goToGamePlay = Intent(this@TriviaMainActivity, TriviaGameplayActivity::class.java)
                goToGamePlay.putExtra("Question Amount", questionAmount.text.toString())
                goToGamePlay.putExtra("Difficulty", difficulty)
                goToGamePlay.putExtra("Type", questionType)

                startActivity(goToGamePlay)
            }
        }
        exitButton?.setOnClickListener {
            val alertDialogBuilder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)

            alertDialogBuilder.setTitle(closeAppText)

            alertDialogBuilder.setPositiveButton(yesText) { _, _ ->
                val goToMain = Intent(this@TriviaMainActivity, MainActivity::class.java)
                startActivity(goToMain)

            }
            alertDialogBuilder.setNegativeButton(noText) { _, _ -> } //Nothing happens

            alertDialogBuilder.create().show(); true
        }
    }

    /**
     * Creates the options menu when the app started by using the trivia_menu layout created
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.trivia_menu, menu)
        return true
    }

    /**
     * There is only one item of the menu which is help. When it is clicked,
     * an alert dialog pops up showing the description of how to use the API.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.help -> {

                val howToPlayText: String = getString(R.string.howToPlay)
                val helpDescription: String = getString(R.string.helpDescription)
                val closeText: String = getString(R.string.close)

                val alertDialogBuilder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)

                alertDialogBuilder.setTitle(howToPlayText)
                alertDialogBuilder.setMessage(helpDescription)

                alertDialogBuilder.setNeutralButton(closeText) {_, _ -> } //Nothing happens

                alertDialogBuilder.create().show(); true
            }
        }

        return super.onOptionsItemSelected(item)
    }
}