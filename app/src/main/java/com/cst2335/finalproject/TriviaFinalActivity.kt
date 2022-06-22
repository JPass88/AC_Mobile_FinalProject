package com.cst2335.finalproject

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*

/**
 * @author Abdullah Ilgun
 * @since 22.03.2021
 * The last activity of the trivia part of the app. The high scores are stored in a list view
 * through a database, and the user can delete his score from the list.
 */
class TriviaFinalActivity : AppCompatActivity() {

    private val players: ArrayList<Players> = ArrayList()
    lateinit var playersAdapter: PlayersAdapter
    private lateinit var database: SQLiteDatabase

    lateinit var playerName: String
    lateinit var difficulty: String
    lateinit var score: String

    /**
     * The user's name is gotten from the previous activity, and will be put into the
     * high score list. When the user wants to delete his score, an alert dialog will pop
     * up to make sure if the user wants to delete.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trivia_final)

        playersAdapter = PlayersAdapter(this, players)

        var intent = intent
        playerName = intent.getStringExtra("Player name").toString()
        difficulty = intent.getStringExtra("Difficulty").toString()
        score = intent.getStringExtra("Score").toString()

        loadData()

        val listView = findViewById<ListView>(R.id.scoreBoard)
        listView.adapter = playersAdapter

        val newRowValues = ContentValues()

        //Adding the player to the players array from the values gotten from the previous activity,
        //and notifying the adapter to change the listview.
        val newId: Long = database.insert(DbHelper.TABLE_NAME, null, newRowValues)
        val newPlayer = Players(newId, playerName, difficulty, score.toLong())
        players.add(newPlayer)
        playersAdapter.notifyDataSetChanged()

        //To delete the user as well as seeing his score and difficulty played
        listView.setOnItemLongClickListener { _, _, position, id ->

            val alertDialogBuilder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)

            var playerToShow = players[position]
            var nameToShow = playerToShow.name.toString()
            var difficultyToShow = playerToShow.difficulty.toString()
            var scoreToShow = playerToShow.score.toString()

            val difficultyPlayedText = getString(R.string.difficultyPlayed)
            val scoreText = getString(R.string.score)
            val deleteText = getString(R.string.delete)
            val cancelText = getString(R.string.cancel)

            alertDialogBuilder.setTitle(nameToShow)
            alertDialogBuilder.setMessage("$difficultyPlayedText $difficultyToShow\n$scoreText $scoreToShow")

            alertDialogBuilder.setPositiveButton(deleteText) { _, _ ->

                database.delete(DbHelper.TABLE_NAME, "_id=?", arrayOf(id.toString())) //Deleting the instance from the database
                players.removeAt(position) //Deleting the instance from the array
                playersAdapter.notifyDataSetChanged() //Changing the listview
            }
            alertDialogBuilder.setNegativeButton(cancelText) { _, _ -> } //Nothing happens

            alertDialogBuilder.create().show(); true
        }

        val mainMenuButton: Button = findViewById(R.id.goToMain)
        mainMenuButton.setOnClickListener {
            val goToMain = Intent(this@TriviaFinalActivity, TriviaMainActivity::class.java)
            startActivity(goToMain)
        }
    }

    /**
     * This class is made to create player objects to populate the players array, and
     * holding the instances' id, name, difficulty played and score values.
     */
    class Players(
            val id: Long,
            val name: String,
            val difficulty: String,
            val score: Long )

    /**
     * This function is to load the database in the on create function by calling it once.
     * Each of the row is gotten from the database and added to the players array
     * one by one. Also, the print cursor function is called to help the developer to see
     * if all the database info is proper.
     */
    private fun loadData() {

        val dbHelper = DbHelper(this)
        database = dbHelper.writableDatabase
        val columns = arrayOf<String>(DbHelper.COL_ID, DbHelper.COL_PLAYER_NAME, DbHelper.COL_DIFFICULTY, DbHelper.COL_SCORE)

        val results: Cursor = database.query(false, dbHelper.companion.TABLE_NAME, columns, null, null, null, null, dbHelper.companion.COL_SCORE, null)
        val playerNameColumnIndex = results.getColumnIndex(dbHelper.companion.COL_PLAYER_NAME)
        val difficultyColumnIndex = results.getColumnIndex(dbHelper.companion.COL_DIFFICULTY)
        val scoreColumnIndex = results.getColumnIndex(dbHelper.companion.COL_SCORE)
        val idColumnIndex = results.getColumnIndex(dbHelper.companion.COL_ID)

        //Adding the new players to the players array
        while (results.moveToNext()) {
            val id = results.getLong(idColumnIndex)
            val playerName = results.getString(playerNameColumnIndex)
            val difficulty = results.getString(difficultyColumnIndex)
            val score = results.getLong(scoreColumnIndex)
            players.add(Players(id, playerName, difficulty, score))
        }
        this.printCursor(results, database.version)
    }

    /**
     * This function is just to help the developer to see if the data are stored
     * properly to the database by log statements.
     */
    private fun printCursor(cursor: Cursor, version: Int) {

        if (cursor.moveToFirst()) {

            val columnNames: Array<String> = cursor.columnNames

            //Statements to print the general database info
            Log.e("Database Version Number: ", version.toString())
            Log.e("Number of columns: ", cursor.columnCount.toString())
            Log.e("Column names: ", columnNames.toString())
            Log.e("Number of rows: ", cursor.count.toString())

            val playerName = cursor.getColumnIndex(DbHelper.COL_PLAYER_NAME)
            val playerDifficulty = cursor.getColumnIndex(DbHelper.COL_DIFFICULTY)
            val playerScore = cursor.getColumnIndex(DbHelper.COL_SCORE)

            //Prints all the players' data stored in the database
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                Log.e("ScoreBoard", "Name: " + cursor.getString(playerName) +"\nDifficulty: "
                        + cursor.getString(playerDifficulty) + "\nScore: " + cursor.getString(playerScore))
                cursor.moveToNext()
            }
        }
    }

    /**
     * Adapter class to change the listview according to the player values in the players array.
     * Takes the current activity and players array as parameters and extends array adapter class
     * to use its functions.
     */
    class PlayersAdapter(private val activity: Activity, private val players: List<Players>):
            ArrayAdapter<Players>(activity, R.layout.activity_trivia_final) {

        /**
         * Manipulates the row in the high score table by the player's name which is
         * in the certain position in the players array, and finally returns that row.
         */
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

            val context = activity.layoutInflater
            val rowView = context.inflate(R.layout.players_list,null)

            val playerName = rowView.findViewById<TextView>(R.id.playerName)

            playerName.text = players[position].name

            return rowView
        }

        /**
         * Returns the player in the certain position of the players array
         */
        override fun getItem(position: Int): Players {
            return players[position]
        }

        /**
         * Returns the id of the player in the certain position of the players array
         */
        override fun getItemId(position: Int): Long {
            return getItem(position).id
        }

        /**
         * Returns the amount of players stored in the database
         */
        override fun getCount(): Int {
            return players.size
        }
    }

    /**
     * This class is to create and open the player database by using its instance. Includes
     * a companion object to access its table name, player name etc. instances statically.
     */
    class DbHelper(context: Context) : SQLiteOpenHelper(context, "PLAYERS", null, 1) {

        companion object {
            public const val DATABASE_NAME = "ScoreBoard"
            public const val VERSION_NUM = 1
            public const val TABLE_NAME = "Players"
            public const val COL_PLAYER_NAME = "Name"
            public const val COL_DIFFICULTY = "Difficulty"
            public const val COL_SCORE = "Score"
            public const val COL_ID = "_id"
        }

        var companion = Companion //To access the table name

        /**
         * This creates the database with auto increment primary key, player name,
         * difficulty, and score attributes.
         */
        override fun onCreate(db: SQLiteDatabase?) {
            db?.execSQL("CREATE TABLE $TABLE_NAME(_id INTEGER PRIMARY KEY AUTOINCREMENT, $COL_PLAYER_NAME TEXT, $COL_DIFFICULTY TEXT, $COL_SCORE INTEGER);")
            //In order to test if the insert queries work
//            db?.execSQL("INSERT INTO $TABLE_NAME($COL_PLAYER_NAME, $COL_DIFFICULTY, $COL_SCORE) VALUES ('Apo', 'Easy', '31')")
//            db?.execSQL("INSERT INTO $TABLE_NAME($COL_PLAYER_NAME, $COL_DIFFICULTY, $COL_SCORE) VALUES ('Bepo', '69', 'Med')")
        }

        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            db!!.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
            onCreate(db)
        }
    }
}

