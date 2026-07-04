package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val track: String, // e.g., "Software Engineer", "AI/ML Engineer", "Data Analyst", "Product Manager"
    val cgpa: Double,
    val coreSkills: String, // Comma-separated
    val targetCompanies: String, // Comma-separated
    val talentScore: Int = 50,
    val assessmentFeedback: String = ""
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
