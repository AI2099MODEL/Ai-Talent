package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.*
import com.example.ui.*
import com.example.viewmodel.TalentViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TalentDevTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    MainNavigationContainer(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MainNavigationContainer(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableStateOf(0) }
    val context = LocalContext.current
    val app = context.applicationContext as Application
    val viewModel: TalentViewModel = viewModel(
        factory = TalentViewModel.Factory(app)
    )

    Column(modifier = modifier.fillMaxSize().background(Slate900)) {
        // App Header Banner
        AppHeaderBanner()

        // Screen Content
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (selectedTab) {
                0 -> DashboardScreen(viewModel, onNavigateToProfile = { selectedTab = 1 })
                1 -> ProfileScreen(viewModel)
                2 -> RoadmapScreen(viewModel)
                3 -> AIPrepScreen(viewModel)
            }
        }

        // Bottom Navigation Bar
        NavigationBar(
            containerColor = Slate800,
            tonalElevation = 8.dp,
            modifier = Modifier.navigationBarsPadding()
        ) {
            NavigationBarItem(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                modifier = Modifier.testTag("nav_dashboard"),
                icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                label = { Text("Dashboard", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = GlowingAmber,
                    selectedTextColor = GlowingAmber,
                    unselectedIconColor = Slate600,
                    unselectedTextColor = Slate600,
                    indicatorColor = Slate700
                )
            )
            NavigationBarItem(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                modifier = Modifier.testTag("nav_profile"),
                icon = { Icon(Icons.Default.AccountCircle, contentDescription = "AI Profile") },
                label = { Text("AI Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = GlowingAmber,
                    selectedTextColor = GlowingAmber,
                    unselectedIconColor = Slate600,
                    unselectedTextColor = Slate600,
                    indicatorColor = Slate700
                )
            )
            NavigationBarItem(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                modifier = Modifier.testTag("nav_roadmap"),
                icon = { Icon(Icons.Default.List, contentDescription = "Roadmap") },
                label = { Text("Roadmap", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = GlowingAmber,
                    selectedTextColor = GlowingAmber,
                    unselectedIconColor = Slate600,
                    unselectedTextColor = Slate600,
                    indicatorColor = Slate700
                )
            )
            NavigationBarItem(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                modifier = Modifier.testTag("nav_prep"),
                icon = { Icon(Icons.Default.Star, contentDescription = "AI Prep") },
                label = { Text("AI Prep", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = GlowingAmber,
                    selectedTextColor = GlowingAmber,
                    unselectedIconColor = Slate600,
                    unselectedTextColor = Slate600,
                    indicatorColor = Slate700
                )
            )
        }
    }
}

@Composable
fun AppHeaderBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Slate800, Slate900)
                )
            )
            .border(width = 1.dp, color = Slate700, shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text(
                    text = "BITS DUBAI • 2025 BATCH",
                    color = GlowingAmber,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "AI Talent Portal",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )
            }
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(CosmicBlue.copy(alpha = 0.2f))
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "BITS Dubai Star",
                    tint = GlowingAmber,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// ==========================================
// 1. DASHBOARD SCREEN
// ==========================================
@Composable
fun DashboardScreen(viewModel: TalentViewModel, onNavigateToProfile: () -> Unit) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Welcome Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate800),
                shape = RoundedCornerShape(16.dp),
                border = BoxBorderDefaults.activeBorder()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Welcome to the AI-Led Talent Development Program!",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This platform is custom-built to align your academic skills with top-tier recruiter standards using state-of-the-art Gemini AI technology.",
                        color = LightGray.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        if (profile == null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate800),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.clickable { onNavigateToProfile() }
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Setup Your AI Talent Profile",
                                color = GlowingAmber,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Submit your CGPA, key skills, and target companies to receive a customized AI assessment report.",
                                color = LightGray.copy(alpha = 0.6f),
                                fontSize = 13.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Setup Profile",
                            tint = CosmicBlue,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        } else {
            item {
                // Readiness Score Circular Meter
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate800),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Placement Readiness Index",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        val score = profile?.talentScore ?: 50
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(130.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = { score / 100f },
                                modifier = Modifier.fillMaxSize(),
                                color = if (score >= 75) Color(0xFF10B981) else if (score >= 50) GlowingAmber else Color(0xFFEF4444),
                                strokeWidth = 12.dp,
                                trackColor = Slate700
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$score%",
                                    color = Color.White,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = if (score >= 75) "Recruiter Ready" else if (score >= 50) "Developing" else "Needs Prep",
                                    color = LightGray.copy(alpha = 0.6f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            item {
                // Current Profile Summary
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate800),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Student Profile",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        ProfileDetailRow(label = "Name", value = profile?.name ?: "")
                        ProfileDetailRow(label = "Career Track", value = profile?.track ?: "")
                        ProfileDetailRow(label = "CGPA", value = String.format("%.2f", profile?.cgpa ?: 0.0))
                        ProfileDetailRow(label = "Core Skills", value = profile?.coreSkills ?: "")
                        ProfileDetailRow(label = "Target Companies", value = profile?.targetCompanies ?: "")
                    }
                }
            }

            item {
                // AI Coach Tip Box
                Card(
                    colors = CardDefaults.cardColors(containerColor = CosmicBlue.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BoxBorderDefaults.glowingBorder()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "AI Coach Hint",
                                tint = CosmicBlue
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI Coach Insight",
                                color = CosmicBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = profile?.assessmentFeedback ?: "Assess your profile to receive advice.",
                            color = LightGray,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = LightGray.copy(alpha = 0.5f), fontSize = 13.sp)
        Text(
            text = value,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f).padding(start = 16.dp)
        )
    }
}

// ==========================================
// 2. PROFILE & AI ASSESSMENT SCREEN
// ==========================================
@Composable
fun ProfileScreen(viewModel: TalentViewModel) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val isAssessing by viewModel.isAssessing.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf(profile?.name ?: "") }
    var track by remember { mutableStateOf(profile?.track ?: "Software Engineer") }
    var cgpaText by remember { mutableStateOf(profile?.cgpa?.toString() ?: "") }
    var skills by remember { mutableStateOf(profile?.coreSkills ?: "") }
    var companies by remember { mutableStateOf(profile?.targetCompanies ?: "") }

    var dropdownExpanded by remember { mutableStateOf(false) }
    val trackOptions = listOf("Software Engineer", "AI/ML Engineer", "Data Analyst", "Product Manager")

    // Update form fields if database changes (e.g., initial load)
    LaunchedEffect(profile) {
        profile?.let {
            name = it.name
            track = it.track
            cgpaText = it.cgpa.toString()
            skills = it.coreSkills
            companies = it.targetCompanies
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Setup AI Talent Profile",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Provide your current credentials to trigger the AI-led evaluation model.",
                color = LightGray.copy(alpha = 0.6f),
                fontSize = 13.sp
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate800, shape = RoundedCornerShape(16.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Student Name") },
                    modifier = Modifier.fillMaxWidth().testTag("profile_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CosmicBlue,
                        unfocusedBorderColor = Slate700
                    )
                )

                // Track Dropdown Selection
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = track,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Target Career Track") },
                        modifier = Modifier.fillMaxWidth().clickable { dropdownExpanded = true },
                        trailingIcon = {
                            IconButton(onClick = { dropdownExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Open tracks", tint = Color.White)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CosmicBlue,
                            unfocusedBorderColor = Slate700
                        )
                    )
                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.background(Slate800)
                    ) {
                        trackOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, color = Color.White) },
                                onClick = {
                                    track = option
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // CGPA field (BITS Pilani uses a 10.0 scale)
                OutlinedTextField(
                    value = cgpaText,
                    onValueChange = { cgpaText = it },
                    label = { Text("Cumulative GPA (CGPA out of 10.0)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("profile_cgpa_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CosmicBlue,
                        unfocusedBorderColor = Slate700
                    )
                )

                // Core Skills
                OutlinedTextField(
                    value = skills,
                    onValueChange = { skills = it },
                    label = { Text("Core Skills (e.g. Java, Python, SQL, Tableau)") },
                    modifier = Modifier.fillMaxWidth().testTag("profile_skills_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CosmicBlue,
                        unfocusedBorderColor = Slate700
                    )
                )

                // Target companies
                OutlinedTextField(
                    value = companies,
                    onValueChange = { companies = it },
                    label = { Text("Target Recruiters (e.g. Amazon, Noon, Careem)") },
                    modifier = Modifier.fillMaxWidth().testTag("profile_companies_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CosmicBlue,
                        unfocusedBorderColor = Slate700
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Submit Action Button
                Button(
                    onClick = {
                        val cgpaVal = cgpaText.toDoubleOrNull() ?: 7.0
                        viewModel.runAssessment(name, track, cgpaVal, skills, companies)
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("profile_submit_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicBlue),
                    shape = RoundedCornerShape(12.dp),
                    enabled = name.isNotBlank() && skills.isNotBlank() && !isAssessing
                ) {
                    if (isAssessing) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("AI is Evaluating...", color = Color.White, fontWeight = FontWeight.Bold)
                    } else {
                        Text("Run AI Talent Assessment", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Display Assessment feedback
        if (profile != null && profile!!.assessmentFeedback.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate800),
                    shape = RoundedCornerShape(16.dp),
                    border = BoxBorderDefaults.glowingBorder()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = "Complete", tint = GlowingAmber)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Assessment Result Summary",
                                color = GlowingAmber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = profile!!.assessmentFeedback,
                            color = Color.White,
                            fontSize = 14.sp,
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "💡 Go to the 'Roadmap' tab to review your custom task items generated based on this review.",
                            color = LightGray.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. LEARNING ROADMAP SCREEN
// ==========================================
@Composable
fun RoadmapScreen(viewModel: TalentViewModel) {
    val items by viewModel.roadmapItems.collectAsStateWithLifecycle()

    val completedCount = items.count { it.isCompleted }
    val totalCount = items.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Your Talent Roadmap",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Track your learning items dynamically. Complete these modules to raise your placement readiness index.",
                color = LightGray.copy(alpha = 0.6f),
                fontSize = 13.sp
            )
        }

        if (totalCount > 0) {
            item {
                // Progress Tracker Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate800),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Milestones Achieved",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "$completedCount of $totalCount",
                                color = GlowingAmber,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        val progressValue = if (totalCount > 0) completedCount.toFloat() / totalCount else 0.0f
                        LinearProgressIndicator(
                            progress = { progressValue },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = GlowingAmber,
                            trackColor = Slate700
                        )
                    }
                }
            }

            items(items) { item ->
                RoadmapCard(item = item, onToggleStatus = { checked ->
                    viewModel.toggleRoadmapItem(item.id, checked)
                })
            }
        } else {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = "No roadmap items",
                        tint = Slate600,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Roadmap Empty",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Please complete your AI Talent Profile assessment in the 'AI Profile' tab to generate a custom roadmap.",
                        color = LightGray.copy(alpha = 0.5f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RoadmapCard(item: LearningRoadmapItem, onToggleStatus: (Boolean) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = item.isCompleted,
                    onCheckedChange = { onToggleStatus(it ?: false) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = GlowingAmber,
                        uncheckedColor = Slate600,
                        checkmarkColor = Slate900
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        color = if (item.isCompleted) LightGray.copy(alpha = 0.5f) else Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Est: ${item.estimatedHours} Hours • Track: ${item.track}",
                        color = LightGray.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Toggle Description",
                    tint = Slate600
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp, start = 48.dp)) {
                    Text(
                        text = item.description,
                        color = LightGray.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

// ==========================================
// 4. AI PREPARATION & MOCK INTERVIEW SCREEN
// ==========================================
@Composable
fun AIPrepScreen(viewModel: TalentViewModel) {
    var prepTab by remember { mutableStateOf(0) } // 0 = AI Mentor, 1 = Mock Interview

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = prepTab,
            containerColor = Slate800,
            contentColor = Color.White,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[prepTab]),
                    color = GlowingAmber
                )
            }
        ) {
            Tab(
                selected = prepTab == 0,
                onClick = { prepTab = 0 },
                text = { Text("AI Mentor Chat", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = prepTab == 1,
                onClick = { prepTab = 1 },
                text = { Text("Mock Interview", fontWeight = FontWeight.Bold) }
            )
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (prepTab) {
                0 -> MentorChatTab(viewModel)
                1 -> MockInterviewTab(viewModel)
            }
        }
    }
}

@Composable
fun MentorChatTab(viewModel: TalentViewModel) {
    val messages by viewModel.mentorMessages.collectAsStateWithLifecycle()
    val isMentorLoading by viewModel.isMentorLoading.collectAsStateWithLifecycle()
    var inputMsg by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    // Automatically scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (messages.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "No messages",
                        tint = Slate600,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Talk to your AI Career Coach",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Ask any career questions, resume tips, or interview queries tailored for Dubai companies.",
                        color = LightGray.copy(alpha = 0.5f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages) { msg ->
                        val isUser = msg.sender == "user"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 12.dp,
                                            topEnd = 12.dp,
                                            bottomStart = if (isUser) 12.dp else 0.dp,
                                            bottomEnd = if (isUser) 0.dp else 12.dp
                                        )
                                    )
                                    .background(if (isUser) CosmicBlue else Slate800)
                                    .padding(12.dp)
                                    .widthIn(max = 280.dp)
                            ) {
                                Text(
                                    text = msg.message,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                    if (isMentorLoading) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Slate800)
                                        .padding(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = GlowingAmber, strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("AI Mentor is thinking...", color = LightGray, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Input Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputMsg,
                onValueChange = { inputMsg = it },
                placeholder = { Text("Ask about placements, resumes...") },
                modifier = Modifier.weight(1f).testTag("mentor_message_input"),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (inputMsg.trim().isNotEmpty()) {
                        viewModel.sendMentorMessage(inputMsg)
                        inputMsg = ""
                        focusManager.clearFocus()
                    }
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = CosmicBlue,
                    unfocusedBorderColor = Slate700
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (inputMsg.trim().isNotEmpty()) {
                        viewModel.sendMentorMessage(inputMsg)
                        inputMsg = ""
                        focusManager.clearFocus()
                    }
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(CosmicBlue)
                    .size(48.dp)
                    .testTag("mentor_send_button")
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }
}

@Composable
fun MockInterviewTab(viewModel: TalentViewModel) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val activeQuestion by viewModel.currentQuestion.collectAsStateWithLifecycle()
    val isGrading by viewModel.isInterviewGrading.collectAsStateWithLifecycle()
    val gradingResult by viewModel.currentGradingResult.collectAsStateWithLifecycle()
    val historyLogs by viewModel.interviewLogs.collectAsStateWithLifecycle()

    var studentAnswer by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val track = profile?.track ?: "Software Engineer"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Mock Technical Assessment",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Simulate technical questions configured for your target role: $track.",
                color = LightGray.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate800),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Question For You:",
                            color = GlowingAmber,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                        IconButton(
                            onClick = {
                                viewModel.loadNewQuestion(track)
                                studentAnswer = ""
                            }
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "New question", tint = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = activeQuestion,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate800, shape = RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = studentAnswer,
                    onValueChange = { studentAnswer = it },
                    placeholder = { Text("Type your technical explanation here...") },
                    modifier = Modifier.fillMaxWidth().height(120.dp).testTag("interview_answer_input"),
                    maxLines = 6,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CosmicBlue,
                        unfocusedBorderColor = Slate700
                    )
                )

                Button(
                    onClick = {
                        viewModel.gradeInterviewAnswer(track, activeQuestion, studentAnswer)
                        focusManager.clearFocus()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("interview_submit_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicBlue),
                    shape = RoundedCornerShape(10.dp),
                    enabled = studentAnswer.isNotBlank() && !isGrading
                ) {
                    if (isGrading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI Grading...", color = Color.White)
                    } else {
                        Text("Submit & Grade Answer", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (gradingResult != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate800),
                    shape = RoundedCornerShape(16.dp),
                    border = BoxBorderDefaults.glowingBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Evaluation feedback",
                                color = GlowingAmber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CosmicBlue.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${gradingResult!!.second}/10",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = gradingResult!!.first,
                            color = Color.White,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                viewModel.loadNewQuestion(track)
                                studentAnswer = ""
                            },
                            modifier = Modifier.fillMaxWidth().testTag("interview_next_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Slate700)
                        ) {
                            Text("Try Another Question", color = Color.White)
                        }
                    }
                }
            }
        }

        if (historyLogs.isNotEmpty()) {
            item {
                Text(
                    text = "Previous Logs",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            items(historyLogs) { log ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate800.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = log.track,
                                color = LightGray.copy(alpha = 0.5f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Score: ${log.score}/10",
                                color = GlowingAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Q: ${log.question}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "Feedback: ${log.feedback}",
                            color = LightGray.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. HELPER COMPOSE BORDERS
// ==========================================
object BoxBorderDefaults {
    fun activeBorder() = BorderStroke(
        width = 1.dp,
        color = Slate700
    )

    fun glowingBorder() = BorderStroke(
        width = 1.dp,
        color = CosmicBlue.copy(alpha = 0.4f)
    )
}
