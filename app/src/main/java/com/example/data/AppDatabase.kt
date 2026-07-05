package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)
}

@Dao
interface LearningDao {
    @Query("SELECT * FROM learning_roadmap ORDER BY id ASC")
    fun getAllRoadmapItems(): Flow<List<LearningRoadmapItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoadmapItems(items: List<LearningRoadmapItem>)

    @Query("UPDATE learning_roadmap SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateCompletionStatus(id: Int, isCompleted: Boolean)

    @Query("DELETE FROM learning_roadmap")
    suspend fun clearRoadmap()
}

@Dao
interface InterviewDao {
    @Query("SELECT * FROM interview_log ORDER BY timestamp DESC")
    fun getAllInterviewLogs(): Flow<List<InterviewLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInterviewLog(log: InterviewLog)
    
    @Query("DELETE FROM interview_log")
    suspend fun clearLogs()
}

@Dao
interface MentorDao {
    @Query("SELECT * FROM mentor_message ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<MentorMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MentorMessage)

    @Query("DELETE FROM mentor_message")
    suspend fun clearHistory()
}

@Dao
interface PrepQuestionDao {
    @Query("SELECT * FROM prep_questions WHERE company = :company AND field = :field AND difficulty = :difficulty")
    suspend fun getQuestions(company: String, field: String, difficulty: String): List<PrepQuestion>

    @Query("SELECT * FROM prep_questions WHERE field = :field AND difficulty = :difficulty")
    suspend fun getQuestionsByFieldAndDifficulty(field: String, difficulty: String): List<PrepQuestion>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<PrepQuestion>)

    @Query("SELECT COUNT(*) FROM prep_questions")
    suspend fun getCount(): Int

    @Query("DELETE FROM prep_questions")
    suspend fun clearQuestions()
}

@Database(
    entities = [UserProfile::class, LearningRoadmapItem::class, InterviewLog::class, MentorMessage::class, PrepQuestion::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun learningDao(): LearningDao
    abstract fun interviewDao(): InterviewDao
    abstract fun mentorDao(): MentorDao
    abstract fun prepQuestionDao(): PrepQuestionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "talent_dev_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
