package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val track: String, // e.g., "Software Engineer", "AI/ML Engineer", "Data Analyst", "Product Manager"
    val cgpa: Double,
    val coreSkills: String, // Comma-separated
    val targetCompanies: String, // Comma-separated
    val talentScore: Int = 50,
    val assessmentFeedback: String = "",
    val resumeText: String = "Nitin Jain\nDubai, UAE\nnitinjain2099@gmail.com\n\nOBJECTIVE\nAmbitious BITS Pilani Dubai engineering student aiming for top corporate networks and placement excellence.\n\nEDUCATION\n- BITS Pilani Dubai Campus\n- CGPA: 9.2\n- Track: Software Engineer\n\nSKILLS\n- Kotlin, Java, Python, Android SDK, Jetpack Compose, Git\n\nEXPERIENCE\n- Tech Project Intern\n  Developed offline-first Jetpack Compose applications featuring Room and Clean Architecture.",
    val resumeFileName: String = "nitinjain_resume.txt",
    val linkedInUrl: String = "https://linkedin.com/in/nitinjain",
    val googleEmail: String = "",
    val googleAvatarUrl: String = "",
    val firebasePdfUrl: String? = null,
    val firebaseSynced: Boolean = false
)

@Entity(tableName = "learning_roadmap")
data class LearningRoadmapItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val track: String,
    val estimatedHours: Int = 4
)

@Entity(tableName = "interview_log")
data class InterviewLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val track: String,
    val question: String,
    val answer: String,
    val feedback: String,
    val score: Int, // 1 to 10
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "mentor_message")
data class MentorMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // "user" or "mentor"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class DubaiEvent(
    val id: String,
    val title: String,
    val description: String,
    val date: String,
    val location: String,
    val category: String, // "AI Event" or "Placement Event"
    val bannerUrl: String
)

@Serializable
data class QuizQuestion(
    val questionText: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String
)

@Serializable
data class Quiz(
    val courseTitle: String,
    val questions: List<QuizQuestion>
)

@Entity(tableName = "prep_questions")
data class PrepQuestion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val company: String,
    val field: String,
    val difficulty: String,
    val questionText: String
)

