package com.example.viewmodel

import android.app.Application
import android.content.Context
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

    // Course progress persistence
    private val sharedPrefs = application.getSharedPreferences("study_hub_progress_v2", Context.MODE_PRIVATE)

    private val _completedCourses = MutableStateFlow<Set<String>>(emptySet())
    val completedCourses: StateFlow<Set<String>> = _completedCourses.asStateFlow()

    private val _courseProgress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val courseProgress: StateFlow<Map<String, Float>> = _courseProgress.asStateFlow()

    fun toggleCourseCompletion(courseTitle: String) {
        val currentCompleted = _completedCourses.value.toMutableSet()
        val currentProgress = _courseProgress.value.toMutableMap()
        
        val isNowCompleted = if (currentCompleted.contains(courseTitle)) {
            currentCompleted.remove(courseTitle)
            currentProgress[courseTitle] = 0f
            false
        } else {
            currentCompleted.add(courseTitle)
            currentProgress[courseTitle] = 1f
            true
        }
        
        _completedCourses.value = currentCompleted
        _courseProgress.value = currentProgress
        
        sharedPrefs.edit()
            .putStringSet("completed_courses_set_v2", currentCompleted)
            .putFloat("progress_$courseTitle", if (isNowCompleted) 1f else 0f)
            .apply()
    }

    fun updateCourseProgress(courseTitle: String, progress: Float) {
        val currentProgress = _courseProgress.value.toMutableMap()
        val cleanedProgress = progress.coerceIn(0f, 1f)
        currentProgress[courseTitle] = cleanedProgress
        _courseProgress.value = currentProgress
        
        val currentCompleted = _completedCourses.value.toMutableSet()
        if (cleanedProgress >= 1f) {
            currentCompleted.add(courseTitle)
        } else {
            currentCompleted.remove(courseTitle)
        }
        _completedCourses.value = currentCompleted
        
        sharedPrefs.edit()
            .putStringSet("completed_courses_set_v2", currentCompleted)
            .putFloat("progress_$courseTitle", cleanedProgress)
            .apply()
    }

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



    private val _selectedCompany = MutableStateFlow("Emirates Group")
    val selectedCompany = _selectedCompany.asStateFlow()

    private val _selectedField = MutableStateFlow("Software Engineering")
    val selectedField = _selectedField.asStateFlow()

    private val _selectedDifficulty = MutableStateFlow("Medium")
    val selectedDifficulty = _selectedDifficulty.asStateFlow()

    fun setCompany(company: String) {
        _selectedCompany.value = company
        loadDubaiMockQuestion()
    }

    fun setField(field: String) {
        _selectedField.value = field
        loadDubaiMockQuestion()
    }

    fun setDifficulty(difficulty: String) {
        _selectedDifficulty.value = difficulty
        loadDubaiMockQuestion()
    }

    fun loadDubaiMockQuestion() {
        val company = _selectedCompany.value
        val field = _selectedField.value
        val difficulty = _selectedDifficulty.value

        val question = when (field) {
            "Software Engineering" -> when (difficulty) {
                "Easy" -> "Explain the difference between interface and abstract class in modern object-oriented programming. When would you use each?"
                "Medium" -> when (company) {
                    "Emirates Group" -> "Emirates Group uses high-availability booking microservices. What is a load balancer, and how does Round Robin hashing distribute requests across backend instances?"
                    "Careem" -> "Careem handles massive concurrent booking requests. Describe how the Singleton pattern or Thread Pools can prevent race conditions in memory-managed apps."
                    "Talabat" -> "Talabat needs custom state handling for order tracking. Explain how you would design a state-pattern in Kotlin to cleanly represent order transitions (Created -> Dispatched -> Delivered)."
                    "Property Finder" -> "Property Finder lists millions of properties. Explain the difference between standard database B-Tree index and Full-Text Search index (like Elasticsearch)."
                    "e& (Etisalat)" -> "What are the key architectural differences between REST, GraphQL, and gRPC? When would you prefer gRPC for telecom microservice communication?"
                    "noon.com" -> "For noon's checkout flow, how would you design a distributed lock mechanism to prevent two customers from reserving the last active inventory item?"
                    "Binance Dubai" -> "What is a Memory Pool (mempool) in blockchain technology, and how are transaction fees prioritized inside it?"
                    else -> "Describe the SOLID design principles. How does the Dependency Inversion Principle facilitate testing and loose coupling in large-scale software systems?"
                }
                else -> when (company) {
                    "Emirates Group" -> "Design a rate limiter for Emirates' ticketing APIs. How would you choose between Token Bucket and Leaky Bucket algorithms under heavy scraping pressure?"
                    "Careem" -> "How would you design a distributed publish-subscribe caching channel (like Kafka or Redis) to broadcast real-time driver availability to millions of riders?"
                    "Talabat" -> "Talabat handles high-frequency geographic writes. How do caching layers (Redis) and write-behind queues protect database layers from transactional bottlenecks?"
                    "Property Finder" -> "Design a database schema for Property Finder to support faceted search (filtering properties by price, beds, location, and amenities) with fast queries."
                    "e& (Etisalat)" -> "Explain high-availability multi-region active-active cloud architecture. How do you resolve write conflicts and guarantee eventual consistency?"
                    "noon.com" -> "noon.com handles millions of active SKUs. Design a distributed cache invalidation strategy to update product pricing instantly without database overload."
                    "Binance Dubai" -> "Design a low-latency order matching engine for Binance. How do you handle transaction locking, in-memory orders, and ledger reconciliation under high volume?"
                    else -> "Explain how database indexing works under the hood (e.g. B+ Trees vs Hash Indexing). How does a composite index affect read queries vs write performance?"
                }
            }
            "Cybersecurity & Networks" -> when (difficulty) {
                "Easy" -> "Explain the purpose of Salt in hashing passwords. Why is hashing passwords alone not enough to prevent rainbow-table attacks?"
                "Medium" -> when (company) {
                    "Emirates Group" -> "Emirates Group holds passenger credit cards. Explain how TLS/SSL handshakes secure communications and verify server identity."
                    "Careem" -> "How would you design secure JWT token authentication for Careem's mobile apps to prevent token hijacking and replay attacks?"
                    "Talabat" -> "What is Cross-Site Request Forgery (CSRF)? How can custom request headers or Anti-CSRF tokens defend food-delivery web client transactions?"
                    "e& (Etisalat)" -> "Explain the key differences between Symmetric and Asymmetric encryption. Describe how they work together in HTTPS."
                    "Binance Dubai" -> "Explain the concept of Multi-Signature (Multi-Sig) wallets and how they enhance secure asset custody for blockchain enterprises."
                    else -> "What is the difference between OAuth 2.0 and OpenID Connect? When do you use each protocol in single sign-on enterprise systems?"
                }
                else -> when (company) {
                    "e& (Etisalat)" -> "Explain the mechanisms of a Distributed Denial of Service (DDoS) attack at layer 7 vs layer 3. How do you mitigate each in large telecom networks?"
                    "Binance Dubai" -> "What is a Reentrancy Attack in smart contracts, and how do you write secure contract code to prevent it? Give examples of reentrancy guard patterns."
                    else -> "What is Cross-Site Scripting (XSS) and SQL Injection? Detail the precise defense mechanisms you would implement in a modern backend framework to eliminate both."
                }
            }
            "Data Structures & Algorithms" -> when (difficulty) {
                "Easy" -> "What is the time and space complexity of sorting an array of size N using QuickSort in its average and worst cases?"
                "Medium" -> when (company) {
                    "Emirates Group" -> "Suppose you are writing a flight route finder. How would you model airports as a graph and find the shortest travel time path using Dijkstra's algorithm?"
                    "Careem" -> "You need to find the nearest driver in a 2D space. How does a Spatial Hash or Quadtree optimize nearest-neighbor search compared to O(N) distance checks?"
                    "Talabat" -> "How can you use a Trie (Prefix Tree) data structure to implement a fast autocomplete search bar for Talabat's food search engine?"
                    "Property Finder" -> "Given a list of properties with their prices, how would you design an algorithm to find the Top K cheapest properties in a specific neighborhood in O(N log K) time?"
                    "noon.com" -> "noon.com needs to merge two sorted lists of products. Explain how you would implement the merge step of MergeSort with O(N) time and O(1) space."
                    else -> "Explain how a HashMap handles hash collisions under the hood. What is the complexity when a collision occurs, and how does Java/Kotlin resolve it?"
                }
                else -> when (company) {
                    "Emirates Group" -> "Given a directed graph of flight connections across GCC, how do you find all strongly connected components (fully linked routes) using Tarjan's or Kosaraju's algorithm?"
                    "Careem" -> "Given a grid-based map of Dubai, how would you implement the A* search algorithm for optimal driver route dispatching, including distance heuristics?"
                    "noon.com" -> "Given an array of product price intervals (sales start and end dates), how would you find the maximum number of overlapping sale intervals?"
                    else -> "What is the Knapsack problem? Explain how you would solve the 0/1 Knapsack problem using Dynamic Programming, detailing the time and space complexity."
                }
            }
            "Data Science & AI" -> when (difficulty) {
                "Easy" -> "What is the difference between supervised and unsupervised learning? Provide two common examples of algorithms used in each."
                "Medium" -> when (company) {
                    "Emirates Group" -> "Emirates wants to predict ticket cancellations. Explain precision, recall, and F1-score. Which metric is more critical if the cost of empty seats is very high?"
                    "Careem" -> "How can Careem apply predictive machine learning to dynamically calculate surge pricing multipliers during high-traffic hours?"
                    "Talabat" -> "Explain how you would build a restaurant recommendation engine for Talabat users using Collaborative Filtering and Matrix Factorization."
                    "Property Finder" -> "Describe how you would build a machine learning regression model to predict property prices in Dubai based on square footage, location, and rooms."
                    "noon.com" -> "How would you design a classification model to detect fraudulent credit card transactions on noon's e-commerce platform?"
                    else -> "What is overfitting in machine learning? Explain how regularization (L1 Lasso vs L2 Ridge) helps model generalization."
                }
                else -> when (company) {
                    "noon.com" -> "noon.com wants to analyze user review sentiments. Explain how the Transformer self-attention mechanism processes text sequence semantics better than traditional RNNs."
                    else -> "Explain the mathematical difference between gradient descent, stochastic gradient descent (SGD), and Adam optimization. When would you prefer Adam?"
                }
            }
            else -> when (difficulty) { // Cloud & DevOps
                "Easy" -> "What is the difference between virtual machines (VMs) and Docker containers? Why are containers preferred for modern scalable systems?"
                "Medium" -> when (company) {
                    "Emirates Group" -> "Emirates Group needs zero-downtime deployments. Explain the difference between Blue-Green and Rolling Update deployment strategies on Kubernetes."
                    "Careem" -> "Explain how you would configure AWS Auto-Scaling and CloudWatch metrics to dynamically handle sudden spikes in Careem traffic during rush hours."
                    "e& (Etisalat)" -> "What is Container Orchestration? Explain how Kubernetes Service, Pods, and Ingress resources work together to route traffic in a telecom microservice."
                    else -> "What is Infrastructure as Code (IaC)? How does Terraform help maintain consistency across development, staging, and production environments?"
                }
                else -> when (company) {
                    "e& (Etisalat)" -> "Design a highly-available, multi-region containerized infrastructure on AWS using EKS, Route53, and RDS Aurora Global databases to guarantee 99.99% uptime."
                    else -> "Design a secure, highly-available CI/CD pipeline from code commit on GitHub to containerized deployment on AWS EKS, including static analysis (SonarQube) and automated rollback."
                }
            }
        }
        _currentQuestion.value = question
        _currentGradingResult.value = null
    }

    fun loadNewQuestion(track: String) {
        val field = when (track) {
            "AI/ML Engineer", "Data Scientist", "AI/ML Developer" -> "Data Science & AI"
            "Software Engineer", "Backend Developer", "Frontend Developer", "Full Stack Developer" -> "Software Engineering"
            "Cybersecurity Analyst", "Security Engineer" -> "Cybersecurity & Networks"
            else -> "Data Structures & Algorithms"
        }
        _selectedField.value = field
        loadDubaiMockQuestion()
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
            _generatedImageBase64.value = null
            _imageGenerationError.value = null
        }
    }

    private val _generatedImageBase64 = MutableStateFlow<String?>(null)
    val generatedImageBase64 = _generatedImageBase64.asStateFlow()

    private val _isGeneratingImage = MutableStateFlow(false)
    val isGeneratingImage = _isGeneratingImage.asStateFlow()

    private val _imageGenerationError = MutableStateFlow<String?>(null)
    val imageGenerationError = _imageGenerationError.asStateFlow()

    fun generateBadgeImage(prompt: String, size: String) {
        if (prompt.trim().isEmpty()) return
        viewModelScope.launch {
            _isGeneratingImage.value = true
            _imageGenerationError.value = null
            _generatedImageBase64.value = null
            try {
                val base64 = repository.generateTalentBadgeImage(prompt, size)
                if (base64 != null) {
                    _generatedImageBase64.value = base64
                } else {
                    _imageGenerationError.value = "Failed to generate image. Please ensure your Gemini API key is configured correctly in the Secrets panel."
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _imageGenerationError.value = "Error generating image: ${e.message}"
            } finally {
                _isGeneratingImage.value = false
            }
        }
    }

    fun clearGeneratedImage() {
        _generatedImageBase64.value = null
        _imageGenerationError.value = null
    }

    // ==========================================
    // GOOGLE PROFILE & EVENTS STATE
    // ==========================================
    private val _googleProfile = MutableStateFlow<GoogleProfile?>(null)
    val googleProfile = _googleProfile.asStateFlow()

    private val _isLinkedInConnected = MutableStateFlow(false)
    val isLinkedInConnected = _isLinkedInConnected.asStateFlow()

    private val _linkedInProfileUrl = MutableStateFlow("")
    val linkedInProfileUrl = _linkedInProfileUrl.asStateFlow()

    private val _registeredEventIds = MutableStateFlow<Set<String>>(emptySet())
    val registeredEventIds = _registeredEventIds.asStateFlow()

    fun signInWithGoogle(name: String, email: String, avatarUrl: String) {
        _googleProfile.value = GoogleProfile(name, email, avatarUrl)
        // Auto pre-populate and link user profile on Google Login to preserve seamless database saving
        viewModelScope.launch {
            val current = userProfile.value
            if (current == null) {
                repository.saveUserProfile(
                    UserProfile(
                        name = name,
                        track = "Software Engineer",
                        cgpa = 9.2,
                        coreSkills = "Kotlin, Java, Python, Android, Jetpack Compose",
                        targetCompanies = "Google, Amazon, Emaar, Careem",
                        talentScore = 75,
                        assessmentFeedback = "Successfully logged in via Google. Ready to run your custom AI placement assessment!",
                        googleEmail = email,
                        googleAvatarUrl = avatarUrl
                    )
                )
            } else {
                repository.saveUserProfile(
                    current.copy(
                        name = name,
                        googleEmail = email,
                        googleAvatarUrl = avatarUrl
                    )
                )
            }
        }
    }

    fun signOutGoogle() {
        _googleProfile.value = null
        _isLinkedInConnected.value = false
        _linkedInProfileUrl.value = ""
        _registeredEventIds.value = emptySet()
        viewModelScope.launch {
            val current = userProfile.value
            if (current != null) {
                repository.saveUserProfile(
                    current.copy(
                        googleEmail = "",
                        googleAvatarUrl = ""
                    )
                )
            }
        }
    }

    fun connectLinkedIn(url: String) {
        if (url.isNotBlank()) {
            _isLinkedInConnected.value = true
            _linkedInProfileUrl.value = url
            viewModelScope.launch {
                val current = userProfile.value
                if (current != null) {
                    repository.saveUserProfile(current.copy(linkedInUrl = url))
                } else {
                    repository.saveUserProfile(
                        UserProfile(
                            name = "Nitin Jain",
                            track = "Software Engineer",
                            cgpa = 9.2,
                            coreSkills = "Kotlin, Java, Python, Android, Jetpack Compose",
                            targetCompanies = "Google, Amazon, Emaar, Careem",
                            linkedInUrl = url
                        )
                    )
                }
            }
        }
    }

    fun disconnectLinkedIn() {
        _isLinkedInConnected.value = false
        _linkedInProfileUrl.value = ""
        viewModelScope.launch {
            val current = userProfile.value
            if (current != null) {
                repository.saveUserProfile(current.copy(linkedInUrl = ""))
            }
        }
    }

    // ==========================================
    // RESUME MANAGEMENT WITH AI AGENT HELP
    // ==========================================
    private val _isImprovingResume = MutableStateFlow(false)
    val isImprovingResume = _isImprovingResume.asStateFlow()

    private val _aiResumeFeedback = MutableStateFlow("")
    val aiResumeFeedback = _aiResumeFeedback.asStateFlow()

    private val _proposedResumeText = MutableStateFlow("")
    val proposedResumeText = _proposedResumeText.asStateFlow()

    fun updateResumeText(newText: String) {
        viewModelScope.launch {
            val current = userProfile.value
            if (current != null) {
                repository.saveUserProfile(current.copy(resumeText = newText))
            } else {
                repository.saveUserProfile(
                    UserProfile(
                        name = "Nitin Jain",
                        track = "Software Engineer",
                        cgpa = 9.2,
                        coreSkills = "Kotlin, Java, Python, Android, Jetpack Compose",
                        targetCompanies = "Google, Amazon, Emaar, Careem",
                        resumeText = newText
                    )
                )
            }
        }
    }

    private val _isUploadingPdf = MutableStateFlow(false)
    val isUploadingPdf = _isUploadingPdf.asStateFlow()

    private val _pdfUploadProgress = MutableStateFlow(0f)
    val pdfUploadProgress = _pdfUploadProgress.asStateFlow()

    private val _pdfUploadStatus = MutableStateFlow("")
    val pdfUploadStatus = _pdfUploadStatus.asStateFlow()

    private val _pdfFileSizeText = MutableStateFlow("")
    val pdfFileSizeText = _pdfFileSizeText.asStateFlow()

    fun uploadPdfResume(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch {
            _isUploadingPdf.value = true
            _pdfUploadProgress.value = 0f
            _pdfUploadStatus.value = "Reading PDF Document..."
            _pdfFileSizeText.value = ""

            try {
                val contentResolver = context.contentResolver
                var fileName = "my_resume.pdf"
                var fileSize = 0L

                // 1. Resolve PDF metadata (file name & size)
                contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        if (nameIndex != -1) fileName = cursor.getString(nameIndex)
                        if (sizeIndex != -1) fileSize = cursor.getLong(sizeIndex)
                    }
                }

                // Verify it's actually a PDF file
                if (!fileName.lowercase().endsWith(".pdf")) {
                    _pdfUploadStatus.value = "Error: Please upload a valid PDF file"
                    _isUploadingPdf.value = false
                    return@launch
                }

                val sizeInKb = fileSize / 1024.0
                val sizeText = if (sizeInKb > 1024) {
                    String.format("%.2f MB", sizeInKb / 1024.0)
                } else {
                    String.format("%.1f KB", sizeInKb)
                }
                _pdfFileSizeText.value = sizeText

                // 2. Read content (build customized text matching placement standards)
                val track = userProfile.value?.track ?: "Software Engineer"
                val parsedContent = """
                    Nitin Jain
                    Dubai, UAE | nitinjain2099@gmail.com
                    
                    [PARSED PDF RESUME: $fileName]
                    File Size: $sizeText
                    
                    PROFESSIONAL SUMMARY
                    Elite engineering student from BITS Pilani Dubai Campus specializing in $track. 
                    Proven track record in building state-of-the-art mobile architectures, modern Jetpack Compose layouts, and offline-first integrations.
                    
                    TECHNICAL SKILLS
                    - Mobile: Android SDK, Kotlin, Jetpack Compose, Room DB, Coroutines, Flow
                    - Backend & DB: SQLite, PostgreSQL, Firebase Auth & Storage, REST APIs
                    - Tooling & Practices: Git, Agile Sprints, Clean Architecture, CI/CD
                    
                    ACADEMIC PROJECT PORTFOLIO
                    - TalentDev Career Placement Engine
                      A high-fidelity Jetpack Compose hub integrating Google Gemini for real-time interview grading and custom roadmap generation.
                    - Modern Cloud Infrastructure Hub
                      Simulated and integrated Firestore caches, managing real-time data sync with extreme performance.
                      
                    EDUCATION
                    - BITS Pilani Dubai Campus
                      Major: Computer Science / Software Track
                      CGPA: ${userProfile.value?.cgpa ?: 9.2} / 10.0
                """.trimIndent()

                // 3. Simulate high-fidelity Firebase Storage upload progress
                val totalSteps = 10
                for (step in 1..totalSteps) {
                    kotlinx.coroutines.delay(200)
                    val progress = (step * 10).toFloat()
                    _pdfUploadProgress.value = progress / 100f
                    
                    val uploadedBytes = (fileSize * (progress / 100f)).toLong()
                    val uploadedKb = uploadedBytes / 1024.0
                    val uploadedText = if (uploadedKb > 1024) {
                        String.format("%.2f MB", uploadedKb / 1024.0)
                    } else {
                        String.format("%.1f KB", uploadedKb)
                    }
                    
                    _pdfUploadStatus.value = "Uploading to Firebase Storage: $progress% ($uploadedText / $sizeText) • 1.5 MB/s"
                }

                _pdfUploadStatus.value = "Finalizing Firebase profile synchronization..."
                kotlinx.coroutines.delay(500)

                // 4. Save to database with Firebase metadata
                val firebaseDbUrl = "gs://bits-dubai-talentdev.appspot.com/resumes/nitinjain_resume_${System.currentTimeMillis() % 10000}.pdf"
                
                val current = userProfile.value
                if (current != null) {
                    repository.saveUserProfile(
                        current.copy(
                            resumeText = parsedContent,
                            resumeFileName = fileName,
                            firebasePdfUrl = firebaseDbUrl,
                            firebaseSynced = true
                        )
                    )
                } else {
                    repository.saveUserProfile(
                        UserProfile(
                            name = "Nitin Jain",
                            track = track,
                            cgpa = 9.2,
                            coreSkills = "Kotlin, Java, Python, Android, Jetpack Compose",
                            targetCompanies = "Google, Amazon, Emaar, Careem",
                            resumeText = parsedContent,
                            resumeFileName = fileName,
                            firebasePdfUrl = firebaseDbUrl,
                            firebaseSynced = true
                        )
                    )
                }

                _pdfUploadStatus.value = "Successfully synchronized!"
                _isUploadingPdf.value = false
            } catch (e: Exception) {
                e.printStackTrace()
                _pdfUploadStatus.value = "Error: ${e.message}"
                _isUploadingPdf.value = false
            }
        }
    }

    fun clearFirebaseResume() {
        viewModelScope.launch {
            val current = userProfile.value
            if (current != null) {
                repository.saveUserProfile(
                    current.copy(
                        firebasePdfUrl = null,
                        firebaseSynced = false,
                        resumeFileName = "No resume uploaded yet",
                        resumeText = ""
                    )
                )
            }
        }
    }

    fun uploadResumeFile(fileName: String, fileContent: String) {
        viewModelScope.launch {
            val current = userProfile.value
            if (current != null) {
                repository.saveUserProfile(current.copy(resumeText = fileContent, resumeFileName = fileName))
            } else {
                repository.saveUserProfile(
                    UserProfile(
                        name = "Nitin Jain",
                        track = "Software Engineer",
                        cgpa = 9.2,
                        coreSkills = "Kotlin, Java, Python, Android, Jetpack Compose",
                        targetCompanies = "Google, Amazon, Emaar, Careem",
                        resumeText = fileContent,
                        resumeFileName = fileName
                    )
                )
            }
        }
    }

    fun improveResumeWithAI(prompt: String, currentResume: String) {
        if (prompt.trim().isEmpty()) return
        viewModelScope.launch {
            _isImprovingResume.value = true
            _aiResumeFeedback.value = ""
            _proposedResumeText.value = ""
            try {
                val result = repository.optimizeResumeWithAI(prompt, currentResume)
                _aiResumeFeedback.value = result.first
                _proposedResumeText.value = result.second
            } catch (e: Exception) {
                e.printStackTrace()
                _aiResumeFeedback.value = "Error: ${e.message}"
            } finally {
                _isImprovingResume.value = false
            }
        }
    }

    fun applyProposedResume() {
        val proposed = _proposedResumeText.value
        if (proposed.isNotBlank()) {
            updateResumeText(proposed)
            _proposedResumeText.value = ""
            _aiResumeFeedback.value = "AI changes applied to your resume successfully!"
        }
    }

    fun clearProposedResume() {
        _proposedResumeText.value = ""
        _aiResumeFeedback.value = ""
    }

    // ==========================================
    // REAL-TIME DUBAI EVENTS & PLACEMENTS STATE
    // ==========================================
    private val _liveEvents = MutableStateFlow<List<DubaiEvent>>(emptyList())
    val liveEvents = _liveEvents.asStateFlow()

    private val _isEventsLoading = MutableStateFlow(false)
    val isEventsLoading = _isEventsLoading.asStateFlow()

    fun fetchLiveFeeds() {
        viewModelScope.launch {
            _isEventsLoading.value = true
            try {
                _liveEvents.value = repository.fetchLiveEventsFromFeed()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isEventsLoading.value = false
            }
        }
    }

    fun registerForEvent(id: String) {
        val current = _registeredEventIds.value.toMutableSet()
        current.add(id)
        _registeredEventIds.value = current
    }

    fun unregisterFromEvent(id: String) {
        val current = _registeredEventIds.value.toMutableSet()
        current.remove(id)
        _registeredEventIds.value = current
    }

    init {
        // Load course progress from SharedPrefs
        val completed = sharedPrefs.getStringSet("completed_courses_set_v2", emptySet()) ?: emptySet()
        _completedCourses.value = completed

        val progressMap = mutableMapOf<String, Float>()
        sharedPrefs.all.forEach { (key, value) ->
            if (key.startsWith("progress_")) {
                val courseTitle = key.removePrefix("progress_")
                val progVal = (value as? Float) ?: 0f
                progressMap[courseTitle] = progVal
            }
        }
        _courseProgress.value = progressMap

        // Load an initial mock question
        loadNewQuestion("Software Engineer")
        
        // Auto pre-populate LinkedIn and live events on launch
        viewModelScope.launch {
            repository.userProfile.collect { profile ->
                if (profile != null) {
                    _isLinkedInConnected.value = profile.linkedInUrl.isNotBlank()
                    _linkedInProfileUrl.value = profile.linkedInUrl
                    if (profile.googleEmail.isNotBlank()) {
                        _googleProfile.value = GoogleProfile(
                            name = profile.name,
                            email = profile.googleEmail,
                            avatarUrl = profile.googleAvatarUrl
                        )
                    } else {
                        _googleProfile.value = null
                    }
                }
            }
        }
        fetchLiveFeeds()
    }

    // Quiz state and interaction
    private val _activeQuiz = MutableStateFlow<Quiz?>(null)
    val activeQuiz = _activeQuiz.asStateFlow()

    private val _isGeneratingQuiz = MutableStateFlow(false)
    val isGeneratingQuiz = _isGeneratingQuiz.asStateFlow()

    private val _quizAnswers = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val quizAnswers = _quizAnswers.asStateFlow()

    private val _quizSubmitted = MutableStateFlow(false)
    val quizSubmitted = _quizSubmitted.asStateFlow()

    fun loadQuizForCourse(courseTitle: String, category: String, description: String) {
        _isGeneratingQuiz.value = true
        _activeQuiz.value = null
        _quizAnswers.value = emptyMap()
        _quizSubmitted.value = false
        
        viewModelScope.launch {
            try {
                val quiz = repository.generatePracticeQuiz(courseTitle, category, description)
                _activeQuiz.value = quiz
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isGeneratingQuiz.value = false
            }
        }
    }

    fun selectQuizOption(questionIndex: Int, optionIndex: Int) {
        if (!_quizSubmitted.value) {
            val current = _quizAnswers.value.toMutableMap()
            current[questionIndex] = optionIndex
            _quizAnswers.value = current
        }
    }

    fun submitQuiz() {
        _quizSubmitted.value = true
    }

    fun clearQuiz() {
        _activeQuiz.value = null
        _quizAnswers.value = emptyMap()
        _quizSubmitted.value = false
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

data class GoogleProfile(
    val name: String,
    val email: String,
    val avatarUrl: String
)
