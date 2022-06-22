package com.cst2335.finalproject

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * @author Abdullah Ilgun
 * @since 22.03.2021
 * Main gameplay activity class to provide user questions and choices which are pulled from
 * the internet. Questions are stored in a list view and the user will be able to open
 * whichever question he wants. There will be indicators nearby the questions whether it's
 * correctly answered or not. All the choices are stored and will be passed to the next
 * activity to generate the user's score.
 */
@Suppress("DEPRECATION")
class TriviaGameplayActivity : AppCompatActivity() {

    lateinit var questionAdapter: QuestionAdapter

    val list = mutableListOf<Question>()

    lateinit var progressBar: ProgressBar

    //In order to make the instances accessible by the other classes statically
    companion object {
        var correctAnswers: Int = 0
        var wrongAnswers: Int = 0
        lateinit var questionAmount: String
        lateinit var difficulty: String
        lateinit var type: String
    }

    var companion = Companion

    /**
     * The amount of questions are retrieved from the previous activity, and that amount of
     * questions will be pulled from the internet to fill the list view. The user can go
     * to the next question by the next button, or open a random question from that view.
     * After all the questions are answered, the activity will be changed to TriviaScoreActivity.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trivia_gameplay)

        //Keeping track of the answers. Only needed in this method.
        correctAnswers = 0
        wrongAnswers = 0

        var intent = intent
        questionAmount = intent.getStringExtra("Question Amount").toString()
        difficulty = intent.getStringExtra("Difficulty").toString()
        type = intent.getStringExtra("Type").toString()

        questionAdapter = QuestionAdapter(this,list)

        progressBar = findViewById(R.id.progressBar)

        //Modifying the url according to the user preferences gotten from the previous activity
        val req = Questions()
        val url = "https://opentdb.com/api.php?amount=$questionAmount&difficulty=$difficulty&type=$type"
        req.execute(url) //Getting the data from the Internet

        val submitButton: Button = findViewById(R.id.go)
        submitButton.setOnClickListener {
            if(correctAnswers + wrongAnswers == questionAmount.toInt()) { //Goes to the next page only if all the questions are answered
                val goToScore = Intent(this@TriviaGameplayActivity, TriviaScoreActivity::class.java)
                goToScore.putExtra("Correct Answers", correctAnswers.toString())
                goToScore.putExtra("Wrong Answers", wrongAnswers.toString())
                goToScore.putExtra("Difficulty", difficulty)
                startActivity(goToScore)
            }
            else {
                val toast = Toast.makeText(applicationContext, getString(R.string.cannotSubmit), Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Custom question class to store the questions in objects and pass them to
     * an array (list), and finally make the changes in the view by using an adapter class
     * Stores the question's number, text, choices and the states of whether the question is
     * answered, and answered correct on wrong.
     */
    class Question(
            val questionNumber: Int,
            var questionText: String,
            val firstChoice: String,
            val secondChoice: String,
            val thirdChoice: String,
            val fourthChoice: String,
            var isAnswered: Boolean,
            var state: String)


    /**
     * List adapter class to get the view of the questions.
     */
    class QuestionAdapter(private val activity: Activity, private val list: List<Question>):
            ArrayAdapter<Question>(activity, R.layout.activity_trivia_gameplay) {

        override fun getCount(): Int {
            return list.size
        }

        /**
         * This is the method adapting the whole view of the questions. At the beginning,
         * it sets the question and button texts according to if the type is boolean or
         * multiple choice. Then on button clicks, the buttons are displayed and the question
         * icon is changed indicating if it's correct or not, and lastly the states of the question
         * are changed.
         * Then, in order to save and load the states of the questions, their states are checked
         * and the question is adapted again according to these states. If that hasn't been done,
         * the question views would have been reset every time they're out of the view.
         *
         * P.S Sorry for that the code is too dry, but there were no time to write the code as
         * the reset issue was unexpected, and the whole code should've been written in a
         * short amount of time.
         */
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val context = activity.layoutInflater
            lateinit var rowView: View

            if(type == "multiple") {

                rowView = context.inflate(R.layout.questions_list,null)

                val questionNumber = rowView.findViewById<TextView>(R.id.questionNumber)
                val questionText = rowView.findViewById<TextView>(R.id.questionText)
                val firstChoice = rowView.findViewById<Button>(R.id.firstChoice)
                val secondChoice = rowView.findViewById<Button>(R.id.secondChoice)
                val thirdChoice = rowView.findViewById<Button>(R.id.thirdChoice)
                val fourthChoice = rowView.findViewById<Button>(R.id.fourthChoice)
                var icon: ImageView = rowView.findViewById(R.id.stateImage)

                questionNumber.text = list[position].questionNumber.toString()
                questionText.text = list[position].questionText
                firstChoice.text = list[position].firstChoice
                secondChoice.text = list[position].secondChoice
                thirdChoice.text = list[position].thirdChoice
                fourthChoice.text = list[position].fourthChoice

                firstChoice.setOnClickListener {
                    list[position].state = "Correct"; list[position].isAnswered = true
                    firstChoice.isEnabled = false
                    secondChoice.isEnabled = false
                    thirdChoice.isEnabled = false
                    fourthChoice.isEnabled = false
                    icon.setImageResource(R.drawable.correct)
                    correctAnswers++
                }
                secondChoice.setOnClickListener {
                    list[position].state = "Wrong"; list[position].isAnswered = true
                    firstChoice.isEnabled = false
                    secondChoice.isEnabled = false
                    thirdChoice.isEnabled = false
                    fourthChoice.isEnabled = false
                    icon.setImageResource(R.drawable.wrong)
                    wrongAnswers++
                }
                thirdChoice.setOnClickListener {
                    list[position].state = "Wrong"; list[position].isAnswered = true
                    firstChoice.isEnabled = false
                    secondChoice.isEnabled = false
                    thirdChoice.isEnabled = false
                    fourthChoice.isEnabled = false
                    icon.setImageResource(R.drawable.wrong)
                    wrongAnswers++
                }
                fourthChoice.setOnClickListener {
                    list[position].state = "Wrong"; list[position].isAnswered = true
                    firstChoice.isEnabled = false
                    secondChoice.isEnabled = false
                    thirdChoice.isEnabled = false
                    fourthChoice.isEnabled = false
                    icon.setImageResource(R.drawable.wrong)
                    wrongAnswers++
                }

                if(list[position].isAnswered) {
                    firstChoice.isEnabled = false
                    secondChoice.isEnabled = false
                    thirdChoice.isEnabled = false
                    fourthChoice.isEnabled = false
                    if(list[position].state == "Correct") {
                        icon.setImageResource(R.drawable.correct)
                    } else {
                        icon.setImageResource(R.drawable.wrong)
                    }
                }

            } else {

                rowView = context.inflate(R.layout.questions_list_truefalse,null)

                val questionNumber = rowView.findViewById<TextView>(R.id.questionNumber)
                val questionText = rowView.findViewById<TextView>(R.id.questionText)

                val trueButton = rowView.findViewById<Button>(R.id.trueButton)
                val falseButton = rowView.findViewById<Button>(R.id.falseButton)
                var icon: ImageView = rowView.findViewById(R.id.stateImage)

                questionNumber.text = list[position].questionNumber.toString()
                questionText.text = list[position].questionText

                trueButton.setOnClickListener {

                    list[position].isAnswered = true
                    trueButton.isEnabled = false
                    falseButton.isEnabled = false

                    if(list[position].firstChoice == "True") {
                        correctAnswers++
                        list[position].state = "Correct";
                    } else {
                        wrongAnswers++
                        list[position].state = "Wrong"
                    }

                    if(list[position].state == "Correct") {
                        icon.setImageResource(R.drawable.correct)
                    } else {
                        icon.setImageResource(R.drawable.wrong)
                    }
                }

                falseButton.setOnClickListener {

                    list[position].isAnswered = true
                    trueButton.isEnabled = false
                    falseButton.isEnabled = false

                    if(list[position].firstChoice == "False") {
                        correctAnswers++
                        list[position].state = "Correct"
                    } else {
                        wrongAnswers++
                        list[position].state = "Wrong"
                    }

                    if(list[position].state == "Correct") {
                        icon.setImageResource(R.drawable.correct)
                    } else {
                        icon.setImageResource(R.drawable.wrong)
                    }
                }

                if(list[position].isAnswered) {
                    trueButton.isEnabled = false
                    falseButton.isEnabled = false

                    if(list[position].state == "Correct") {
                        icon.setImageResource(R.drawable.correct)
                    } else {
                        icon.setImageResource(R.drawable.wrong)
                    }
                }
            }
            return rowView
        }
    }

    /**
     * This class is to achieve async task for this activity. It provides connection to the Internet, and pulling the
     * required data in JSON format, then will parse the JSON file and populate the proper elements in the gameplay
     * layout file. An object of this class is used in the gameplay activity class, and it's executed with the
     * URL of the JSON file.
     */
    @Suppress("DEPRECATION")
    inner class Questions : AsyncTask<String, kotlin.Int, String>() {

        var data: String = ""
        lateinit var questionsArray: JSONArray
        lateinit var questionString: String
        lateinit var answerString: String
        lateinit var choiceString: String
        lateinit var choiceArray: List<String>

        /**
         * This method provides the connection to the Internet, and puls the JSON file from it. The data is stored in
         * the questionText variable, and will be passed to on post execute method to populate the layout.
         */
        override fun doInBackground(vararg params: String?): String {

            try {
                val url = URL(params[0])
                val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
                val response: InputStream = urlConnection.inputStream

                publishProgress(20,50,75) //Progress bar

                val bufferedReader: BufferedReader = BufferedReader(InputStreamReader(response))
                var line = ""

                //Gets the data and stored in a string variable which is data
                while(line != null) {
                    line = bufferedReader.readLine()
                    data += line
                }

            } catch (e: Exception) {
                e.run { printStackTrace() }
            }
            return "Done"
        }

        /**
         * This is to show the progress of fetching the data from the Internet
         * by using the progress bar made in the layout.
         */
        override fun onProgressUpdate(vararg value: Int?) {
            progressBar.visibility = View.VISIBLE
            value[0]?.let { progressBar.progress = it }
        }

        /**
         * This method simply takes the data variables which were filled in do in background method and puts their
         * values to the equivalent elements in the layout file.
         */
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            progressBar.visibility = View.INVISIBLE

            //Fixing the special characters presented in a wrong way in the json file
            if(data.contains("&#039;")) data = data.replace("&#039;", "'")
            if(data.contains("&quot;")) data = data.replace("&quot;", "''")
            if(data.contains("&eacute;")) data = data.replace("&eacute;", "é")

            //Converting the data to a json object, and storing it as an array.
            val json = JSONObject(data)
            questionsArray = json.getJSONArray("results")

            for(i in 0 until questionsArray.length()) {
                val question: JSONObject = questionsArray.getJSONObject(i)
                questionString = question.getString("question");
                answerString = question.getString("correct_answer");
                choiceString = question.getString("incorrect_answers");

                //Parsing the json file and storing the each answer in incorrect answers array.
                //It is used for both multiple choice and true false type questions as
                //in the true false questions, there will be an incorrect answers array
                //which will contain one element.
                choiceString = choiceString.substring(2, choiceString.length - 2);
                choiceString = choiceString.replace("\",\"", ",");
                choiceArray = choiceString.split(",");

                //Adding the question objects containing the question text and to the questions list
                if(type == "multiple") {
                    list.add(Question(i+1, questionString, answerString, choiceArray[0], choiceArray[1], choiceArray[2], false, "Neutral"))
                } else {
                    list.add(Question(i+1, questionString, answerString, choiceArray[0], choiceArray[0], choiceArray[0], false, "Neutral"))
                }
            }

            val listView = findViewById<ListView>(R.id.questions)
            listView.adapter = questionAdapter
        }
    }
}
