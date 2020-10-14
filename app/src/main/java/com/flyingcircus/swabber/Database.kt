package com.flyingcircus.swabber

import android.content.Context
import androidx.room.*
import androidx.room.Database
import java.io.Serializable

// This is the actual table that stores the data. Each row is a single high score, and each column is one of the table
// variables: Position, Player Name, Score, Date
@Entity(primaryKeys = arrayOf("Difficulty", "Position"), indices = arrayOf(Index(value = ["Difficulty", "Position"], unique = true)))  // setting unique to true to allow only one score at each position
data class Score (
    @ColumnInfo(name = "Difficulty") var difficulty: String,
    @ColumnInfo(name = "Position") var position: Int,
    @ColumnInfo(name = "Player") var player_name: String,
    @ColumnInfo(name = "Score") var score: Int,
    @ColumnInfo(name = "Date") var date: String
    // TODO: Add BoardSize, total_elapsed_time, number_of_dead, number_of_wrong_masks ?
) : Serializable

// This is the DAO - Data Access Object. This is where you define the methods (that are actually SQL Queries) to access
// the data, like searching, deleting or inserting new data.
@Dao
interface ScoresDao {
    companion object {
        const val topScoresNum = 5
    }
    // insert a new score log into the scoreboard
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNewHighScore(highscore: Score)

    // delete all scores
    @Query("DELETE FROM score WHERE Difficulty == :difficulty")
    fun deleteAll(difficulty: String)

    // get all scores
    @Query("SELECT * FROM score ORDER BY Position ASC")
    fun loadAllScores(): Array<Score>

    // get a specific score by its position (only the score itself)
    @Query("SELECT Score FROM score WHERE Difficulty == :difficulty AND Position == :position")
    fun getScoreAtPosition(difficulty: String, position: Int): Int

    // get full score details in a specific position
    @Query("SELECT * FROM score WHERE Difficulty == :difficulty AND Position == :position")
    fun getFullScoreAtPosition(difficulty: String, position: Int): Score

    // get top scores of a specific difficulty
    @Query("SELECT * FROM score WHERE Difficulty == :difficulty ORDER BY Position ASC")
    fun getTopScores(difficulty: String): Array<Score>

    // get all the scores lower then a certain score, to check for a new highscore
    @Query("SELECT * FROM score WHERE Difficulty == :difficulty AND Score < (:score - 1)")
    fun getScoresLowerThen(difficulty: String, score: Int): Array<Score>
}

// This is the Database object. It stores the DAO in it, from which you can access and change the data.
// It is also defined as a "Singleton" - meaning that only one instance of it can exist in the whole app
// at any given time. This is because creating a database instance is an expensive operation.
@Database(entities = arrayOf(Score::class), version = 1, exportSchema = false)
abstract class ScoreDatabase : RoomDatabase() {
    abstract fun scoresDao(): ScoresDao

    // Defining a companion object ("static method") to make sure the database is a Singleton - only create a database instance
    // if another one doesn't already exist
    companion object {
        @Volatile
        private var INSTANCE: ScoreDatabase? = null

        fun getDatabase(context: Context): ScoreDatabase {
            // check if the database currently exists in the app
            val tempInstance = INSTANCE
            if (tempInstance != null) {  // if the database exists
                return tempInstance
            }
            synchronized(this) {  // no idea what this is, but if it doesn't exist, create it
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        ScoreDatabase::class.java,
                        "High Scores"

//                    .createFromAsset("database/scores.db")
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}

