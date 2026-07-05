package com.example.data

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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

    private fun getOfflineMentorReply(userMessage: String): String {
        val lower = userMessage.lowercase()
        return when {
            lower.contains("careem") -> """
                **Careem Placement Advice (Dubai Tech Giant):**
                Careem is a premier employer in Dubai (owned by Uber). For their software engineering roles, focus heavily on:
                1. **System Design:** Design a ride-hailing app, database partitioning, and real-time geospatial caching (H3/S2 spatial indices).
                2. **DSA:** Graphs (Dijkstra's/A* for route optimization), concurrency control, and high-performance HashMaps.
                3. **Behavioral:** They value "Captain culture" (extreme customer obsession) and fast-paced delivery.
                
                *Tip:* Expect a HackerRank test, followed by 2 technical panel rounds and 1 system design round.
            """.trimIndent()
            
            lower.contains("emirates") -> """
                **Emirates Group IT Placement Advice:**
                Emirates has a massive global IT division based in Dubai. Their recruitment pipeline:
                1. **Online Assessment:** Technical multiple-choice questions plus Java/Python/C# coding.
                2. **Technical Interview:** Enterprise microservices, SQL query tuning, transaction management, and cloud infrastructure (AWS/Azure).
                3. **Behavioral Round:** Emphasize high-reliability coding, safety, scalability, and teamwork.
                
                *Tip:* Mentioning experience with secure distributed APIs will instantly impress their recruiters.
            """.trimIndent()
            
            lower.contains("talabat") -> """
                **Talabat Placements (Quick-Commerce & Food Tech):**
                Talabat (Delivery Hero) operates a major tech hub in Dubai. In interviews:
                1. **Concurrency & Real-time:** Explain message-driven systems (Kafka/RabbitMQ) and real-time location tracking.
                2. **Stack Expertise:** Be fluent in Go, Kotlin, Node.js, or React/Next.js.
                3. **Optimization:** Standard caching with Redis to reduce heavy database read loads.
                
                *Tip:* Mentioning database transaction isolation or horizontal scaling is highly valued here.
            """.trimIndent()

            lower.contains("property finder") || lower.contains("propertyfinder") -> """
                **Property Finder (Dubai PropTech Leader):**
                Property Finder is Dubai's top real estate platform. Focus on:
                1. **Search & Filtration:** Know how indexing works (B-trees, Elasticsearch, Lucene) for rapid multi-dimensional filtering.
                2. **Frontend & Mobile:** High proficiency in React, React-Native, or Jetpack Compose.
                3. **Caching:** Memcached/Redis for static listing details.
                
                *Tip:* Highlight projects handling multi-faceted user search/filters.
            """.trimIndent()

            lower.contains("e&") || lower.contains("etisalat") -> """
                **e& (Etisalat Group Telecom Tech):**
                e& is a global telecom titan headquartered in Abu Dhabi/Dubai. In interviews:
                1. **APIs & Communication:** Understand REST vs GraphQL vs gRPC for high-performance telecom microservices.
                2. **Networks & Security:** Deep networking fundamentals (TCP/IP, SSL/TLS handshakes, VPNs).
                3. **High Availability:** Active-active multi-region replication and load balancer configurations.
                
                *Tip:* Certifications in AWS, Azure, or Cisco CCNA are highly regarded.
            """.trimIndent()

            lower.contains("noon") -> """
                **noon.com (E-commerce Placement Tips):**
                Noon is the GCC's leading homegrown e-commerce player. Focus areas:
                1. **E-commerce Architecture:** Cart persistence, inventory locking mechanisms, and secure checkout flows.
                2. **DSA:** Sorting, searching, and tree structures (Tries for auto-complete product searches).
                3. **Distributed Caching:** Managing millions of active SKUs using CDNs and distributed caches.
            """.trimIndent()

            lower.contains("binance") -> """
                **Binance Dubai (Web3 & FinTech Placements):**
                Binance holds a virtual asset license in Dubai. Focus on:
                1. **High-Frequency Trading Engine:** Low-latency networking, memory-efficient data structures, and multi-threaded synchronization.
                2. **Blockchain & Security:** Cryptography (symmetric/asymmetric, hashing), smart contract vulnerabilities, and secure custody.
                3. **Compliance:** Know KYC/AML integration mechanisms.
            """.trimIndent()

            lower.contains("resume") || lower.contains("cv") || lower.contains("portfolio") -> """
                **Expert Resume Advice for Dubai Placements:**
                1. **Keep it 1-Page:** Recruiter screening takes less than 6 seconds.
                2. **Quantify Achievements:** Use the X-Y-Z formula (e.g., 'Optimized query latency by 35% by implementing Redis caching, serving 10k users').
                3. **Modern Tech Keywords:** Include Docker, GitHub, AWS, Room DB, or Jetpack Compose in a dedicated Skills section.
                4. **Clean Links:** Ensure clickable, active links to your GitHub and professional LinkedIn profile.
            """.trimIndent()

            lower.contains("salary") || lower.contains("package") || lower.contains("aed") || lower.contains("income") -> """
                **Dubai Computer Science Salary Insights (Tax-Free):**
                Dubai offers excellent TAX-FREE salaries for CS graduates:
                - **Startups & SMEs:** AED 8,000 to AED 14,000 / month.
                - **Tier-1 MNCs (Careem, Talabat, noon):** AED 15,000 to AED 22,000 / month.
                - **Top-Tier FinTech / Hedge Funds:** AED 25,000+ / month.
                
                *Benefits:* Employers usually cover residency visas, comprehensive health insurance, and annual flight tickets home.
            """.trimIndent()

            lower.contains("cybersecurity") || lower.contains("security") || lower.contains("cyber") || lower.contains("defense") -> """
                **Dubai Cybersecurity Placement Prep:**
                With Dubai's strict Cyber Security regulations, companies hire heavily here:
                1. **OWASP Top 10:** Understand XSS, SQL Injection, CSRF, and broken access controls.
                2. **Network Security:** VPNs, firewalls, Wireshark packet capture, and intrusion detection (SIEM).
                3. **Identity Access:** OAuth 2.0, OpenID Connect, and secure multi-factor authentication.
                
                *Bonus:* Certifications like CompTIA Security+, CEH, or ISC2 CC are highly beneficial.
            """.trimIndent()

            lower.contains("dsa") || lower.contains("algorithms") || lower.contains("data structure") || lower.contains("leetcode") -> """
                **Data Structures & Algorithms Preparation:**
                DSA is critical for Careem, noon, and Amazon Dubai interviews:
                1. **Key structures:** Trees, HashMaps, Graphs (Dijkstra's/Kruskal's), and Trie for fast searches.
                2. **Complexity:** Be confident calculating Big-O time and space complexity.
                3. **Dynamic Programming:** Know standard subproblem patterns (LCS, knapsack).
                
                *Action Plan:* Solve LeetCode Top Interview 150 (Focus on Medium-level questions).
            """.trimIndent()

            lower.contains("software engineer") || lower.contains("developer") || lower.contains("backend") || lower.contains("frontend") -> """
                **Software Engineering Placement Guide:**
                To secure standard developer roles in Dubai:
                1. **SOLID Principles:** Be ready to write modular, testable, object-oriented code.
                2. **Databases:** Know database design, SQL query indexing, and normal forms.
                3. **Testing:** Write clean unit tests and understand mock objects.
                4. **Version Control:** Demonstrate professional git etiquette (branching, pull requests, rebase).
            """.trimIndent()

            lower.contains("cloud") || lower.contains("devops") || lower.contains("docker") || lower.contains("kubernetes") || lower.contains("aws") -> """
                **Cloud & DevOps Placement Prep:**
                1. **Docker:** Create multi-stage, secure containerized environments.
                2. **CI/CD:** Automate pipelines using GitHub Actions or GitLab CI.
                3. **Terraform:** Understand Infrastructure as Code.
                4. **AWS / Azure:** Know VPC networking, load balancing, and IAM policies.
            """.trimIndent()

            lower.contains("hello") || lower.contains("hi ") || lower.contains("hey") || lower.contains("greetings") -> """
                Hello! I am your AI Career Mentor. I am fully available offline and completely free of any API dependency.
                
                How can I help you prepare for your Dubai Computer Science placement interviews? Ask me about:
                - **Specific Companies** (Careem, Emirates Group, Talabat, Property Finder, noon)
                - **Salary packages & tax-free benefits**
                - **Resume & CV design rules**
                - **DSA or Cybersecurity preparation tips**
            """.trimIndent()

            else -> """
                **Dubai Placement Career Guidance:**
                Dubai is a rapid-growth global technology gateway. For CS students:
                1. **Local Connections:** Leverage LinkedIn to connect with engineering managers in Dubai Marina, DIFC, and Internet City.
                2. **Technical Mastery:** Excel in core CS disciplines (Data Structures, Clean Coding, and Cloud hosting).
                3. **Soft Skills:** Communication is highly valued in multinational teams representing over 50+ nationalities.
                
                Feel free to ask more specific questions about Dubai firms, salaries, interview streams, or CV tips!
            """.trimIndent()
        }
    }

    private fun evaluateOfflineInterviewAnswer(track: String, question: String, answer: String): Pair<String, Int> {
        val lowerAnswer = answer.lowercase().trim()
        
        if (lowerAnswer.isEmpty()) {
            return Pair("You did not provide an answer. Please write your explanation to receive constructive feedback.", 1)
        }
        
        if (lowerAnswer.length < 15) {
            return Pair("Your answer is too short (less than 15 characters). In a real Dubai multinational placement interview, you must explain your thought process thoroughly, detailing structural choices and trade-offs. Elaborate further to demonstrate your depth of knowledge.", 3)
        }
        
        var score = 6
        val feedbackList = mutableListOf<String>()
        
        if (lowerAnswer.length > 200) {
            score += 2
            feedbackList.add("Great depth of explanation! You provided a highly descriptive response that shows structural understanding.")
        } else if (lowerAnswer.length > 80) {
            score += 1
            feedbackList.add("Good, well-structured length. You covered the foundational points of the question.")
        } else {
            feedbackList.add("Your answer is somewhat brief. Try expanding with concrete examples or mentioning specific implementation structures in a real assessment.")
        }
        
        val technicalKeywords = listOf("complexity", "time", "space", "memory", "performance", "cache", "database", "scale", "latency", "index", "solid", "thread", "concurrency", "security", "encryption")
        val foundKeywords = technicalKeywords.filter { lowerAnswer.contains(it) }
        if (foundKeywords.isNotEmpty()) {
            score += 1
            feedbackList.add("Excellent use of core technical vocabulary: ${foundKeywords.take(3).joinToString(", ")}.")
        } else {
            feedbackList.add("Tip: Try to weave in analytical terms like time complexity, scalability, or memory management to sound more professional.")
        }
        
        val hasHashMapKeywords = lowerAnswer.contains("collision") || lowerAnswer.contains("hash") || lowerAnswer.contains("bucket") || lowerAnswer.contains("equals") || lowerAnswer.contains("linked") || lowerAnswer.contains("tree")
        val hasDatabaseKeywords = lowerAnswer.contains("relational") || lowerAnswer.contains("schema") || lowerAnswer.contains("scale") || lowerAnswer.contains("join") || lowerAnswer.contains("nosql") || lowerAnswer.contains("sql") || lowerAnswer.contains("mongo") || lowerAnswer.contains("postgres")
        val hasSystemKeywords = lowerAnswer.contains("rate limit") || lowerAnswer.contains("token") || lowerAnswer.contains("prevent") || lowerAnswer.contains("dos") || lowerAnswer.contains("security") || lowerAnswer.contains("payload") || lowerAnswer.contains("jwt") || lowerAnswer.contains("auth")
        val hasGraphKeywords = lowerAnswer.contains("graph") || lowerAnswer.contains("dijkstra") || lowerAnswer.contains("vertex") || lowerAnswer.contains("edge") || lowerAnswer.contains("node") || lowerAnswer.contains("path") || lowerAnswer.contains("shortest")
        val hasCloudKeywords = lowerAnswer.contains("container") || lowerAnswer.contains("docker") || lowerAnswer.contains("kubernetes") || lowerAnswer.contains("deployment") || lowerAnswer.contains("pipeline") || lowerAnswer.contains("aws") || lowerAnswer.contains("cicd")
        val hasSecurityKeywords = lowerAnswer.contains("tls") || lowerAnswer.contains("ssl") || lowerAnswer.contains("handshake") || lowerAnswer.contains("encryption") || lowerAnswer.contains("key") || lowerAnswer.contains("cert") || lowerAnswer.contains("mitm")
        val hasAiKeywords = lowerAnswer.contains("precision") || lowerAnswer.contains("recall") || lowerAnswer.contains("classification") || lowerAnswer.contains("feature") || lowerAnswer.contains("overfitting") || lowerAnswer.contains("regularization") || lowerAnswer.contains("converge")
        
        if (hasHashMapKeywords || hasDatabaseKeywords || hasSystemKeywords || hasGraphKeywords || hasCloudKeywords || hasSecurityKeywords || hasAiKeywords) {
            score += 1
            feedbackList.add("You successfully addressed the core concept of the technical prompt with relevant domain terms.")
        } else {
            feedbackList.add("Be sure to explicitly discuss the direct algorithmic components or technical mechanisms mentioned in the question.")
        }
        
        val finalScore = score.coerceIn(1, 10)
        
        val summaryFeedback = when {
            finalScore >= 8 -> "This is an outstanding response. Top-tier technology organizations value candidates who express tech choices with this level of maturity. Perfect terminology usage and deep logical explanation."
            finalScore >= 6 -> "A solid response that meets the basic standards of an entry-level interview. To stand out for competitive tech placements, try adding specific real-world examples, database schema outlines, or computational complexity calculations."
            else -> "This answer needs more technical refinement. Technical interviewers expect candidates to detail step-by-step algorithms, database schemas, and precise design choices. Review this topic in the Study tab to build a stronger foundation."
        }
        
        val fullFeedback = "${feedbackList.joinToString(" ")}\n\n**Recruiter Verdict:** $summaryFeedback"
        return Pair(fullFeedback, finalScore)
    }

    suspend fun sendMessageToMentor(userMessage: String): String = withContext(Dispatchers.IO) {
        val userMsgEntity = MentorMessage(sender = "user", message = userMessage)
        db.mentorDao().insertMessage(userMsgEntity)

        // ALWAYS run completely free / offline as requested by the user, avoiding paywall/Google API
        delay(600) // Simulated processing latency for rich feel
        val textResponse = getOfflineMentorReply(userMessage)
        db.mentorDao().insertMessage(MentorMessage(sender = "mentor", message = textResponse))
        textResponse
    }

    suspend fun evaluateMockInterview(
        track: String,
        question: String,
        answer: String
    ): Pair<String, Int> = withContext(Dispatchers.IO) {
        // ALWAYS run completely free / offline as requested by the user, avoiding paywall/Google API
        delay(800) // Simulated grading latency
        val evaluation = evaluateOfflineInterviewAnswer(track, question, answer)
        db.interviewDao().insertInterviewLog(
            InterviewLog(track = track, question = question, answer = answer, feedback = evaluation.first, score = evaluation.second)
        )
        evaluation
    }

    suspend fun optimizeResumeWithAI(prompt: String, currentResume: String): Pair<String, String> = withContext(Dispatchers.IO) {
        if (apiKey.isEmpty() || apiKey == "YOUR_GEMINI_API_KEY_HERE") {
            val mockFeedback = "Offline mode. Here is a simulated suggestion: make sure you emphasize your Kotlin and Jetpack Compose skills."
            val mockResume = currentResume + "\n\n[AI SUGGESTION]\n- Advanced proficiency in Kotlin, Android Architecture Components, and Jetpack Compose."
            return@withContext Pair(mockFeedback, mockResume)
        }

        val requestPrompt = """
            You are an elite Tech Resume Coach and Recruiter specializing in Dubai's competitive tech scene (Dubai Internet City, Silicon Oasis).
            A student wants to improve their resume based on the following instruction:
            "$prompt"

            Here is their current resume:
            ```
            $currentResume
            ```

            Please do two things:
            1. Formulate a highly actionable critique and improvement advice (max 100 words).
            2. Generate the completely updated, optimized, professional-grade resume text incorporating the user's instruction and industry best practices.

            You MUST output the response in a structured JSON format with EXACTLY these keys:
            {
              "feedback": "<your critique/advice>",
              "optimizedResume": "<the completely revised, ready-to-paste resume text>"
            }
        """.trimIndent()

        try {
            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = requestPrompt)))),
                generationConfig = GenerationConfig(responseMimeType = "application/json")
            )
            val response = apiService.generateContent(apiKey, request)
            val textResponse = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            
            if (textResponse.isNotEmpty()) {
                val json = Json { ignoreUnknownKeys = true }
                val jsonObj = json.parseToJsonElement(textResponse).jsonObject
                val feedback = jsonObj["feedback"]?.jsonPrimitive?.content ?: "Suggestions generated successfully."
                val optimized = jsonObj["optimizedResume"]?.jsonPrimitive?.content ?: currentResume
                Pair(feedback, optimized)
            } else {
                Pair("Could not optimize resume.", currentResume)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Pair("Error optimizing resume: ${e.message}", currentResume)
        }
    }

    suspend fun generateTalentBadgeImage(prompt: String, size: String): String? = withContext(Dispatchers.IO) {
        if (apiKey.isEmpty() || apiKey == "YOUR_GEMINI_API_KEY_HERE") {
            return@withContext null
        }
        try {
            val request = ImageGenerateRequest(
                contents = listOf(ImageContent(parts = listOf(ImagePart(text = prompt)))),
                generationConfig = ImageGenerationConfig(
                    imageConfig = ImageSizeConfig(aspectRatio = "1:1", imageSize = size)
                )
            )
            val response = apiService.generateImage("gemini-3-pro-image-preview", apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull { it.inlineData != null }?.inlineData?.data
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun fetchLiveEventsFromFeed(): List<DubaiEvent> = withContext(Dispatchers.IO) {
        if (apiKey.isEmpty() || apiKey == "YOUR_GEMINI_API_KEY_HERE") {
            return@withContext getFallbackDubaiEvents()
        }

        val prompt = """
            Retrieve the latest 8-10 real-world tech events, conferences, hackathons, or software placement drives currently happening or scheduled in Dubai and the wider UAE for 2026/2027.
            Make sure to include Dubai Government initiatives (such as Dubai Future Foundation, Digital Dubai Authority, Museum of the Future) and UAE-wide tech hub events (like Abu Dhabi Hub71, Sharjah Research Technology and Innovation Park - SRTIP).
            Ensure the details are accurate (no placeholders, no dummy templates).
            For each event, provide:
            - id (e.g. "evt_1", "evt_2")
            - title (real-world event title)
            - description (real-world details)
            - date (actual date & time)
            - location (real location in Dubai/UAE like Dubai World Trade Centre, Hub71 Abu Dhabi, SRTIP Sharjah, BITS Pilani Dubai Campus)
            - category ("AI Event", "Placement Event", or "GovTech & UAE")
            - bannerUrl (a highly relevant Unsplash photo URL)

            You MUST output the response in a structured JSON array format:
            [
              {
                "id": "evt_1",
                "title": "Dubai Generative AI Summit 2026",
                "description": "...",
                "date": "...",
                "location": "...",
                "category": "AI Event",
                "bannerUrl": "..."
              },
              ...
            ]
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
                val eventsJson = json.parseToJsonElement(textResponse).jsonArray
                val parsedEvents = mutableListOf<DubaiEvent>()
                for (e in eventsJson) {
                    val eObj = e.jsonObject
                    parsedEvents.add(
                        DubaiEvent(
                            id = eObj["id"]?.jsonPrimitive?.content ?: "evt_${System.currentTimeMillis()}",
                            title = eObj["title"]?.jsonPrimitive?.content ?: "Tech Event",
                            description = eObj["description"]?.jsonPrimitive?.content ?: "",
                            date = eObj["date"]?.jsonPrimitive?.content ?: "Upcoming 2026",
                            location = eObj["location"]?.jsonPrimitive?.content ?: "Dubai, UAE",
                            category = eObj["category"]?.jsonPrimitive?.content ?: "AI Event",
                            bannerUrl = eObj["bannerUrl"]?.jsonPrimitive?.content ?: "https://images.unsplash.com/photo-1546412414-8035e1776c9a?auto=format&fit=crop&w=600&q=80"
                        )
                    )
                }
                if (parsedEvents.isNotEmpty()) {
                    return@withContext parsedEvents
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext getFallbackDubaiEvents()
    }

    private fun getFallbackDubaiEvents(): List<DubaiEvent> {
        return listOf(
            DubaiEvent(
                id = "evt_live_1",
                title = "Dubai Internet City AI & Cloud Careers 2026",
                description = "Join top hiring partners including Careem, Astra Tech, and Microsoft for live onsite code challenges and placement matching in Dubai Internet City.",
                date = "Sept 18, 2026 • 10:00 AM",
                location = "Dubai Internet City Amphitheatre, Dubai",
                category = "Placement Event",
                bannerUrl = "https://images.unsplash.com/photo-1540575467063-178a50c2df87?auto=format&fit=crop&w=600&q=80"
            ),
            DubaiEvent(
                id = "evt_live_2",
                title = "BITS Dubai Annual Placement & Internship Drive 2026",
                description = "Exclusive campus placement drive hosting over 45 leading Middle East multinationals, advisory firms, and deep tech startups recruiting for full-time engineering roles.",
                date = "Oct 22, 2026 • 09:00 AM",
                location = "BITS Pilani Dubai Campus, Academic City",
                category = "Placement Event",
                bannerUrl = "https://images.unsplash.com/photo-1523240795612-9a054b0db644?auto=format&fit=crop&w=600&q=80"
            ),
            DubaiEvent(
                id = "evt_live_3",
                title = "Dubai Generative AI World Hackathon 2026",
                description = "A massive 48-hour challenge organized by the Dubai Future Foundation to build and pitch production-ready LLM agents. Cash prize of AED 100,000.",
                date = "Nov 14, 2026 • 08:00 AM",
                location = "Museum of the Future, Dubai",
                category = "AI Event",
                bannerUrl = "https://images.unsplash.com/photo-1504384308090-c894fdcc538d?auto=format&fit=crop&w=600&q=80"
            ),
            DubaiEvent(
                id = "evt_live_4",
                title = "Silicon Oasis Deep Learning Symposium 2026",
                description = "Leading Middle East researchers present active work in computer vision and generative models. Networking session and recruitment booths.",
                date = "Dec 05, 2026 • 02:00 PM",
                location = "Dubai Silicon Oasis Seminars, Dubai",
                category = "AI Event",
                bannerUrl = "https://images.unsplash.com/photo-1591453089816-0fbb971b454c?auto=format&fit=crop&w=600&q=80"
            ),
            DubaiEvent(
                id = "evt_live_5",
                title = "Digital Dubai GovTech & Open Data Challenge 2026",
                description = "Organized by the Digital Dubai Authority to build predictive citizen service models using regional public transit, utility, and energy datasets.",
                date = "Sept 29, 2026 • 09:00 AM",
                location = "Digital Dubai HQ, Dubai",
                category = "GovTech & UAE",
                bannerUrl = "https://images.unsplash.com/photo-1517245386807-bb43f82c33c4?auto=format&fit=crop&w=600&q=80"
            ),
            DubaiEvent(
                id = "evt_live_6",
                title = "GITEX Global & Expand North Star 2026",
                description = "The world's largest tech, AI, and startup show. Experience world-class tech exhibits, and participate in direct recruiter matchmaking sessions.",
                date = "Oct 12 - 16, 2026",
                location = "Dubai World Trade Centre, Dubai",
                category = "Placement Event",
                bannerUrl = "https://images.unsplash.com/photo-1475721027785-f74eccf877e2?auto=format&fit=crop&w=600&q=80"
            ),
            DubaiEvent(
                id = "evt_live_7",
                title = "Abu Dhabi Hub71 AI Unicorn Pitch & Placement Day",
                description = "Pitch robust AI systems to Abu Dhabi venture capitalists and interview for software placement slots inside Hub71's active deep-tech portfolio.",
                date = "Oct 29, 2026 • 10:30 AM",
                location = "Hub71, Al Maryah Island, Abu Dhabi",
                category = "AI Event",
                bannerUrl = "https://images.unsplash.com/photo-1515187029135-18ee286d815b?auto=format&fit=crop&w=600&q=80"
            ),
            DubaiEvent(
                id = "evt_live_8",
                title = "Sharjah SRTIP Global Tech Innovation Forum 2026",
                description = "Focus on industrial automation, drone software development, and sustainability code sprints, hosted by Sharjah Research Technology and Innovation Park.",
                date = "Nov 22, 2026 • 09:30 AM",
                location = "SRTIP, University City, Sharjah",
                category = "GovTech & UAE",
                bannerUrl = "https://images.unsplash.com/photo-1531403009284-440f080d1e12?auto=format&fit=crop&w=600&q=80"
            ),
            DubaiEvent(
                id = "evt_live_9",
                title = "Dubai Future Foundation GovTech Accelerator 2026",
                description = "An intense 8-week program partnering high-caliber university engineering students with UAE government ministries to engineer production-ready AI solutions.",
                date = "Dec 12, 2026 • 09:00 AM",
                location = "Area 2071, Emirates Towers, Dubai",
                category = "GovTech & UAE",
                bannerUrl = "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?auto=format&fit=crop&w=600&q=80"
            )
        )
    }

    suspend fun generatePracticeQuiz(
        courseTitle: String,
        category: String,
        description: String
    ): Quiz = withContext(Dispatchers.IO) {
        if (apiKey.isEmpty() || apiKey == "YOUR_GEMINI_API_KEY_HERE") {
            return@withContext getMockQuiz(courseTitle, category)
        }

        val systemPrompt = """
            You are an expert academic AI tutor. Your task is to generate a high-quality 5-question multiple-choice quiz based on the provided course title, category, and syllabus description.
            The quiz must cover actual topics from that domain and contain varying levels of difficulty.
            Each question must have exactly 4 options.
            Provide a helpful, detailed explanation of why the correct answer is right and why other options are wrong.
            
            You MUST return ONLY a JSON object that adheres strictly to this schema:
            {
              "courseTitle": "The course title",
              "questions": [
                {
                  "questionText": "Question text here...",
                  "options": ["Option A", "Option B", "Option C", "Option D"],
                  "correctAnswerIndex": 0, // 0-based index of the correct option
                  "explanation": "Detailed explanation here..."
                }
              ]
            }
        """.trimIndent()

        val userPrompt = """
            Course Title: $courseTitle
            Category: $category
            Description/Syllabus: $description
        """.trimIndent()

        try {
            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = "$systemPrompt\n\nGenerate quiz for:\n$userPrompt")))),
                generationConfig = GenerationConfig(responseMimeType = "application/json")
            )
            val response = apiService.generateContent(apiKey, request)
            val textResponse = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            
            if (textResponse.isNotEmpty()) {
                val json = Json { ignoreUnknownKeys = true }
                return@withContext json.decodeFromString<Quiz>(textResponse)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext getMockQuiz(courseTitle, category)
    }

    private fun getMockQuiz(courseTitle: String, category: String): Quiz {
        val questions = when (category.uppercase()) {
            "DSA" -> listOf(
                QuizQuestion(
                    questionText = "What is the worst-case time complexity of inserting an element into an unbalanced Binary Search Tree (BST)?",
                    options = listOf("O(1)", "O(log n)", "O(n)", "O(n log n)"),
                    correctAnswerIndex = 2,
                    explanation = "In the worst case, an unbalanced BST degenerates into a linear structure (like a linked list), requiring O(n) traversals to find the insertion point."
                ),
                QuizQuestion(
                    questionText = "Which data structure is typically used to implement Breadth-First Search (BFS)?",
                    options = listOf("Stack", "Queue", "Priority Queue", "Linked List"),
                    correctAnswerIndex = 1,
                    explanation = "Breadth-First Search explores nodes level by level, which naturally requires a FIFO (First-In, First-Out) Queue to track discovered but unexplored nodes."
                ),
                QuizQuestion(
                    questionText = "What is the primary advantage of a hash table over a simple array?",
                    options = listOf("It uses less memory overall", "It guarantees ordered element traversal", "It allows O(1) average-case search and insertion", "It is easier to implement"),
                    correctAnswerIndex = 2,
                    explanation = "By using a hashing function to map keys to bucket indices, hash tables offer outstanding average-case O(1) time complexity for search, insertion, and deletion."
                ),
                QuizQuestion(
                    questionText = "Which sorting algorithm employs a classic divide-and-conquer strategy?",
                    options = listOf("Bubble Sort", "Merge Sort", "Kruskal's Algorithm", "Dijkstra's Algorithm"),
                    correctAnswerIndex = 1,
                    explanation = "Merge Sort is a quintessential divide-and-conquer algorithm. It recursively splits the array into single-element lists, sorts them, and merges them back together."
                ),
                QuizQuestion(
                    questionText = "What is the space complexity of an in-place sorting algorithm?",
                    options = listOf("O(1) auxiliary space", "O(log n) auxiliary space", "O(n) auxiliary space", "O(n^2) auxiliary space"),
                    correctAnswerIndex = 0,
                    explanation = "An in-place algorithm manipulates the input data structure directly and uses only a constant O(1) amount of extra auxiliary memory."
                )
            )
            "CYBERSECURITY" -> listOf(
                QuizQuestion(
                    questionText = "What does the 'S' in HTTPS stand for?",
                    options = listOf("Standard", "Secure", "Socket", "Shield"),
                    correctAnswerIndex = 1,
                    explanation = "HTTPS is Hypertext Transfer Protocol Secure. It uses encryption (via SSL/TLS) to establish secure channels and protect data in transit."
                ),
                QuizQuestion(
                    questionText = "Which cryptographic algorithm is a widely accepted symmetric block cipher?",
                    options = listOf("RSA", "ECC", "AES", "Diffie-Hellman"),
                    correctAnswerIndex = 2,
                    explanation = "AES (Advanced Encryption Standard) is a symmetric cipher. It uses the same secret key to encrypt and decrypt block payloads."
                ),
                QuizQuestion(
                    questionText = "What constitutes a SQL injection (SQLi) vulnerability?",
                    options = listOf("Injecting malicious database queries via unvalidated user input", "Overloading database buffers with high connection numbers", "Sniffing active database passwords over unencrypted LAN networks", "Decrypting salted password hashes"),
                    correctAnswerIndex = 0,
                    explanation = "SQLi occurs when user input is treated as dynamic executable code in SQL statements, enabling attackers to run arbitrary database commands."
                ),
                QuizQuestion(
                    questionText = "What is the primary goal of Multi-Factor Authentication (MFA)?",
                    options = listOf("Requiring very long passwords with multiple special characters", "Protecting physical hardware against unauthorized system access", "Adding layered defense-in-depth by requiring multiple validation factor categories", "Encrypting active user credentials locally"),
                    correctAnswerIndex = 2,
                    explanation = "MFA provides defense-in-depth by requiring verification across distinct categories: something you know (password), something you have (token), or something you are (biometrics)."
                ),
                QuizQuestion(
                    questionText = "Which attack attempts to make an active service unavailable by flooding it with high volumes of traffic?",
                    options = listOf("Man-in-the-Middle", "Distributed Denial of Service (DDoS)", "Phishing Campaign", "Buffer Overflow exploit"),
                    correctAnswerIndex = 1,
                    explanation = "DDoS attacks employ distributed networks of compromised machines to inundate a targeted server or resource with high-volume traffic, making it crash or stall."
                )
            )
            "AI & ML" -> listOf(
                QuizQuestion(
                    questionText = "What is the key role of an activation function in deep neural networks?",
                    options = listOf("Initializing network weights", "Introducing non-linearity to learn complex non-linear patterns", "Calculating loss gradients directly", "Accelerating the backpropagation learning rates"),
                    correctAnswerIndex = 1,
                    explanation = "Without non-linear activation functions (like ReLU or GeLU), a neural network would just be a series of linear transformations, restricting its capability to simple linear fits."
                ),
                QuizQuestion(
                    questionText = "What is 'overfitting' in machine learning?",
                    options = listOf("When a model generalizes perfectly to new test data but poorly on training runs", "When a model excels on training data but generalizes poorly to unseen validation data", "When neural network training cycles finish too quickly", "When an active model is completely unable to learn any patterns"),
                    correctAnswerIndex = 1,
                    explanation = "Overfitting occurs when a model models the training data too closely (including its random noise), meaning it fails to generalize to fresh, independent datasets."
                ),
                QuizQuestion(
                    questionText = "Which task represents a classic Supervised Learning problem?",
                    options = listOf("Grouping customer clusters using unlabelled K-Means data", "Predicting house sales prices using linear regression with labeled historical sales data", "Reducing high-dimensional feature spaces via PCA", "Generating synthetic landscapes using GAN models"),
                    correctAnswerIndex = 1,
                    explanation = "Supervised learning utilizes labeled datasets where each input training record is paired with a clear, ground-truth label target (like historical house sale values)."
                ),
                QuizQuestion(
                    questionText = "What is the core function of the 'gradient descent' optimizer?",
                    options = listOf("Initializing network biases dynamically", "Iteratively adjusting weights to minimize the defined loss function", "Normalizing database input vectors", "Evaluating final test accuracy scores"),
                    correctAnswerIndex = 1,
                    explanation = "Gradient descent is an optimization algorithm that iteratively calculates loss gradients and moves weights in the opposite direction to minimize overall error."
                ),
                QuizQuestion(
                    questionText = "Which breakthrough mechanism forms the core of modern Transformer architectures?",
                    options = listOf("Recurrent feedback loops", "Convolutional feature extraction blocks", "Self-Attention mechanisms", "Policy gradient models"),
                    correctAnswerIndex = 2,
                    explanation = "Transformers depend entirely on Self-Attention mechanisms, which enable models to weigh and capture complex relationships between input tokens regardless of their distance."
                )
            )
            "CLOUD" -> listOf(
                QuizQuestion(
                    questionText = "What is the primary benefit of Cloud Elasticity?",
                    options = listOf("Securing master database files", "Dynamically scaling computing resources up or down on-demand to match traffic fluctuations", "Ensuring direct offline capabilities", "Eliminating regional server locations"),
                    correctAnswerIndex = 1,
                    explanation = "Cloud Elasticity enables systems to scale compute, storage, and network capacities automatically to match real-time resource demand, minimizing wasted resources."
                ),
                QuizQuestion(
                    questionText = "What does 'Serverless Computing' represent?",
                    options = listOf("Running lightweight code scripts purely on physical client hardware", "An architecture where the cloud provider manages infrastructure provisioning, scaling, and system maintenance dynamically", "Setting up local container hosts on private networks", "An unmanaged cloud virtual machine instance"),
                    correctAnswerIndex = 1,
                    explanation = "Serverless models (like AWS Lambda or Google Cloud Functions) delegate server management, scaling, and resource allocations entirely to the cloud provider, billing only for actual execution runtime."
                ),
                QuizQuestion(
                    questionText = "What is the primary purpose of a Content Delivery Network (CDN)?",
                    options = listOf("Backing up cloud database instances", "Caching static payloads globally on edge servers closer to users to minimize latency", "Running high-performance microservice APIs", "Encrypting database transactions"),
                    correctAnswerIndex = 1,
                    explanation = "CDNs place and cache copies of static assets (images, CSS, media) on a global network of edge servers, delivering them to users with minimal latency."
                ),
                QuizQuestion(
                    questionText = "What does SaaS stand for in cloud paradigms?",
                    options = listOf("System as a Security-Standard", "Software as a Service", "Servers as an Architecture-System", "Storage as a Synchronized-service"),
                    correctAnswerIndex = 1,
                    explanation = "SaaS delivers full, hosted software applications directly via web browsers or client apps, eliminating local deployment, license, and hosting burdens."
                ),
                QuizQuestion(
                    questionText = "What is 'Infrastructure as Code' (IaC)?",
                    options = listOf("Writing low-level database triggers", "Managing and provisioning server, network, and storage resources through machine-readable configuration files", "Styling web page elements", "Compiling compiled executable payloads"),
                    correctAnswerIndex = 1,
                    explanation = "IaC tools (like Terraform) allow operators to define, version-control, and provision infrastructure using declarative configuration files, ensuring repeatable setups."
                )
            )
            "ROBOTICS" -> listOf(
                QuizQuestion(
                    questionText = "What is ROS in modern robotics development?",
                    options = listOf("Robot Operating System - a flexible middleware framework for writing robot software", "A real-time hardware sensor interface", "A proprietary query language", "A physical joint motor controller"),
                    correctAnswerIndex = 0,
                    explanation = "ROS (Robot Operating System) acts as a flexible middleware framework, offering hardware abstraction, low-level device control, inter-process messaging, and package utilities."
                ),
                QuizQuestion(
                    questionText = "What parameters does a LIDAR sensor measure?",
                    options = listOf("Ambient light colors", "Distance to surrounding objects using pulsed laser reflections and time-of-flight", "The joint rotational speed", "Decibel audio levels"),
                    correctAnswerIndex = 1,
                    explanation = "LIDAR sends out rapid laser pulses and measures the time-of-flight for reflections to return, building high-resolution 2D/3D map point clouds of surrounding geometry."
                ),
                QuizQuestion(
                    questionText = "What does SLAM stand for in autonomous robot navigation?",
                    options = listOf("Systematic Linear Acceleration Model", "Simultaneous Localization and Mapping", "Static Locomotion and Actuator Management", "Sensor Logging and Analysis Module"),
                    correctAnswerIndex = 1,
                    explanation = "SLAM algorithms allow a mobile robot to build an accurate map of an unknown environment while simultaneously estimating its own precise position on that map."
                ),
                QuizQuestion(
                    questionText = "What is the primary role of an actuator in robots?",
                    options = listOf("Converting raw physical sensor observations into digital values", "Converting control signals or electricity into physical kinetic motion", "Calculating algorithmic inverse kinematics paths", "Storing energy reserves"),
                    correctAnswerIndex = 1,
                    explanation = "Actuators serve as a robot's mechanical components (like motors, gears, and pneumatic cylinders), translating electrical signals into dynamic movement."
                ),
                QuizQuestion(
                    questionText = "What is 'inverse kinematics' used for?",
                    options = listOf("Calculating the flow of electric signals in sensor loops", "Calculating joint configurations needed to position a robot's tool or end-effector at a specific 3D position", "Measuring battery voltage curves", "Calibrating inertial sensor drift"),
                    correctAnswerIndex = 1,
                    explanation = "Inverse kinematics computes the specific joint parameters and angles needed to move a robotic arm's hand or end-effector to a targeted 3D position."
                )
            )
            else -> listOf(
                QuizQuestion(
                    questionText = "What does SOLID stand for in clean object-oriented software design?",
                    options = listOf("A set of 5 design principles for writing clear and maintainable code", "An optimization standard for modern database indices", "A compiler pipeline speed specification", "A standard debugging workflow"),
                    correctAnswerIndex = 0,
                    explanation = "SOLID encapsulates Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, and Dependency Inversion design principles."
                ),
                QuizQuestion(
                    questionText = "In Agile methodology, what is the core purpose of a Sprint?",
                    options = listOf("A fast-paced software hackathon event", "A fixed timebox (usually 1-4 weeks) during which specific product increments are designed, built, and tested", "The final release preparation stage", "An optimization query in database servers"),
                    correctAnswerIndex = 1,
                    explanation = "Sprints are iterations of standard duration where Agile teams focus on a committed set of user stories to produce working, deployable increments."
                ),
                QuizQuestion(
                    questionText = "Which Git command can combine multiple commits into a single unified commit?",
                    options = listOf("git checkout", "git merge", "git rebase -i (using squash)", "git branch -m"),
                    correctAnswerIndex = 2,
                    explanation = "Interactive rebase (git rebase -i) with the 'squash' or 'fixup' option compiles several consecutive commits into a single commit, keeping Git histories clean."
                ),
                QuizQuestion(
                    questionText = "What is the key focus of Unit Testing?",
                    options = listOf("Testing system behavior under extreme traffic spikes", "Verifying the correct functional behavior of an individual module or function in complete isolation", "Validating UI screen layout and sizes", "Conducting end-to-end security audits"),
                    correctAnswerIndex = 1,
                    explanation = "Unit tests verify that small, decoupled modules of code (like individual classes or methods) behave correctly under isolation using mock inputs."
                ),
                QuizQuestion(
                    questionText = "What is a Software Design Pattern?",
                    options = listOf("A specific snippet of copy-pasteable code", "A design mockup representing user interface flows", "A generalized, reusable solution to a commonly occurring software architectural or structural problem", "A compiler speed-up setting"),
                    correctAnswerIndex = 2,
                    explanation = "Software design patterns (like Singleton, Factory, or Observer) are conceptual architectural templates that developers use to solve recurring structural problems."
                )
            )
        }

        return Quiz(courseTitle = courseTitle, questions = questions)
    }

    suspend fun getPrepQuestions(company: String, field: String, difficulty: String): List<PrepQuestion> = withContext(Dispatchers.IO) {
        val count = db.prepQuestionDao().getCount()
        if (count < 300) {
            db.prepQuestionDao().clearQuestions()
            val questionsList = generateAllQuestions()
            db.prepQuestionDao().insertQuestions(questionsList)
        }
        db.prepQuestionDao().getQuestions(company, field, difficulty)
    }

    suspend fun getGenericPrepQuestions(field: String, difficulty: String): List<PrepQuestion> = withContext(Dispatchers.IO) {
        val count = db.prepQuestionDao().getCount()
        if (count < 300) {
            db.prepQuestionDao().clearQuestions()
            val questionsList = generateAllQuestions()
            db.prepQuestionDao().insertQuestions(questionsList)
        }
        val rawQuestions = db.prepQuestionDao().getQuestionsByFieldAndDifficulty(field, difficulty)
        rawQuestions.map { q ->
            q.copy(questionText = cleanQuestionText(q.questionText, q.company))
        }
    }

    suspend fun populatePrepQuestions(questions: List<PrepQuestion>) = withContext(Dispatchers.IO) {
        db.prepQuestionDao().insertQuestions(questions)
    }

    suspend fun getPrepQuestionsCount(): Int = withContext(Dispatchers.IO) {
        db.prepQuestionDao().getCount()
    }
}

data class CompanyScenarios(
    val service: String,
    val dsa: String,
    val ai: String,
    val devops: String
)

fun getScenariosForCompany(company: String): CompanyScenarios {
    return when (company) {
        "Emirates Group" -> CompanyScenarios(
            service = "flight reservation and seat booking microservices",
            dsa = "airport connections map where you need to find the shortest flight path with minimum layovers",
            ai = "passenger booking cancellations and dynamically predicting seat upgrade likelihood",
            devops = "global airline ticketing backend requiring 99.999% uptime across multiple AWS regions"
        )
        "Careem" -> CompanyScenarios(
            service = "real-time ride-hailing driver dispatch and GPS tracking systems",
            dsa = "grid map of drivers where you must find the closest active driver to a rider in a 2D space",
            ai = "surge pricing multipliers dynamically adjusting based on historic demand and real-time requests",
            devops = "high-frequency real-time web-socket gateways handling millions of active drivers"
        )
        "Talabat" -> CompanyScenarios(
            service = "food and quick-commerce delivery status tracking",
            dsa = "restaurant menu autocomplete search bar handling millions of concurrent keystroke requests",
            ai = "restaurant recommendation systems using collaborative filtering and purchase histories",
            devops = "order dispatch microservices executing thousands of database writes per second"
        )
        "Property Finder" -> CompanyScenarios(
            service = "property listing faceted search and multi-dimensional filtering",
            dsa = "real-estate listings where you must query the top K cheapest properties in Dubai Marina",
            ai = "predictive model estimating villa market valuation in Dubai based on square footage and historic sales",
            devops = "high-performance image-delivery CDN pipelines loading millions of high-resolution property photos"
        )
        "e& (Etisalat)" -> CompanyScenarios(
            service = "telecom microservices handling millions of active subscribers",
            dsa = "routing telephone network signals represented as a spanning tree with minimum latency",
            ai = "predicting subscriber churn rates and identifying fraudulent calling patterns using machine learning",
            devops = "highly available containerized core communication networks with zero-trust API boundaries"
        )
        "noon.com" -> CompanyScenarios(
            service = "e-commerce cart persistence and high-volume flash sale checkouts",
            dsa = "product list search indexing where we merge and sort millions of active SKUs dynamically",
            ai = "detecting credit card fraud on checkouts using real-time transactions classification models",
            devops = "high-volume GCC e-commerce marketplace undergoing extreme traffic spikes on White Friday"
        )
        "Binance Dubai" -> CompanyScenarios(
            service = "high-frequency digital asset order books and trading engine logs",
            dsa = "low-latency order matching queue handling thousands of order executions per millisecond",
            ai = "predictive market price trends and detecting unusual algorithmic trading loops on exchange books",
            devops = "highly secure, low-latency, audited ledger systems adhering to Dubai VARA regulations"
        )
        "Amazon UAE" -> CompanyScenarios(
            service = "regional warehouse inventory allocation and checkout flows",
            dsa = "delivery route planning problem mapping delivery vans to Dubai addresses using TSP heuristics",
            ai = "personalized homepage recommendation system predicting product interest from browse histories",
            devops = "multi-tenant scalable AWS architecture serving millions of active regional shoppers"
        )
        "G42" -> CompanyScenarios(
            service = "generative AI training jobs and cloud supercomputing node allocation",
            dsa = "nearest-neighbor vector search in a high-dimensional embedding database (Milvus/Qdrant)",
            ai = "Jais Arabic Large Language Model pre-training, parameter tuning, and efficient model inference",
            devops = "supercomputing cluster orchestration running heavy GPU workloads under Kubernetes"
        )
        "Astra Tech" -> CompanyScenarios(
            service = "Botim super-app high-concurrency messaging and VoIP chat servers",
            dsa = "chat contact list search autocomplete using a prefix Trie structure across millions of users",
            ai = "predicting user wallet transaction trends and optimizing advertising click-through-rates",
            devops = "ultra-low latency global VoIP relay servers with persistent WebSocket connection pools"
        )
        "Dubizzle" -> CompanyScenarios(
            service = "user-to-user classifieds listings and live chat messaging database",
            dsa = "duplicate listing detection model matching photos and listing titles in linear time",
            ai = "identifying classifieds spam, fake property listings, and automated bot accounts using classification models",
            devops = "rapid image processing and thumbnail resizing pipeline storing millions of user-uploaded images"
        )
        "Mashreq Bank" -> CompanyScenarios(
            service = "Mashreq Neo core banking transactional ledgers and secure payment gateways",
            dsa = "transaction ledger audit trail verifying integrity via a cryptographic Merkle Tree",
            ai = "credit card risk assessment scoring and predicting loan default rates using logistic regression",
            devops = "PCI-DSS compliant cloud infrastructure hosting secure bank transactions under strict audits"
        )
        "First Abu Dhabi Bank (FAB)" -> CompanyScenarios(
            service = "enterprise payment processing and SWIFT financial communication layers",
            dsa = "interbank routing transactions network modeled as a directed graph to find safe payment paths",
            ai = "detecting high-value money laundering operations using graph neural networks (GNNs)",
            devops = "highly resilient financial services cloud with active-active disaster recovery environments"
        )
        "Majid Al Futtaim (MAF)" -> CompanyScenarios(
            service = "SHARE loyalty rewards points reconciliation and POS transactional backends",
            dsa = "unified retail customer ID lookup map across shopping malls, grocery stores, and cinemas",
            ai = "hyper-personalized promotion matching analyzing customer purchase baskets using association rules",
            devops = "multi-tenant retail platform integrating offline POS machines to real-time cloud analytics"
        )
        "Emaar" -> CompanyScenarios(
            service = "luxury property booking and Burj Khalifa tourist ticketing platforms",
            dsa = "visitor slot booking schedule optimization modeled as an interval-scheduling algorithm",
            ai = "predictive maintenance scheduling for smart-malls and elevator failure models using sensor feeds",
            devops = "enterprise booking platform serving millions of global tourists during peak holiday seasons"
        )
        "Slack" -> CompanyScenarios(
            service = "real-time chat message synchronization and workspace presence servers",
            dsa = "active workspace channel members list where you need to search and auto-complete names in a hierarchical prefix Trie",
            ai = "workspace sentiment search and smart channel summary highlights",
            devops = "high-throughput websocket connection pools handling tens of millions of concurrent connections"
        )
        "Uber" -> CompanyScenarios(
            service = "real-time ride-hailing driver dispatch and route matching engine",
            dsa = "geofence spatial map finding closest active drivers to a rider on a 2D plane",
            ai = "dynamic surge pricing multipliers based on rider demand and driver density",
            devops = "fault-tolerant geo-distributed gateways routing millions of telemetry updates per second"
        )
        "Netflix" -> CompanyScenarios(
            service = "video assets encoding, playback state persistence, and CDN catalog lookup",
            dsa = "distributed network of caching nodes to determine the closest media file chunk path",
            ai = "personalized recommendation ranking based on past viewing history and contextual watch times",
            devops = "globally replicated media storage clusters serving multi-gigabit video streams simultaneously"
        )
        "Spotify" -> CompanyScenarios(
            service = "music playbacks synchronization, catalog search, and offline cache validation",
            dsa = "music recommendation graph database to find similar artists with K degrees of separation",
            ai = "personalized playlist generation matching user audio feature embeddings and acoustic preference profiles",
            devops = "highly scalable audio delivery network with client-side offline storage state reconciliation"
        )
        "Airbnb" -> CompanyScenarios(
            service = "global rental properties search and multi-faceted reservation calendar",
            dsa = "calendar availability grid representing active bookings as non-overlapping intervals",
            ai = "dynamic price optimizer recommending ideal listing price based on local booking trends and seasonality",
            devops = "scalable read-heavy catalog database with automatic read replicas and fast Redis caching layers"
        )
        "Stripe" -> CompanyScenarios(
            service = "PCI-compliant payment processing gateway and transactional accounting ledger",
            dsa = "transaction auditing trail verifying record integrity via a high-performance Merkle Tree",
            ai = "real-time checkout fraud classification using multi-feature risk scoring models",
            devops = "zero-tolerance API gateway with strict rate-limiting, tokenization, and active-active failover clusters"
        )
        "Google" -> CompanyScenarios(
            service = "distributed web search crawl indexers and dynamic result ranking",
            dsa = "high-throughput inverted index document search mapping keyword queries to sorted page listings",
            ai = "semantic understanding of queries using multi-layer transformer sequence modeling",
            devops = "massive containerized orchestration cluster running millions of services globally with high redundancy"
        )
        "Meta" -> CompanyScenarios(
            service = "social graph connection networks and news feed delivery server",
            dsa = "friend recommendation system finding mutual connections using Breadth-First Search (BFS)",
            ai = "real-time personalized feed ranking prioritizing content with custom weight matrices",
            devops = "ultra-low latency distributed memcached layers serving billions of database reads per second"
        )
        "Microsoft" -> CompanyScenarios(
            service = "enterprise active directory authentication and cloud workspace synchronization",
            dsa = "hierarchical role-based access control (RBAC) groups represented as a directed acyclic graph (DAG)",
            ai = "co-pilot code suggestions matching syntax pattern semantics using pre-trained model inference",
            devops = "highly available enterprise clouds hosting millions of virtual machines with strict partition isolation"
        )
        "Apple" -> CompanyScenarios(
            service = "iCloud photo backups syncing and push notifications broadcast service",
            dsa = "adaptive image resolution thumbnail generation using linear space-filling curves",
            ai = "on-device predictive text and speech-to-text translation using resource-optimized neural models",
            devops = "highly secure, end-to-end encrypted backup storage with automated sharding and zero-trust verification"
        )
        else -> CompanyScenarios(
            service = "high-performance scalable backend services",
            dsa = "large-scale data processing system",
            ai = "predictive user trend modeling and performance estimation",
            devops = "continuous deployment and automated container infrastructure"
        )
    }
}

fun generateAllQuestions(): List<PrepQuestion> {
    val companies = listOf(
        "Emirates Group", "Careem", "Talabat", "Property Finder", "e& (Etisalat)",
        "noon.com", "Binance Dubai", "Amazon UAE", "G42", "Astra Tech",
        "Dubizzle", "Mashreq Bank", "First Abu Dhabi Bank (FAB)", "Majid Al Futtaim (MAF)", "Emaar",
        "Slack", "Uber", "Netflix", "Spotify", "Airbnb", "Stripe", "Google", "Meta", "Microsoft", "Apple"
    )
    val fields = listOf(
        "Software Engineering", "Cybersecurity & Networks", "Data Structures & Algorithms",
        "Data Science & AI", "Cloud Computing & DevOps"
    )
    val difficulties = listOf("Easy", "Medium", "Hard")

    val list = mutableListOf<PrepQuestion>()

    for (company in companies) {
        val sc = getScenariosForCompany(company)
        for (field in fields) {
            for (diff in difficulties) {
                val text = when (field) {
                    "Software Engineering" -> when (diff) {
                        "Easy" -> "For $company, explain the difference between interfaces and abstract classes in Kotlin. How does interface delegation help achieve modularity in $company's Android codebase?"
                        "Medium" -> "At $company, we maintain high scalability for our ${sc.service}. Explain how you would implement a Thread Pool or Coroutine Dispatcher to safely handle thousands of concurrent operations without blocking the main thread."
                        "Hard" -> "Design a robust, distributed API rate limiter to protect $company's core ${sc.service}. Compare the advantages of Token Bucket versus Leaky Bucket algorithms, and detail how you would store rate-limit counters in a Redis cache."
                        else -> ""
                    }
                    "Cybersecurity & Networks" -> when (diff) {
                        "Easy" -> "What is the purpose of salting a password before hashing it at $company? Why is simple hashing like MD5 or SHA-256 vulnerable to rainbow-table and dictionary attacks?"
                        "Medium" -> "At $company, securing customer transit data is a top priority. Walk through the complete SSL/TLS cryptographic handshake, detailing how symmetric and asymmetric encryption work together to establish a secure channel."
                        "Hard" -> "Design a Zero-Trust API authentication and authorization architecture for $company's microservices. Detail how JWT access tokens, secure refresh tokens, and OAuth2 scopes prevent token hijacking and replay attacks."
                        else -> ""
                    }
                    "Data Structures & Algorithms" -> when (diff) {
                        "Easy" -> "Explain the worst-case and average-case time and space complexities of sorting user profiles or listing timestamps at $company using QuickSort versus MergeSort."
                        "Medium" -> "Suppose you are designing a system for $company to handle a ${sc.dsa}. Explain how you would model this graph or tree structure, and write an algorithm to find the optimal result efficiently."
                        "Hard" -> "Given a massive, high-frequency stream of concurrent transactions or geo-locations from $company's ${sc.service}, design a custom in-memory data structure (e.g. Quadtree, Segment Tree, or Min-Heap) to query the top K optimal records in O(N log K) time."
                        else -> ""
                    }
                    "Data Science & AI" -> when (diff) {
                        "Easy" -> "What is the key difference between supervised and unsupervised machine learning? Give a concrete example of how $company can use unsupervised learning to cluster users or identify anomalies."
                        "Medium" -> "At $company, we use machine learning to predict and optimize our ${sc.ai}. Explain how you would evaluate your model's quality using Precision, Recall, and F1-score, and decide which metric is more critical for our business."
                        "Hard" -> "Explain the mathematical differences between Gradient Descent, SGD, and Adam optimization. How does the multi-head self-attention mechanism in modern Transformer architectures optimize the training of models for $company's ${sc.ai}?"
                        else -> ""
                    }
                    else -> when (diff) { // Cloud Computing & DevOps
                        "Easy" -> "Explain the structural difference between lightweight Docker containers and traditional virtual machines (VMs). Why are containers preferred for deploying modern systems at $company?"
                        "Medium" -> "At $company, we require continuous uptime. Compare Blue-Green and Rolling Update deployment strategies on Kubernetes, and detail how you would handle rolling back a faulty release of the ${sc.service}."
                        "Hard" -> "Design a highly-available, multi-region containerized cloud architecture on AWS or Azure to host $company's ${sc.service}. Include VPC networking, load balancers, auto-scaling groups, and a fully automated CI/CD pipeline with security scanning."
                        else -> ""
                    }
                }
                list.add(
                    PrepQuestion(
                        company = company,
                        field = field,
                        difficulty = diff,
                        questionText = text
                    )
                )
            }
        }
    }
    return list
}

fun cleanQuestionText(text: String, company: String): String {
    var cleaned = text
    cleaned = cleaned.replace("For $company,", "Explain", ignoreCase = true)
    cleaned = cleaned.replace("For $company", "Explain", ignoreCase = true)
    
    cleaned = cleaned.replace("At $company, we maintain", "In highly scalable systems, we maintain", ignoreCase = true)
    cleaned = cleaned.replace("At $company we maintain", "In highly scalable systems, we maintain", ignoreCase = true)
    cleaned = cleaned.replace("At $company, securing", "Securing", ignoreCase = true)
    cleaned = cleaned.replace("At $company securing", "Securing", ignoreCase = true)
    cleaned = cleaned.replace("At $company, we use", "In data-driven systems, we use", ignoreCase = true)
    cleaned = cleaned.replace("At $company we use", "In data-driven systems, we use", ignoreCase = true)
    cleaned = cleaned.replace("At $company, we require", "In mission-critical platforms, we require", ignoreCase = true)
    cleaned = cleaned.replace("At $company we require", "In mission-critical platforms, we require", ignoreCase = true)
    cleaned = cleaned.replace("At $company,", "In enterprise platforms,", ignoreCase = true)
    cleaned = cleaned.replace("At $company", "In enterprise platforms", ignoreCase = true)
    
    cleaned = cleaned.replace("$company's Android codebase", "a modern Android codebase", ignoreCase = true)
    cleaned = cleaned.replace("$company's core", "the platform's core", ignoreCase = true)
    cleaned = cleaned.replace("$company's microservices", "modern microservices", ignoreCase = true)
    cleaned = cleaned.replace("$company's", "the system's", ignoreCase = true)
    cleaned = cleaned.replace(company, "the system", ignoreCase = true)
    
    cleaned = cleaned.replace("protect the system's", "protect the platform's", ignoreCase = true)
    cleaned = cleaned.replace("protect the system", "protect the platform", ignoreCase = true)
    cleaned = cleaned.replace("at the system?", "in production systems?", ignoreCase = true)
    cleaned = cleaned.replace("at the system", "in production", ignoreCase = true)
    cleaned = cleaned.replace("from the system's", "from the system's", ignoreCase = true)
    cleaned = cleaned.replace("from the system", "from the system", ignoreCase = true)
    cleaned = cleaned.replace("how the system can use", "how an application can use", ignoreCase = true)
    cleaned = cleaned.replace("how the system uses", "how an application uses", ignoreCase = true)
    cleaned = cleaned.replace("systems at the system?", "systems in production?", ignoreCase = true)
    cleaned = cleaned.replace("systems at the system", "systems in production", ignoreCase = true)
    cleaned = cleaned.replace("for the system", "for the application", ignoreCase = true)
    cleaned = cleaned.replace("designing a system for the system to", "designing a system to", ignoreCase = true)
    cleaned = cleaned.replace("designing a system for the system", "designing a system", ignoreCase = true)
    cleaned = cleaned.replace("For the system:", "Explain:", ignoreCase = true)
    
    cleaned = cleaned.replace("Explain, explain", "Explain", ignoreCase = true)
    cleaned = cleaned.replace("Explain, ", "Explain ", ignoreCase = true)
    
    return cleaned.trim()
}

