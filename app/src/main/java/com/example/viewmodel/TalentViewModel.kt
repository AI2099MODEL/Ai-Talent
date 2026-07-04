package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TalentViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = TalentRepository(db)

    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val roadmapItems: StateFlow<List<LearningRoadmapItem>> = repository.roadmapItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val interviewLogs: StateFlow<List<InterviewLog>> = repository.interviewLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mentorMessages: StateFlow<List<MentorMessage>> = repository.mentorMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Local States
    private val _isAssessing = MutableStateFlow(false)
    val isAssessing = _isAssessing.asStateFlow()

    private val _isMentorLoading = MutableStateFlow(false)
    val isMentorLoading = _isMentorLoading.asStateFlow()

    private val _isInterviewGrading = MutableStateFlow(false)
    val isInterviewGrading = _isInterviewGrading.asStateFlow()

    private val _currentQuestion = MutableStateFlow("")
    val currentQuestion = _currentQuestion.asStateFlow()

    private val _currentGradingResult = MutableStateFlow<Pair<String, Int>?>(null) // Feedback to Score
    val currentGradingResult = _currentGradingResult.asStateFlow()

    init {
        // Load an initial mock question
        loadNewQuestion("Software Engineer")
    }

    fun loadNewQuestion(track: String) {
        val questions = when (track) {
            "AI/ML Engineer" -> listOf(
                "Explain the concept of overfitting. What are the key strategies to identify and prevent it in deep learning models?",
                "What is gradient descent and how does the choice of learning rate affect optimization convergence?",
                "Explain the difference between L1 (Lasso) and L2 (Ridge) regularization. When would you prefer one over the other?"
            )
            "Data Analyst" -> listOf(
                "What is the difference between a LEFT JOIN, RIGHT JOIN, and INNER JOIN in SQL? Give a concrete scenario of when to use each.",
                "How would you design an A/B test to evaluate if a new search engine algorithm improves user engagement?",
                "Describe how you would handle missing or null values in a dataset during the data cleaning process."
            )
            "Product Manager" -> listOf(
                "How would you prioritize product features for a new BITS Dubai student portal when you have limited engineering bandwidth?",
                "Describe a digital product you use daily that has a poor user experience. What specific design decisions would you change?",
                "How would you define and track success metrics for a newly launched career placement platform?"
            )
            else -> listOf(
                "Explain how a HashMap works under the hood in Java/Kotlin. What is a hash collision and how is it resolved?",
                "What is the difference between SQL and NoSQL databases? When would you choose one database architecture over the other?",
                "Describe the difference between a process and a thread. How do you handle race conditions in multi-threaded systems?"
            )
        }
        _currentQuestion.value = questions.random()
        _currentGradingResult.value = null
    }

    fun runAssessment(
        name: String,
        track: String,
        cgpa: Double,
        skills: String,
        companies: String
    ) {
        viewModelScope.launch {
            _isAssessing.value = true
            try {
                repository.runAIAssessment(name, track, cgpa, skills, companies)
                loadNewQuestion(track)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isAssessing.value = false
            }
        }
    }

    fun toggleRoadmapItem(id: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.updateRoadmapItemStatus(id, isCompleted)
        }
    }

    fun sendMentorMessage(msgText: String) {
        if (msgText.trim().isEmpty()) return
        viewModelScope.launch {
            _isMentorLoading.value = true
            try {
                repository.sendMessageToMentor(msgText)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isMentorLoading.value = false
            }
        }
    }

    fun gradeInterviewAnswer(track: String, question: String, answer: String) {
        if (answer.trim().isEmpty()) return
        viewModelScope.launch {
            _isInterviewGrading.value = true
            try {
                val result = repository.evaluateMockInterview(track, question, answer)
                _currentGradingResult.value = result
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isInterviewGrading.value = false
            }
        }
    }

    fun resetData() {
        viewModelScope.launch {
            repository.clearAllData()
            _currentGradingResult.value = null
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TalentViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TalentViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
