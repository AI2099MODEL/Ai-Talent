package com.example.data

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class TalentRepository(private val db: AppDatabase) {

    val userProfile: Flow<UserProfile?> = db.userDao().getUserProfile()
    val roadmapItems: Flow<List<LearningRoadmapItem>> = db.learningDao().getAllRoadmapItems()
    val interviewLogs: Flow<List<InterviewLog>> = db.interviewDao().getAllInterviewLogs()
    val mentorMessages: Flow<List<MentorMessage>> = db.mentorDao().getAllMessages()

    private val apiService = RetrofitClient.service
    private val apiKey = BuildConfig.GEMINI_API_KEY

    suspend fun saveUserProfile(profile: UserProfile) {
        db.userDao().insertOrUpdateProfile(profile)
    }

    suspend fun updateRoadmapItemStatus(id: Int, isCompleted: Boolean) {
        db.learningDao().updateCompletionStatus(id, isCompleted)
    }

    suspend fun clearAllData() {
        db.learningDao().clearRoadmap()
        db.interviewDao().clearLogs()
        db.mentorDao().clearHistory()
    }

    suspend fun runAIAssessment(
        name: String,
        track: String,
        cgpa: Double,
        skills: String,
        companies: String
    ): String = withContext(Dispatchers.IO) {
        if (apiKey.isEmpty() || apiKey == "YOUR_GEMINI_API_KEY_HERE") {
            // Return placeholder roadmap items and mock assessment if API key is not set
            val defaultItems = listOf(
                LearningRoadmapItem(title = "Core DSA Preparation", description = "Solve 50+ medium LeetCode questions focusing on Arrays, Trees, and Dynamic Programming.", track = track, estimatedHours = 8),
                LearningRoadmapItem(title = "System Design & Architecture", description = "Understand microservices, load balancing, caching mechanisms, and system design patterns.", track = track, estimatedHours = 10),
                LearningRoadmapItem(title = "Mock Interviews & Resume Review", description = "Refine resume layout and participate in peer-to-peer technical mock sessions.", track = track, estimatedHours = 6)
            )
            db.learningDao().clearRoadmap()
            db.learningDao().insertRoadmapItems(defaultItems)
            
            val mockFeedback = "API Key not configured in the Secrets panel. Using local offline guidelines. Based on your profile as a $track with CGPA $cgpa, your focus should be on building core technical projects and mastering interview patterns tailored for $companies."
            db.userDao().insertOrUpdateProfile(
                UserProfile(name = name, track = track, cgpa = cgpa, coreSkills = skills, targetCompanies = companies, talentScore = 65, assessmentFeedback = mockFeedback)
            )
            return@withContext mockFeedback
        }

        val prompt = """
            You are an elite Tech Career Placement Coach at BITS Pilani Dubai Campus.
            A student has submitted their details for talent development assessment:
            - Name: $name
            - Track: $track
            - CGPA: $cgpa
            - Core Skills: $skills
            - Target Companies: $companies

            Please do two things:
            1. Formulate a highly personalized feedback report (max 200 words) assessing their strengths, skill gaps, and custom action items for their target companies. Be direct, encouraging, and BITS-centric if possible.
            2. Generate a list of 4 concrete learning modules (tasks) for their personalized roadmap.

            You MUST output the response in a structured JSON format with EXACTLY these keys:
            {
              "score": <an integer placement readiness score between 10 and 100>,
              "feedback": "<the personalized feedback report>",
              "modules": [
                {
                  "title": "<title of roadmap task 1>",
                  "description": "<actionable explanation of task 1>",
                  "hours": <estimated study hours, e.g. 10>
                },
                ...
              ]
            }
        """.trimIndent()

        try {
            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                generationConfig = GenerationConfig(responseMimeType = "application/json")
            )
            val response = apiService.generateContent(apiKey, request)
            val textResponse = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            
            if (textResponse.isNotEmpty()) {
                val json = Json { ignoreUnknownKeys = true }
                val jsonObj = json.parseToJsonElement(textResponse).jsonObject
                val score = jsonObj["score"]?.jsonPrimitive?.content?.toIntOrNull() ?: 60
                val feedback = jsonObj["feedback"]?.jsonPrimitive?.content ?: "Assessment complete."
                
                val modulesJson = jsonObj["modules"]?.jsonArray
                val items = mutableListOf<LearningRoadmapItem>()
                if (modulesJson != null) {
                    for (m in modulesJson) {
                        val mObj = m.jsonObject
                        val title = mObj["title"]?.jsonPrimitive?.content ?: "Review Core Skills"
                        val desc = mObj["description"]?.jsonPrimitive?.content ?: "Review and practice core concepts."
                        val hours = mObj["hours"]?.jsonPrimitive?.content?.toIntOrNull() ?: 5
                        items.add(LearningRoadmapItem(title = title, description = desc, track = track, estimatedHours = hours))
                    }
                }

                db.learningDao().clearRoadmap()
                if (items.isNotEmpty()) {
                    db.learningDao().insertRoadmapItems(items)
                }

                db.userDao().insertOrUpdateProfile(
                    UserProfile(
                        name = name,
                        track = track,
                        cgpa = cgpa,
                        coreSkills = skills,
                        targetCompanies = companies,
                        talentScore = score,
                        assessmentFeedback = feedback
                    )
                )
                feedback
            } else {
                "Failed to generate assessment. Please try again."
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Error contacting AI Mentor: ${e.message}"
        }
    }

    suspend fun sendMessageToMentor(userMessage: String): String = withContext(Dispatchers.IO) {
        val userMsgEntity = MentorMessage(sender = "user", message = userMessage)
        db.mentorDao().insertMessage(userMsgEntity)

        if (apiKey.isEmpty() || apiKey == "YOUR_GEMINI_API_KEY_HERE") {
            val reply = "Offline response: Your question is noted. To get actual AI mentoring from our Placement engine, please configure your Gemini API Key in the Secrets panel."
            db.mentorDao().insertMessage(MentorMessage(sender = "mentor", message = reply))
            return@withContext reply
        }

        try {
            val systemInstruction = "You are an expert AI Career Mentor for BITS Dubai students. Provide extremely clear, crisp, and direct coaching advice. Keep your response under 150 words."
            
            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = userMessage)))),
                systemInstruction = Content(parts = listOf(Part(text = systemInstruction)))
            )
            val response = apiService.generateContent(apiKey, request)
            val textResponse = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No mentor response."
            
            db.mentorDao().insertMessage(MentorMessage(sender = "mentor", message = textResponse))
            textResponse
        } catch (e: Exception) {
            val errorReply = "Error talking to AI Mentor: ${e.message}"
            db.mentorDao().insertMessage(MentorMessage(sender = "mentor", message = errorReply))
            errorReply
        }
    }

    suspend fun evaluateMockInterview(
        track: String,
        question: String,
        answer: String
    ): Pair<String, Int> = withContext(Dispatchers.IO) {
        if (apiKey.isEmpty() || apiKey == "YOUR_GEMINI_API_KEY_HERE") {
            val mockFeedback = "Offline evaluation. Good effort! Practice explaining your technical design choices and focus on accuracy."
            val score = 7
            db.interviewDao().insertInterviewLog(
                InterviewLog(track = track, question = question, answer = answer, feedback = mockFeedback, score = score)
            )
            return@withContext Pair(mockFeedback, score)
        }

        val prompt = """
            You are evaluating a candidate's response to an interview question for the track: $track.
            Question: $question
            Candidate's Answer: $answer

            Please grade this answer carefully. Provide constructive, direct feedback (max 100 words) and a numerical score out of 10.
            You MUST return a structured JSON response with EXACTLY these keys:
            {
              "score": <integer from 1 to 10>,
              "feedback": "<your constructive feedback text>"
            }
        """.trimIndent()

        try {
            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                generationConfig = GenerationConfig(responseMimeType = "application/json")
            )
            val response = apiService.generateContent(apiKey, request)
            val textResponse = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            
            if (textResponse.isNotEmpty()) {
                val json = Json { ignoreUnknownKeys = true }
                val jsonObj = json.parseToJsonElement(textResponse).jsonObject
                val score = jsonObj["score"]?.jsonPrimitive?.content?.toIntOrNull() ?: 6
                val feedback = jsonObj["feedback"]?.jsonPrimitive?.content ?: "No feedback generated."
                
                db.interviewDao().insertInterviewLog(
                    InterviewLog(track = track, question = question, answer = answer, feedback = feedback, score = score)
                )
                Pair(feedback, score)
            } else {
                Pair("Failed to evaluate.", 5)
            }
        } catch (e: Exception) {
            Pair("Error grading: ${e.message}", 5)
        }
    }
}
