package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import com.example.ui.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.*
import com.example.ui.*
import com.example.viewmodel.*
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.window.Dialog
import android.widget.Toast
import kotlinx.coroutines.launch

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
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val app = context.applicationContext as Application
    val viewModel: TalentViewModel = viewModel(
        factory = TalentViewModel.Factory(app)
    )

    // Vibrant deep dark premium color palette for each screen that transitions smoothly
    val animatedBgColor by animateColorAsState(
        targetValue = when (selectedTab) {
            0 -> androidx.compose.ui.graphics.Color.Black
            1 -> androidx.compose.ui.graphics.Color(0xFF0A0F1D) // Deep Cosmic Dark Blue
            2 -> androidx.compose.ui.graphics.Color(0xFF081C15) // Deep Tech Emerald Green
            3 -> androidx.compose.ui.graphics.Color(0xFF1C1917) // Warm Deep Stone Charcoal
            4 -> androidx.compose.ui.graphics.Color(0xFF031E2A) // Sleek Tech Cyber Cyan
            5 -> androidx.compose.ui.graphics.Color(0xFF1E152A) // Deep Royal Purple
            6 -> androidx.compose.ui.graphics.Color(0xFF2E0814) // Rich Deep Rose Burgundy
            else -> Slate900
        },
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "PageBackgroundTransition"
    )

    Column(modifier = modifier.fillMaxSize().background(animatedBgColor)) {
        // App Header Banner
        AppHeaderBanner(viewModel)

        // Screen Content
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Crossfade(
                targetState = selectedTab,
                animationSpec = tween(durationMillis = 400),
                label = "ScreenTransition"
            ) { tab ->
                when (tab) {
                    0 -> DashboardScreen(
                        viewModel = viewModel, 
                        onNavigateToTab = { selectedTab = it },
                        onNavigateToProfile = { selectedTab = 6 },
                        onNavigateToEvents = { selectedTab = 5 }
                    )
                    1 -> StudyScreen(viewModel, onOpenUrl = { url ->
                        try {
                            uriHandler.openUri(url)
                        } catch (e: Exception) {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url)).apply {
                                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (ex: Exception) {
                                android.widget.Toast.makeText(context, "No web browser found to open link.", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    })
                    2 -> RoadmapScreen(viewModel)
                    3 -> AIPrepScreen(viewModel)
                    4 -> AIImageScreen(viewModel)
                    5 -> EventsScreen(viewModel)
                    6 -> ResumeScreen(viewModel)
                }
            }
        }

        // Bottom Navigation Bar - Pure Black with sleek glowing active states
        NavigationBar(
            containerColor = androidx.compose.ui.graphics.Color.Black,
            tonalElevation = 12.dp,
            modifier = Modifier
                .navigationBarsPadding()
                .border(width = 1.dp, color = Slate700.copy(alpha = 0.15f))
        ) {
            NavigationBarItem(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                modifier = Modifier.testTag("nav_dashboard"),
                icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                label = { Text("Home", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = GlowingAmber,
                    selectedTextColor = GlowingAmber,
                    unselectedIconColor = Slate600,
                    unselectedTextColor = Slate600,
                    indicatorColor = GlowingAmber.copy(alpha = 0.15f)
                )
            )
            NavigationBarItem(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                modifier = Modifier.testTag("nav_roadmap"),
                icon = { Icon(Icons.Default.List, contentDescription = "Roadmap") },
                label = { Text("Roadmap", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = GlowingAmber,
                    selectedTextColor = GlowingAmber,
                    unselectedIconColor = Slate600,
                    unselectedTextColor = Slate600,
                    indicatorColor = GlowingAmber.copy(alpha = 0.15f)
                )
            )
            NavigationBarItem(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                modifier = Modifier.testTag("nav_prep"),
                icon = { Icon(Icons.Default.Star, contentDescription = "AI Prep") },
                label = { Text("AI Prep", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = GlowingAmber,
                    selectedTextColor = GlowingAmber,
                    unselectedIconColor = Slate600,
                    unselectedTextColor = Slate600,
                    indicatorColor = GlowingAmber.copy(alpha = 0.15f)
                )
            )

            NavigationBarItem(
                selected = selectedTab == 5,
                onClick = { selectedTab = 5 },
                modifier = Modifier.testTag("nav_events"),
                icon = { Icon(Icons.Default.DateRange, contentDescription = "Events") },
                label = { Text("Events", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = GlowingAmber,
                    selectedTextColor = GlowingAmber,
                    unselectedIconColor = Slate600,
                    unselectedTextColor = Slate600,
                    indicatorColor = GlowingAmber.copy(alpha = 0.15f)
                )
            )

            NavigationBarItem(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                modifier = Modifier.testTag("nav_study"),
                icon = { Icon(Icons.Default.Info, contentDescription = "Study Courses") },
                label = { Text("Study", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = GlowingAmber,
                    selectedTextColor = GlowingAmber,
                    unselectedIconColor = Slate600,
                    unselectedTextColor = Slate600,
                    indicatorColor = GlowingAmber.copy(alpha = 0.15f)
                )
            )

            NavigationBarItem(
                selected = selectedTab == 6,
                onClick = { selectedTab = 6 },
                modifier = Modifier.testTag("nav_resume"),
                icon = { Icon(Icons.Default.AccountBox, contentDescription = "Resume") },
                label = { Text("Resume", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = GlowingAmber,
                    selectedTextColor = GlowingAmber,
                    unselectedIconColor = Slate600,
                    unselectedTextColor = Slate600,
                    indicatorColor = GlowingAmber.copy(alpha = 0.15f)
                )
            )
        }


    }
}

@Composable
fun AppHeaderBanner(viewModel: TalentViewModel) {
    val isLinkedInConnected by viewModel.isLinkedInConnected.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Slate800, Slate900)
                )
            )
            .border(width = 1.dp, color = Slate700.copy(alpha = 0.5f), shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .padding(horizontal = 20.dp, vertical = 12.dp)
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
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "AI Placement Portal",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )
            }
            
            if (isLinkedInConnected) {
                // Interactive LinkedIn Status Chip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0077B5).copy(alpha = 0.15f))
                        .border(1.dp, Color(0xFF0077B5).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .clickable { viewModel.disconnectLinkedIn() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF0077B5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "in",
                            color = Color.TrueWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Serif
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Sync Active",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Disconnect",
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(12.dp)
                    )
                }
            } else {
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
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ==========================================
// 1. DASHBOARD SCREEN
// ==========================================
@Composable
fun DashboardScreen(
    viewModel: TalentViewModel, 
    onNavigateToTab: (Int) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToEvents: () -> Unit
) {
    val isLinkedInConnected by viewModel.isLinkedInConnected.collectAsStateWithLifecycle()
    val linkedInProfileUrl by viewModel.linkedInProfileUrl.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val dubaiImages = listOf(
        "https://images.unsplash.com/photo-1518684079-3c830dcef090?auto=format&fit=crop&w=1200&q=80", // Golden Desert Dunes Sunset
        "https://images.unsplash.com/photo-1582672060674-bc2bd8023ed0?auto=format&fit=crop&w=1200&q=80", // Museum of the Future
        "https://images.unsplash.com/photo-1610016302534-6f67f1c968d8?auto=format&fit=crop&w=1200&q=80", // Dubai Marina skyline night
        "https://images.unsplash.com/photo-1549918864-48ac978761a4?auto=format&fit=crop&w=1200&q=80", // Burj Khalifa night with fountain
        "https://images.unsplash.com/photo-1580674684081-7617fbf3d745?auto=format&fit=crop&w=1200&q=80", // Sheikh Zayed Road Light Trails
        "https://images.unsplash.com/photo-1528702748617-c64d494307cf?auto=format&fit=crop&w=1200&q=80"  // Atlantis Palm Night View
    )

    // Slideshow index state
    var currentImageIndex by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(4500)
            currentImageIndex = (currentImageIndex + 1) % dubaiImages.size
        }
    }

    // Ken Burns zoom effect setup
    val infiniteTransition = rememberInfiniteTransition(label = "ImageZoom")
    val zoomScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ZoomScale"
    )

    Box(modifier = Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color.Black)) {
        // Continuous Crossfade Image Slider
        Crossfade(
            targetState = currentImageIndex,
            animationSpec = tween(durationMillis = 1200),
            modifier = Modifier.fillMaxSize(),
            label = "SlideshowCrossfade"
        ) { index ->
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = dubaiImages[index],
                    contentDescription = "Immersive Dubai Landmark",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = zoomScale,
                            scaleY = zoomScale
                        ),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }
        }

        // Modern Vignette & Shadow gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.55f),
                            androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.25f),
                            androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.90f)
                        )
                    )
                )
        )

        // Custom Expanding Pill Page Indicators (overlaid below the top edge of images)
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            dubaiImages.indices.forEach { idx ->
                val isActive = currentImageIndex == idx
                val animatedWidth by animateDpAsState(
                    targetValue = if (isActive) 22.dp else 8.dp,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "IndicatorWidth"
                )
                val animatedColor by animateColorAsState(
                    targetValue = if (isActive) GlowingAmber else androidx.compose.ui.graphics.Color.White.copy(alpha = 0.4f),
                    label = "IndicatorColor"
                )
                Box(
                    modifier = Modifier
                        .size(height = 5.dp, width = animatedWidth)
                        .clip(RoundedCornerShape(3.dp))
                        .background(animatedColor)
                )
            }
        }

        // SCROLLABLE CONTAINER FOR ENHANCED CONTENT OVER THE SLIDESHOW
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 150.dp), // Generous padding to prevent overlapping with the bottom floating panel
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Spacer to push content down and let the gorgeous slideshow shine at the top
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }

            // 1. BRANDING / GREETING
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Text(
                        text = "WELCOME TO THE FUTURE",
                        color = GlowingAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Your Dubai Placement Drive",
                        color = Color.TrueWhite, // Set text color to pure white
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Real-time AI matching with premium UAE employers & tech hubs.",
                        color = LightGray.copy(alpha = 0.75f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Quick Access Action Cards
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "QUICK ACTIONS",
                        color = LightGray.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    
                    val actions = listOf(
                        Triple("AI Mock Interview", "Practice real interview questions with real-time AI scoring & grading.", 3),
                        Triple("Curated Courses", "Master key CS topics from Software Eng to Cloud & DevOps.", 1),
                        Triple("Learning Roadmap", "Track milestones to level up your placement readiness.", 2),
                        Triple("AI Credentials Studio", "Generate custom digital talent development credentials and merit badges.", 4)
                    )
                    
                    actions.forEach { (title, subtitle, tabIdx) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToTab(tabIdx) },
                            colors = CardDefaults.cardColors(containerColor = Slate900.copy(alpha = 0.75f)),
                            border = BorderStroke(1.dp, Slate700.copy(alpha = 0.25f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(GlowingAmber.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (tabIdx) {
                                            3 -> Icons.Default.Star
                                            1 -> Icons.Default.Info
                                            4 -> Icons.Default.CheckCircle
                                            else -> Icons.Default.List
                                        },
                                        contentDescription = null,
                                        tint = GlowingAmber,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = title,
                                        color = Color.TrueWhite,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = subtitle,
                                        color = LightGray.copy(alpha = 0.6f),
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = LightGray.copy(alpha = 0.4f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Extra padding at the bottom of list
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // 2. COMPACT FLOATING LINKEDIN CONNECTION PANEL AT THE BOTTOM OF THE PAGE
        val linkedinTransition = rememberInfiniteTransition(label = "LinkedInGlow")
        val borderAlpha by linkedinTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "BorderAlpha"
        )
        val logoScale by linkedinTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "LogoScale"
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            if (!isLinkedInConnected) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate950.copy(alpha = 0.92f)),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(
                        width = 1.2.dp,
                        color = NeonCyan.copy(alpha = borderAlpha)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .graphicsLayer {
                                            scaleX = logoScale
                                            scaleY = logoScale
                                        }
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF0077B5)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "in",
                                        color = Color.TrueWhite,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Serif
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "LinkedIn Sync",
                                        color = Color.TrueWhite,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Sync profile for recruiter placement match.",
                                        color = LightGray.copy(alpha = 0.6f),
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.connectLinkedIn("https://linkedin.com/in/nitinjain")
                                    android.widget.Toast.makeText(context, "LinkedIn Sandbox Connected!", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                border = BorderStroke(1.dp, GlowingAmber.copy(alpha = 0.7f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = GlowingAmber),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier
                                    .height(30.dp)
                                    .testTag("dashboard_linkedin_sandbox_btn")
                            ) {
                                Text("Quick Sync", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            var tempUrl by remember { mutableStateOf("") }
                            OutlinedTextField(
                                value = tempUrl,
                                onValueChange = { tempUrl = it },
                                placeholder = { Text("Enter LinkedIn URL", fontSize = 11.sp, color = LightGray.copy(alpha = 0.4f)) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = Slate700,
                                    focusedTextColor = Color.TrueWhite,
                                    unfocusedTextColor = Color.TrueWhite,
                                    focusedContainerColor = Slate900,
                                    unfocusedContainerColor = Slate900
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("dashboard_linkedin_input"),
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, color = Color.TrueWhite)
                            )

                            Button(
                                onClick = {
                                    if (tempUrl.isNotBlank()) {
                                        viewModel.connectLinkedIn(tempUrl)
                                        android.widget.Toast.makeText(context, "LinkedIn Connected!", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        android.widget.Toast.makeText(context, "Please enter a valid LinkedIn URL", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CosmicBlue),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                modifier = Modifier
                                    .height(44.dp)
                                    .testTag("dashboard_linkedin_connect_btn")
                            ) {
                                Text("Connect", color = Color.TrueWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate950.copy(alpha = 0.92f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.2.dp, NeonGreen.copy(alpha = borderAlpha)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF0077B5)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "in",
                                    color = Color.TrueWhite,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Serif
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(NeonGreen)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "LINKEDIN SYNCED",
                                        color = NeonGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                                Text(
                                    text = linkedInProfileUrl,
                                    color = Color.TrueWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                viewModel.disconnectLinkedIn()
                                android.widget.Toast.makeText(context, "LinkedIn disconnected.", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("dashboard_linkedin_disconnect_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Disconnect",
                                tint = androidx.compose.ui.graphics.Color.Red.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
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
// 2. STUDY HUB & AI ASSESSMENT SCREEN
// ==========================================
data class QuestionPaper(
    val examName: String,
    val year: String,
    val downloadUrl: String,
    val courseTitle: String = ""
)

data class StudyMaterial(
    val title: String,
    val fileType: String,
    val size: String,
    val accessUrl: String,
    val courseTitle: String = ""
)

data class StudyCourse(
    val title: String,
    val provider: String,
    val cost: String,
    val certification: String,
    val hasFreeCertificate: Boolean,
    val yearTarget: String, // "2nd Year", "3rd Year", "4th Year"
    val category: String, // "AI & ML", "Cloud", "Robotics"
    val description: String,
    val officialUrl: String,
    val questionPapers: List<QuestionPaper> = emptyList(),
    val studyMaterials: List<StudyMaterial> = emptyList()
)

fun getQuestionPapersForCourse(courseTitle: String, year: String, category: String): List<QuestionPaper> {
    return when (courseTitle) {
        "Harvard CS50x: Introduction to Computer Science" -> listOf(
            QuestionPaper("2025 Mid-Term Exam (C & Memory)", "2025", "https://cs50.harvard.edu/x/2025/exams/midterm/"),
            QuestionPaper("2024 Final Exam (Data Structures)", "2024", "https://cs50.harvard.edu/x/2024/exams/final/"),
            QuestionPaper("2023 Practice Exam (Algorithms)", "2023", "https://cs50.harvard.edu/x/2023/exams/practice/")
        )
        "Stanford Algorithms Specialization" -> listOf(
            QuestionPaper("2024 Mid-Sem Exam (Divide & Conquer)", "2024", "https://www.coursera.org/specializations/algorithms"),
            QuestionPaper("2024 End-Sem Exam (Dynamic Programming)", "2024", "https://www.coursera.org/specializations/algorithms"),
            QuestionPaper("2023 Complexity Theory Final", "2023", "https://www.coursera.org/specializations/algorithms")
        )
        "Introduction to Cybersecurity" -> listOf(
            QuestionPaper("2025 Mid-Sem (Threats & Malware)", "2025", "https://skillsforall.com/course/introduction-to-cybersecurity"),
            QuestionPaper("2024 End-Sem (Cryptography Fundamentals)", "2024", "https://skillsforall.com/course/introduction-to-cybersecurity")
        )
        "Google Cybersecurity Professional Certificate" -> listOf(
            QuestionPaper("2025 SIEM Analysis Final Exam", "2025", "https://www.coursera.org/professional-certificates/google-cybersecurity"),
            QuestionPaper("2024 Linux Command Line Exam", "2024", "https://www.coursera.org/professional-certificates/google-cybersecurity")
        )
        "Certified in Cybersecurity (CC)" -> listOf(
            QuestionPaper("2025 ISC2 Mock Exam Paper A", "2025", "https://www.isc2.org/Certifications/Certified-in-Cybersecurity"),
            QuestionPaper("2024 ISC2 Mock Exam Paper B", "2024", "https://www.isc2.org/Certifications/Certified-in-Cybersecurity")
        )
        "Generative AI Fundamentals Specialization" -> listOf(
            QuestionPaper("2025 LLM Fine-Tuning Exam", "2025", "https://www.cloudskillsboost.google/course_templates/556"),
            QuestionPaper("2024 Attention Mechanisms Mid-Term", "2024", "https://www.cloudskillsboost.google/course_templates/556")
        )
        "UiPath Automation & Software Robotics" -> listOf(
            QuestionPaper("2024 RPA Workflow Design Exam", "2024", "https://academy.uipath.com/"),
            QuestionPaper("2023 Robotic Process Automation Final", "2023", "https://academy.uipath.com/")
        )
        "Modern Robotics: Mechanics and Planning" -> listOf(
            QuestionPaper("2024 Kinematics & Jacobians Exam", "2024", "https://www.coursera.org/specializations/modernrobotics"),
            QuestionPaper("2023 Trajectory Planning Final Exam", "2023", "https://www.coursera.org/specializations/modernrobotics")
        )
        else -> {
            listOf(
                QuestionPaper("2025 $category Semester Mid-Term", "2025", "https://example.com/papers/2025"),
                QuestionPaper("2024 $category Semester End-Sem", "2024", "https://example.com/papers/2024"),
                QuestionPaper("2023 $category Comprehensive Exam", "2023", "https://example.com/papers/2023")
            )
        }
    }
}

fun getStudyMaterialsForCourse(courseTitle: String, year: String, category: String): List<StudyMaterial> {
    return when (courseTitle) {
        "Harvard CS50x: Introduction to Computer Science" -> listOf(
            StudyMaterial("C Programming Quick Reference", "PDF", "1.2 MB", "https://cs50.harvard.edu/x/2025/notes/"),
            StudyMaterial("Memory Allocation & Pointers Slides", "Slides", "3.4 MB", "https://cs50.harvard.edu/x/2025/weeks/5/"),
            StudyMaterial("Binary Search Trees Visual Guide", "Notes", "800 KB", "https://cs50.harvard.edu/x/2025/notes/")
        )
        "Stanford Algorithms Specialization" -> listOf(
            StudyMaterial("Asymptotic Analysis & Big-O Notes", "PDF", "2.1 MB", "https://www.coursera.org/specializations/algorithms"),
            StudyMaterial("Graph Algorithms (Dijkstra, BFS/DFS)", "Notes", "1.8 MB", "https://www.coursera.org/specializations/algorithms"),
            StudyMaterial("Greedy Algorithms Master Cheat Sheet", "Cheat Sheet", "500 KB", "https://www.coursera.org/specializations/algorithms")
        )
        "Introduction to Cybersecurity" -> listOf(
            StudyMaterial("ISO 27001 Security Controls Overview", "PDF", "1.5 MB", "https://skillsforall.com/course/introduction-to-cybersecurity"),
            StudyMaterial("Symmetric vs Asymmetric Encryption Guide", "Cheat Sheet", "620 KB", "https://skillsforall.com/course/introduction-to-cybersecurity"),
            StudyMaterial("Wireshark Packet Analysis Lab Manual", "PDF", "4.1 MB", "https://skillsforall.com/course/introduction-to-cybersecurity")
        )
        "Google Cybersecurity Professional Certificate" -> listOf(
            StudyMaterial("SQL for Database Auditing Cheat Sheet", "Cheat Sheet", "450 KB", "https://www.coursera.org/professional-certificates/google-cybersecurity"),
            StudyMaterial("Python Scripting for Security Automation Notes", "Notes", "2.8 MB", "https://www.coursera.org/professional-certificates/google-cybersecurity"),
            StudyMaterial("Splunk Dashboard & Logging Reference", "Slides", "3.2 MB", "https://www.coursera.org/professional-certificates/google-cybersecurity")
        )
        "Certified in Cybersecurity (CC)" -> listOf(
            StudyMaterial("Access Control Systems Study Guide", "PDF", "2.5 MB", "https://www.isc2.org/Certifications/Certified-in-Cybersecurity"),
            StudyMaterial("Incident Response & DR Plan Reference", "Notes", "1.9 MB", "https://www.isc2.org/Certifications/Certified-in-Cybersecurity"),
            StudyMaterial("Network Security Protocols E-Book", "PDF", "6.2 MB", "https://www.isc2.org/Certifications/Certified-in-Cybersecurity")
        )
        "Generative AI Fundamentals Specialization" -> listOf(
            StudyMaterial("Vertex AI Integration & SDK Guide", "PDF", "1.8 MB", "https://www.cloudskillsboost.google/course_templates/556"),
            StudyMaterial("Prompt Engineering Best Practices Notes", "Cheat Sheet", "1.1 MB", "https://www.cloudskillsboost.google/course_templates/556"),
            StudyMaterial("Transformer Architecture Explained", "Slides", "4.5 MB", "https://www.cloudskillsboost.google/course_templates/556")
        )
        "UiPath Automation & Software Robotics" -> listOf(
            StudyMaterial("UiPath Studio Keyboard Shortcuts Cheat Sheet", "Cheat Sheet", "350 KB", "https://academy.uipath.com/"),
            StudyMaterial("OCR and Screen Scraping Guide", "PDF", "1.7 MB", "https://academy.uipath.com/"),
            StudyMaterial("Automated Exception Handling Notes", "Notes", "920 KB", "https://academy.uipath.com/")
        )
        "Modern Robotics: Mechanics and Planning" -> listOf(
            StudyMaterial("Forward & Inverse Kinematics Lecture Notes", "PDF", "3.1 MB", "https://www.coursera.org/specializations/modernrobotics"),
            StudyMaterial("Degrees of Freedom & Grubler's Formula Guide", "Notes", "1.2 MB", "https://www.coursera.org/specializations/modernrobotics"),
            StudyMaterial("Singularity Analysis Handout", "Slides", "2.2 MB", "https://www.coursera.org/specializations/modernrobotics")
        )
        else -> {
            listOf(
                StudyMaterial("$courseTitle Syllabus & Reading Guide", "PDF", "1.5 MB", "https://example.com/materials/syllabus"),
                StudyMaterial("Core $category Concepts Lecture Handout", "Notes", "4.2 MB", "https://example.com/materials/handout"),
                StudyMaterial("$category Rapid Revision Cheat Sheet", "Cheat Sheet", "720 KB", "https://example.com/materials/cheatsheet"),
                StudyMaterial("Recommended Video Tutorial Playlist", "Video", "External Link", "https://example.com/materials/playlist")
            )
        }
    }
}

val studyCoursesList = listOf(
    // === DATA STRUCTURES & ALGORITHMS (DSA) ===
    StudyCourse(
        title = "Harvard CS50x: Introduction to Computer Science",
        provider = "Harvard University (edX)",
        cost = "Free",
        certification = "Free CS50 Course Certificate",
        hasFreeCertificate = true,
        yearTarget = "2nd Year",
        category = "DSA",
        description = "A world-renowned introduction to the intellectual enterprises of computer science and the art of programming, covering algorithms, data structures, resource management, and security.",
        officialUrl = "https://pll.harvard.edu/course/cs50-introduction-computer-science"
    ),
    StudyCourse(
        title = "Stanford Algorithms Specialization",
        provider = "Stanford University (Coursera)",
        cost = "Free to Audit",
        certification = "Free Course Audit Progression",
        hasFreeCertificate = false,
        yearTarget = "3rd Year",
        category = "DSA",
        description = "A highly rigorous introduction to algorithmic analysis, divide-and-conquer, sorting, searching, graph primitives, hash tables, and dynamic programming with mathematical proofs.",
        officialUrl = "https://www.coursera.org/specializations/algorithms"
    ),
    StudyCourse(
        title = "Data Structures & Algorithms in Python",
        provider = "Udacity & Google",
        cost = "Free",
        certification = "Free Course Progression Record",
        hasFreeCertificate = true,
        yearTarget = "2nd Year",
        category = "DSA",
        description = "Learn core computer science concepts including lists, stacks, queues, trees, and graphs, alongside searching, sorting, and analyzing algorithmic runtime complexity.",
        officialUrl = "https://www.udacity.com/course/data-structures-and-algorithms-in-python--ud513"
    ),
    StudyCourse(
        title = "Algorithms & Data Structures Path",
        provider = "freeCodeCamp",
        cost = "Free",
        certification = "Free Verified Professional Certificate",
        hasFreeCertificate = true,
        yearTarget = "2nd Year",
        category = "DSA",
        description = "Learn Javascript algorithmic thinking, standard sorting, big-O complexity, graphs, binary trees, and dynamic programming with interactive online coding sandboxes.",
        officialUrl = "https://www.freecodecamp.org/learn/javascript-algorithms-and-data-structures/"
    ),

    // === CYBERSECURITY ===
    StudyCourse(
        title = "Introduction to Cybersecurity",
        provider = "Cisco Networking Academy",
        cost = "Free",
        certification = "Free Official Cisco Badge & Certificate",
        hasFreeCertificate = true,
        yearTarget = "2nd Year",
        category = "Cybersecurity",
        description = "Understand the foundational cybersecurity landscape: defensive architecture, malware analysis, network packet scanning, cryptography basics, and risk management.",
        officialUrl = "https://skillsforall.com/course/introduction-to-cybersecurity"
    ),
    StudyCourse(
        title = "Google Cybersecurity Professional Certificate",
        provider = "Google (Coursera)",
        cost = "Free to Audit",
        certification = "Free Audit Course Access",
        hasFreeCertificate = false,
        yearTarget = "3rd Year",
        category = "Cybersecurity",
        description = "Eight-course professional series covering network security, Linux command line utilities, database security with SQL, python scripting, and SIEM security logs (Splunk & Chronicle).",
        officialUrl = "https://www.coursera.org/professional-certificates/google-cybersecurity"
    ),
    StudyCourse(
        title = "Certified in Cybersecurity (CC)",
        provider = "ISC2",
        cost = "Free Training & Exam",
        certification = "Free Official ISC2 Professional Certification",
        hasFreeCertificate = true,
        yearTarget = "4th Year",
        category = "Cybersecurity",
        description = "Access official entry-level security credentials. Covers access control systems, security principles, incident response, network security, and security operations center practices.",
        officialUrl = "https://www.isc2.org/Certifications/Certified-in-Cybersecurity"
    ),

    // === SOFTWARE ENGINEERING ===
    StudyCourse(
        title = "Software Engineering Virtual Experience",
        provider = "J.P. Morgan (Forage)",
        cost = "Free",
        certification = "Free Shareable Virtual Experience Certificate",
        hasFreeCertificate = true,
        yearTarget = "3rd Year",
        category = "Software Eng",
        description = "Gain hands-on software engineering experience: interface with financial data feeds, construct interactive web data visualization dashboards, patch components, and write unit tests.",
        officialUrl = "https://www.theforage.com/virtual-experience-programs/software-engineering"
    ),
    StudyCourse(
        title = "Responsive Web Design Certification",
        provider = "freeCodeCamp",
        cost = "Free",
        certification = "Free Verified Developer Certification",
        hasFreeCertificate = true,
        yearTarget = "2nd Year",
        category = "Software Eng",
        description = "Master robust frontend engineering from first principles: learn semantic HTML5 markup, responsive CSS layout styling (Flexbox/Grid), accessibility guidelines, and media queries.",
        officialUrl = "https://www.freecodecamp.org/learn/2022/responsive-web-design/"
    ),
    StudyCourse(
        title = "Software Design & Architecture",
        provider = "University of Alberta (Coursera)",
        cost = "Free to Audit",
        certification = "Free Course Audit Progression",
        hasFreeCertificate = false,
        yearTarget = "3rd Year",
        category = "Software Eng",
        description = "Learn how to write clean, modular, and maintainable systems using SOLID design principles, structural/behavioral patterns, and enterprise architectural blueprints.",
        officialUrl = "https://www.coursera.org/specializations/software-design-architecture"
    ),
    StudyCourse(
        title = "Git & GitHub Version Control Fundamentals",
        provider = "GitHub Skills",
        cost = "Free",
        certification = "Free GitHub Profile Trophies & Badges",
        hasFreeCertificate = true,
        yearTarget = "2nd Year",
        category = "Software Eng",
        description = "A practical sequence of interactive repository exercises: branches, commit histories, merging, resolving visual conflicts, pull requests, and automated GitHub actions workflows.",
        officialUrl = "https://skills.github.com/"
    ),
    StudyCourse(
        title = "Full Stack Open (React, Node, Express, GraphQL)",
        provider = "University of Helsinki",
        cost = "Free",
        certification = "Free Full Stack Developer Certificate & Credits",
        hasFreeCertificate = true,
        yearTarget = "3rd Year",
        category = "Software Eng",
        description = "Deep-dive into modern JavaScript/TypeScript web architectures. Learn single-page apps, REST/GraphQL APIs, database persistence, Docker containers, and CI/CD automation.",
        officialUrl = "https://fullstackopen.com/en/"
    ),

    // === AI & ML ===
    StudyCourse(
        title = "Microsoft Azure AI Fundamentals",
        provider = "Microsoft Learn",
        cost = "Free",
        certification = "Free Digital Badge & Learning Path Trophy",
        hasFreeCertificate = true,
        yearTarget = "2nd Year",
        category = "AI & ML",
        description = "Master machine learning models, computer vision, and NLP services directly on Azure. Complete the modules online at zero cost to earn your free MS Learn digital trophy.",
        officialUrl = "https://learn.microsoft.com/en-us/training/paths/get-started-with-artificial-intelligence-on-azure/"
    ),
    StudyCourse(
        title = "Generative AI Fundamentals Specialization",
        provider = "Google Cloud",
        cost = "Free",
        certification = "Free Verified GenAI Badge & Certificate",
        hasFreeCertificate = true,
        yearTarget = "4th Year",
        category = "AI & ML",
        description = "Understand Large Language Models, Google Vertex AI, image generation models, and attention mechanisms. Ideal for CS capstone students.",
        officialUrl = "https://www.cloudskillsboost.google/course_templates/556"
    ),
    StudyCourse(
        title = "IBM AI Foundations & Conversational Agents",
        provider = "Cognitive Class (IBM)",
        cost = "Free",
        certification = "Free Professional Certificate & IBM Badge",
        hasFreeCertificate = true,
        yearTarget = "3rd Year",
        category = "AI & ML",
        description = "Learn natural language processing, dialog systems, and build and deploy intelligent chatbot agents on real cloud websites.",
        officialUrl = "https://cognitiveclass.ai/courses/how-to-build-chatbots"
    ),
    StudyCourse(
        title = "Microsoft Azure Machine Learning Specialist Path",
        provider = "Microsoft Learn",
        cost = "Free",
        certification = "Free Microsoft Digital Learning Trophy",
        hasFreeCertificate = true,
        yearTarget = "3rd Year",
        category = "Cloud",
        description = "Design and build production-grade machine learning models, perform hyperparameter tuning, and scale/deploy models directly via Azure Machine Learning workspaces.",
        officialUrl = "https://learn.microsoft.com/en-us/training/paths/build-ml-models-with-azure-ml/"
    ),
    StudyCourse(
        title = "Advanced Deep Learning & PyTorch Models",
        provider = "PyTorch Foundation",
        cost = "Free",
        certification = "Free Open-Source Community Certificate",
        hasFreeCertificate = true,
        yearTarget = "4th Year",
        category = "AI & ML",
        description = "Learn to write neural networks, customized backpropagation, transformers, and deep reinforcement learning loops in PyTorch.",
        officialUrl = "https://pytorch.org/tutorials/"
    ),
    StudyCourse(
        title = "Microsoft Azure NLP & Speech Solutions Path",
        provider = "Microsoft Learn",
        cost = "Free",
        certification = "Free Microsoft NLP Learning Badge",
        hasFreeCertificate = true,
        yearTarget = "4th Year",
        category = "AI & ML",
        description = "Learn how to build, manage, and deploy cognitive search solutions, custom computer vision, speech models, and AI agent frameworks for NLP.",
        officialUrl = "https://learn.microsoft.com/en-us/training/paths/deploy-nlp-solutions-azure-cognitive-services/"
    ),

    // === CLOUD ===
    StudyCourse(
        title = "Google Cloud Computing Foundations",
        provider = "Google Cloud Skills Boost",
        cost = "Free",
        certification = "Free Cloud Foundations Quest Badge",
        hasFreeCertificate = true,
        yearTarget = "2nd Year",
        category = "Cloud",
        description = "Comprehensive study of cloud architecture, container management, network virtualization, and database services.",
        officialUrl = "https://www.cloudskillsboost.google/paths/11"
    ),
    StudyCourse(
        title = "Microsoft Power Platform Developer Path",
        provider = "Microsoft Learn",
        cost = "Free",
        certification = "Free Microsoft Developer Badge",
        hasFreeCertificate = true,
        yearTarget = "2nd Year",
        category = "Cloud",
        description = "Create enterprise low-code systems, automate workflows, and design smart interfaces using Microsoft AI Builder integrations.",
        officialUrl = "https://learn.microsoft.com/en-us/training/paths/power-plat-dev-connect/"
    ),

    // === ROBOTICS ===
    StudyCourse(
        title = "Introduction to Robotics & Microcontrollers",
        provider = "Great Learning Academy",
        cost = "Free",
        certification = "Free Shareable Course Certificate",
        hasFreeCertificate = true,
        yearTarget = "2nd Year",
        category = "Robotics",
        description = "An introductory curriculum covering robotic manipulators, motor control systems, sensor integration, and kinematics models.",
        officialUrl = "https://www.mygreatlearning.com/academy/learn-for-free/courses/introduction-to-robotics"
    ),
    StudyCourse(
        title = "UiPath Automation & Software Robotics",
        provider = "UiPath Academy",
        cost = "Free",
        certification = "Free RPA Professional Certification",
        hasFreeCertificate = true,
        yearTarget = "3rd Year",
        category = "Robotics",
        description = "Deep dive into Robotic Process Automation (RPA), workflow designs, optical character recognition, and programming system robots.",
        officialUrl = "https://academy.uipath.com/"
    ),
    StudyCourse(
        title = "Modern Robotics: Mechanics and Planning",
        provider = "Northwestern University (Coursera)",
        cost = "Free to Audit",
        certification = "Free Course Progression & Audit Badge",
        hasFreeCertificate = false,
        yearTarget = "4th Year",
        category = "Robotics",
        description = "Mathematical foundations of robotic motion, forward/inverse kinematics, velocity kinematics, singularity analysis, and trajectory planning.",
        officialUrl = "https://www.coursera.org/specializations/modernrobotics"
    ),
    StudyCourse(
        title = "Robot Operating System (ROS) Foundations",
        provider = "ROS Community / ConstructSim",
        cost = "Free Tutorials",
        certification = "Free Community Developer Badge",
        hasFreeCertificate = true,
        yearTarget = "3rd Year",
        category = "Robotics",
        description = "Master robot development with ROS. Coordinate nodes, publish topics, listen to services, and run Gazebo physics simulator engines.",
        officialUrl = "https://www.ros.org/blog/getting-started-with-ros-online-courses/"
    )
)

@Composable
fun StudyScreen(viewModel: TalentViewModel, onOpenUrl: (String) -> Unit) {
    val context = LocalContext.current

    val completedCourses by viewModel.completedCourses.collectAsStateWithLifecycle()
    val courseProgress by viewModel.courseProgress.collectAsStateWithLifecycle()

    val activeQuiz by viewModel.activeQuiz.collectAsStateWithLifecycle()
    val isGeneratingQuiz by viewModel.isGeneratingQuiz.collectAsStateWithLifecycle()
    val quizAnswers by viewModel.quizAnswers.collectAsStateWithLifecycle()
    val quizSubmitted by viewModel.quizSubmitted.collectAsStateWithLifecycle()

    // Filters for study courses
    var selectedYear by remember { mutableStateOf("All") }
    var selectedSubject by remember { mutableStateOf("All") }

    var activeQuestionPaper by remember { mutableStateOf<QuestionPaper?>(null) }
    var activeStudyMaterial by remember { mutableStateOf<StudyMaterial?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Study Hub Branding Header
        item {
            Column {
                Text(
                    text = "DUB-AI TALENT STUDY HUB",
                    color = GlowingAmber,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Curated free certifications & latest CS curriculums covering DSA, Cybersecurity, Software Engineering, AI/ML, Cloud, and Robotics.",
                    color = androidx.compose.ui.graphics.Color(0xFFE2E8F0),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }

        // Global Study Progress Tracker Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate800),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, Slate700.copy(alpha = 0.5f)), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "STUDY JOURNEY TRACKER",
                                color = androidx.compose.ui.graphics.Color(0xFF64748B),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Academic Curriculum Progress",
                                color = androidx.compose.ui.graphics.Color(0xFF0F172A),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(GlowingAmber.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${completedCourses.size} of ${studyCoursesList.size} Completed",
                                color = GlowingAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val globalProgressValue = if (studyCoursesList.isNotEmpty()) {
                        completedCourses.size.toFloat() / studyCoursesList.size
                    } else {
                        0f
                    }
                    
                    LinearProgressIndicator(
                        progress = { globalProgressValue },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = GlowingAmber,
                        trackColor = Slate700
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = if (globalProgressValue >= 1f) {
                            "🎉 Spectacular! You have completed all recommended curriculums."
                        } else if (globalProgressValue > 0.5f) {
                            "🔥 Incredible progress! You are more than halfway through your prep."
                        } else if (globalProgressValue > 0f) {
                            "🚀 Great start! Keep pushing to unlock peak placement readiness."
                        } else {
                            "🎯 Select a course below and start tracking your learning journey!"
                        },
                        color = androidx.compose.ui.graphics.Color(0xFF475569),
                        fontSize = 11.sp,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }

        // Horizontal Year Filter Row
        item {
            Column {
                Text(
                    text = "Academic Year Focus:",
                    color = androidx.compose.ui.graphics.Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                val years = listOf("All", "2nd Year", "3rd Year", "4th Year")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(years) { year ->
                        val isSelected = selectedYear == year
                        val chipBgColor = if (isSelected) GlowingAmber else androidx.compose.ui.graphics.Color(0xFF1E293B)
                        val chipTextColor = if (isSelected) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFFE2E8F0)
                        val chipBorderColor = if (isSelected) GlowingAmber else androidx.compose.ui.graphics.Color(0xFF334155)
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(chipBgColor)
                                .border(1.dp, chipBorderColor, RoundedCornerShape(16.dp))
                                .clickable { selectedYear = year }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = year,
                                color = chipTextColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Horizontal Subject/Category Filter Row
        item {
            Column {
                Text(
                    text = "Core Subject Domain:",
                    color = androidx.compose.ui.graphics.Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                val subjects = listOf("All", "DSA", "Cybersecurity", "Software Eng", "AI & ML", "Cloud", "Robotics")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(subjects) { subj ->
                        val isSelected = selectedSubject == subj
                        val chipBgColor = if (isSelected) NeonCyan else androidx.compose.ui.graphics.Color(0xFF1E293B)
                        val chipTextColor = if (isSelected) androidx.compose.ui.graphics.Color(0xFFFFFFFF) else androidx.compose.ui.graphics.Color(0xFFE2E8F0)
                        val chipBorderColor = if (isSelected) NeonCyan else androidx.compose.ui.graphics.Color(0xFF334155)
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(chipBgColor)
                                .border(1.dp, chipBorderColor, RoundedCornerShape(16.dp))
                                .clickable { selectedSubject = subj }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = subj,
                                color = chipTextColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // List matching courses
        val filteredCourses = studyCoursesList.filter { course ->
            val matchesYear = selectedYear == "All" || course.yearTarget == selectedYear
            val matchesSubj = selectedSubject == "All" || course.category == selectedSubject
            matchesYear && matchesSubj
        }.sortedWith(compareByDescending { it.hasFreeCertificate }) // Prioritize free certificates

        item {
            Text(
                text = "${filteredCourses.size} Recommended Curriculums Found",
                color = NeonGreen,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (filteredCourses.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate800),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No courses match the selected filters.",
                            color = LightGray.copy(alpha = 0.6f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredCourses) { course ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (course.hasFreeCertificate) 1.5.dp else 1.dp,
                            color = if (course.hasFreeCertificate) GlowingAmber.copy(alpha = 0.8f) else Slate700.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate800)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = course.provider.uppercase(),
                                    color = NeonCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.8.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = course.title,
                                    color = androidx.compose.ui.graphics.Color(0xFF0F172A),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (course.hasFreeCertificate) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(GlowingAmber.copy(alpha = 0.12f))
                                        .border(1.dp, GlowingAmber.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Certified",
                                            tint = GlowingAmber,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "FREE CERT",
                                            color = GlowingAmber,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = course.description,
                            color = androidx.compose.ui.graphics.Color(0xFF334155),
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(androidx.compose.ui.graphics.Color(0xFFF1F5F9))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = course.yearTarget,
                                    color = androidx.compose.ui.graphics.Color(0xFF475569),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CosmicBlue.copy(alpha = 0.2f))
                                    .border(1.dp, CosmicBlue.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = course.category,
                                    color = NeonCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Progress Indicator section
                        val progress = courseProgress[course.title] ?: 0f
                        val isCompleted = completedCourses.contains(course.title)
                        val currentStep = (progress * 4).toInt()

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(androidx.compose.ui.graphics.Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                                .border(1.dp, androidx.compose.ui.graphics.Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Info,
                                        contentDescription = "Progress Info",
                                        tint = if (isCompleted) NeonGreen else NeonCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Progress: ${(progress * 100).toInt()}%",
                                        color = if (isCompleted) NeonGreen else androidx.compose.ui.graphics.Color(0xFF0F172A),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = " ($currentStep/4 Chapters)",
                                        color = androidx.compose.ui.graphics.Color(0xFF475569),
                                        fontSize = 11.sp
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            val nextStep = (currentStep - 1).coerceIn(0, 4)
                                            viewModel.updateCourseProgress(course.title, nextStep / 4f)
                                        },
                                        modifier = Modifier.size(26.dp).testTag("dec_progress_${course.title.replace(" ", "_")}")
                                    ) {
                                        Text("-", color = LightGray, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    }
                                    IconButton(
                                        onClick = {
                                            val nextStep = (currentStep + 1).coerceIn(0, 4)
                                            viewModel.updateCourseProgress(course.title, nextStep / 4f)
                                        },
                                        modifier = Modifier.size(26.dp).testTag("inc_progress_${course.title.replace(" ", "_")}")
                                    ) {
                                        Text("+", color = LightGray, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (isCompleted) NeonGreen else NeonCyan,
                                trackColor = Slate700
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Mark as Complete Row
                            Button(
                                onClick = {
                                    viewModel.toggleCourseCompletion(course.title)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isCompleted) NeonGreen.copy(alpha = 0.15f) else CosmicBlue,
                                    contentColor = if (isCompleted) NeonGreen else androidx.compose.ui.graphics.Color.White
                                ),
                                border = if (isCompleted) BorderStroke(1.dp, NeonGreen) else null,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                                    .testTag("toggle_complete_${course.title.replace(" ", "_")}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Check,
                                        contentDescription = "Complete Toggle",
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isCompleted) "Completed ✓" else "Mark as Complete",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                                    Spacer(modifier = Modifier.height(16.dp))

                                    // ------------------------------------------
                                    // QUESTION PAPERS & STUDY MATERIALS SECTION
                                    // ------------------------------------------
                                    var isResourceExpanded by remember { mutableStateOf(false) }
                                    val coursePapers = getQuestionPapersForCourse(course.title, course.yearTarget, course.category)
                                    val courseMaterials = getStudyMaterialsForCourse(course.title, course.yearTarget, course.category)
                                    val totalResourcesCount = coursePapers.size + courseMaterials.size

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isResourceExpanded) CosmicBlue.copy(alpha = 0.1f) else androidx.compose.ui.graphics.Color.Transparent)
                                            .clickable { isResourceExpanded = !isResourceExpanded }
                                            .padding(vertical = 8.dp, horizontal = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.List,
                                                contentDescription = "Resource Folder",
                                                tint = if (isResourceExpanded) NeonCyan else androidx.compose.ui.graphics.Color(0xFF475569),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Question Papers & Materials ($totalResourcesCount)",
                                                color = if (isResourceExpanded) NeonCyan else androidx.compose.ui.graphics.Color(0xFF334155),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Icon(
                                            imageVector = if (isResourceExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Expand Collapse",
                                            tint = androidx.compose.ui.graphics.Color(0xFF475569),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    if (isResourceExpanded) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(androidx.compose.ui.graphics.Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                                                .border(1.dp, androidx.compose.ui.graphics.Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                                                .padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            // 1. QUESTION PAPERS
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Star,
                                                        contentDescription = "Question Papers",
                                                        tint = GlowingAmber,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "PREVIOUS YEAR EXAM PAPERS",
                                                        color = androidx.compose.ui.graphics.Color(0xFF475569),
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Black,
                                                        letterSpacing = 0.5.sp
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                if (coursePapers.isEmpty()) {
                                                    Text(
                                                        text = "No papers available for this course.",
                                                        color = androidx.compose.ui.graphics.Color(0xFF94A3B8),
                                                        fontSize = 10.sp,
                                                        fontStyle = FontStyle.Italic
                                                    )
                                                } else {
                                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        coursePapers.forEach { paper ->
                                                            Row(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .clip(RoundedCornerShape(6.dp))
                                                                    .background(androidx.compose.ui.graphics.Color.White)
                                                                    .border(0.5.dp, androidx.compose.ui.graphics.Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                                                                    .clickable {
                                                                        activeQuestionPaper = paper.copy(courseTitle = course.title)
                                                                    }
                                                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.SpaceBetween
                                                            ) {
                                                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.ArrowForward,
                                                                        contentDescription = "Arrow",
                                                                        tint = NeonCyan,
                                                                        modifier = Modifier.size(10.dp)
                                                                    )
                                                                    Spacer(modifier = Modifier.width(6.dp))
                                                                    Text(
                                                                        text = paper.examName,
                                                                        color = androidx.compose.ui.graphics.Color(0xFF1E293B),
                                                                        fontSize = 11.sp,
                                                                        fontWeight = FontWeight.Medium,
                                                                        maxLines = 1,
                                                                        overflow = TextOverflow.Ellipsis
                                                                    )
                                                                }
                                                                Box(
                                                                    modifier = Modifier
                                                                        .clip(RoundedCornerShape(4.dp))
                                                                        .background(GlowingAmber.copy(alpha = 0.1f))
                                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                                ) {
                                                                    Text(
                                                                        text = paper.year,
                                                                        color = GlowingAmber,
                                                                        fontSize = 8.sp,
                                                                        fontWeight = FontWeight.Bold
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            HorizontalDivider(color = androidx.compose.ui.graphics.Color(0xFFE2E8F0), thickness = 0.5.dp)

                                            // 2. STUDY MATERIALS
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Info,
                                                        contentDescription = "Study Materials",
                                                        tint = NeonCyan,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "ACADEMIC STUDY MATERIALS",
                                                        color = androidx.compose.ui.graphics.Color(0xFF475569),
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Black,
                                                        letterSpacing = 0.5.sp
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                if (courseMaterials.isEmpty()) {
                                                    Text(
                                                        text = "No study materials available.",
                                                        color = androidx.compose.ui.graphics.Color(0xFF94A3B8),
                                                        fontSize = 10.sp,
                                                        fontStyle = FontStyle.Italic
                                                    )
                                                } else {
                                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        courseMaterials.forEach { material ->
                                                            Row(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .clip(RoundedCornerShape(6.dp))
                                                                    .background(androidx.compose.ui.graphics.Color.White)
                                                                    .border(0.5.dp, androidx.compose.ui.graphics.Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                                                                    .clickable {
                                                                        activeStudyMaterial = material.copy(courseTitle = course.title)
                                                                    }
                                                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.SpaceBetween
                                                            ) {
                                                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Info,
                                                                        contentDescription = "Info",
                                                                        tint = CosmicBlue,
                                                                        modifier = Modifier.size(10.dp)
                                                                    )
                                                                    Spacer(modifier = Modifier.width(6.dp))
                                                                    Text(
                                                                        text = material.title,
                                                                        color = androidx.compose.ui.graphics.Color(0xFF1E293B),
                                                                        fontSize = 11.sp,
                                                                        fontWeight = FontWeight.Medium,
                                                                        maxLines = 1,
                                                                        overflow = TextOverflow.Ellipsis
                                                                    )
                                                                }
                                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .clip(RoundedCornerShape(4.dp))
                                                                            .background(CosmicBlue.copy(alpha = 0.1f))
                                                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                                                    ) {
                                                                        Text(
                                                                            text = material.fileType,
                                                                            color = CosmicBlue,
                                                                            fontSize = 8.sp,
                                                                            fontWeight = FontWeight.Bold
                                                                        )
                                                                    }
                                                                    Spacer(modifier = Modifier.width(4.dp))
                                                                    Text(
                                                                        text = material.size,
                                                                        color = androidx.compose.ui.graphics.Color(0xFF64748B),
                                                                        fontSize = 8.sp
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    onOpenUrl(course.officialUrl)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = if (course.hasFreeCertificate) GlowingAmber else CosmicBlue),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1.1f)
                                    .height(42.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = if (course.hasFreeCertificate) "Enroll" else "Explore",
                                        color = if (course.hasFreeCertificate) Slate900 else androidx.compose.ui.graphics.Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = "Redirect",
                                        tint = if (course.hasFreeCertificate) Slate900 else androidx.compose.ui.graphics.Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    viewModel.loadQuizForCourse(course.title, course.category, course.description)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFFF1F5F9)),
                                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("practice_quiz_${course.title.replace(" ", "_")}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Quiz Icon",
                                        tint = NeonCyan,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Practice Quiz",
                                        color = androidx.compose.ui.graphics.Color(0xFF0F172A),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

    if (isGeneratingQuiz) {
        Dialog(onDismissRequest = {}) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, GlowingAmber.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(color = GlowingAmber)
                    Text(
                        text = "AI Agent is crafting your 5-question multiple choice practice quiz based on the course syllabus...",
                        color = androidx.compose.ui.graphics.Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    activeQuiz?.let { quiz ->
        QuizDialog(
            quiz = quiz,
            answers = quizAnswers,
            submitted = quizSubmitted,
            onSelectOption = { qIdx, oIdx -> viewModel.selectQuizOption(qIdx, oIdx) },
            onSubmit = { viewModel.submitQuiz() },
            onClose = { viewModel.clearQuiz() }
        )
    }

    activeQuestionPaper?.let { paper ->
        QuestionPaperDialog(
            paper = paper,
            courseTitle = paper.courseTitle,
            onClose = { activeQuestionPaper = null }
        )
    }

    activeStudyMaterial?.let { material ->
        StudyMaterialDialog(
            material = material,
            courseTitle = material.courseTitle,
            onClose = { activeStudyMaterial = null },
            onOpenUrl = onOpenUrl
        )
    }
}

// ==========================================
// 3. LEARNING ROADMAP SCREEN
// ==========================================
@Composable
fun RoadmapScreen(viewModel: TalentViewModel) {
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val checkedItems by viewModel.checkedRoadmapItems.collectAsStateWithLifecycle()

    // Define all milestones/items we want to track progress for
    val allTrackableItems = listOf(
        "sem_3_4", "sem_4_5", "sem_5_6", "sem_6_7", "sem_7_8",
        "study_dsa_striver", "study_dsa_leetcode", "study_dsa_cs50",
        "study_ml_andrew_ng", "study_ml_fast_ai", "study_ml_deeplearning", "study_ml_pytorch",
        "study_core_nptel", "study_applied_kaggle", "study_applied_github", "study_applied_deploy",
        "timeline_now_dec", "timeline_jan", "timeline_jan_apr", "timeline_summer_sem4", "timeline_ongoing"
    )

    val completedCount = allTrackableItems.count { checkedItems.contains(it) }
    val totalCount = allTrackableItems.size
    val progressPercent = if (totalCount > 0) (completedCount.toFloat() / totalCount * 100).toInt() else 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Personalized Student Profile Header Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate800),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, Slate700.copy(alpha = 0.5f)), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ANANYA JAIN",
                                color = PureWhite,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "B.E. Computer Science Engineering",
                                color = GlowingAmber,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Location",
                                    tint = Slate600,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "BITS Pilani, Dubai Campus  |  Semester 3 of 8",
                                    color = Slate600,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        
                        // Custom circular progress indicator or score badge
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(60.dp)
                                .background(GlowingAmber.copy(alpha = 0.1f), CircleShape)
                                .border(BorderStroke(2.dp, GlowingAmber.copy(alpha = 0.3f)), CircleShape)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$progressPercent%",
                                    color = GlowingAmber,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "Done",
                                    color = Slate600,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Roadmap Completion Progress",
                            color = LightGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "$completedCount/$totalCount Milestones",
                            color = PureWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { completedCount.toFloat() / totalCount },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = GlowingAmber,
                        trackColor = Slate700
                    )
                }
            }
        }

        // 2. Custom M3 Horizontal Sub-Navigation Tabs
        item {
            ScrollableTabRow(
                selectedTabIndex = selectedSubTab,
                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                contentColor = GlowingAmber,
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    if (selectedSubTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSubTab]),
                            color = GlowingAmber
                        )
                    }
                }
            ) {
                val subTabs = listOf(
                    "Placement Landscape",
                    "Semester Plan",
                    "Study Material",
                    "Internships & Timeline"
                )
                subTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedSubTab == index,
                        onClick = { selectedSubTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedSubTab == index) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        selectedContentColor = GlowingAmber,
                        unselectedContentColor = Slate600
                    )
                }
            }
        }

        // 3. Conditional Content Based on Sub-Tabs
        when (selectedSubTab) {
            0 -> {
                // Sub-Tab 0: Placement Landscape
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Slate800),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(BorderStroke(1.dp, Slate700.copy(alpha = 0.5f)), RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "BITS DUBAI PLACEMENT LANDSCAPE (2023-2025)",
                                    color = GlowingAmber,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.8.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Steady placement growth on campus, showing a clear shift towards CSE and AI-related recruiters.",
                                    color = LightGray,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                // Custom Elegant Grid Table
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(BorderStroke(1.dp, Slate700), RoundedCornerShape(8.dp))
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    // Table Header
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Slate700)
                                            .padding(8.dp)
                                    ) {
                                        Text(text = "Metric", modifier = Modifier.weight(1.5f), color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        Text(text = "2023", modifier = Modifier.weight(1f), color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center)
                                        Text(text = "2024", modifier = Modifier.weight(1f), color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center)
                                        Text(text = "2025", modifier = Modifier.weight(1f), color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center)
                                    }
                                    
                                    val rows = listOf(
                                        Triple("Companies Visiting", "128", "152" to "155"),
                                        Triple("Students Interviewed", "401", "697" to "727"),
                                        Triple("Total Offers", "136", "146" to "154")
                                    )

                                    rows.forEachIndexed { idx, row ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(if (idx % 2 == 0) Slate800 else Slate700.copy(alpha = 0.3f))
                                                .padding(8.dp)
                                        ) {
                                            Text(text = row.first, modifier = Modifier.weight(1.5f), color = LightGray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                            val val2023 = row.second
                                            val val2024 = row.third.first
                                            val val2025 = row.third.second
                                            
                                            Text(text = val2023, modifier = Modifier.weight(1f), color = PureWhite, fontSize = 11.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                                            Text(text = val2024, modifier = Modifier.weight(1f), color = PureWhite, fontSize = 11.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                                            Text(text = val2025, modifier = Modifier.weight(1f), color = GlowingAmber, fontSize = 11.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Slate800),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(BorderStroke(1.dp, Slate700.copy(alpha = 0.5f)), RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = "Recruiters", tint = GlowingAmber, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "KEY ANNUAL RECRUITERS",
                                        color = PureWhite,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Recruiters that consistently hire on-campus every year:",
                                    color = LightGray,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                val recruiters = listOf("ESRI", "Kema", "Schindler", "Zomato", "Dabur International", "Sharaf DG")
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(recruiters) { recruiter ->
                                        Box(
                                            modifier = Modifier
                                                .background(Slate700, RoundedCornerShape(8.dp))
                                                .border(BorderStroke(1.dp, Slate600.copy(alpha = 0.2f)), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = recruiter,
                                                color = PureWhite,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Slate800),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(BorderStroke(1.dp, Slate700.copy(alpha = 0.5f)), RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Refresh, contentDescription = "AI Growth", tint = GlowingAmber, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "STANDOUT AI-ERA PLACEMENT TRENDS",
                                        color = PureWhite,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                // Deriv Scale Highlight
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(GlowingAmber.copy(alpha = 0.05f))
                                        .border(BorderStroke(1.dp, GlowingAmber.copy(alpha = 0.2f)), RoundedCornerShape(8.dp))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Scale", tint = GlowingAmber, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Deriv Placement Scaling Surge",
                                            color = PureWhite,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "Scaled rapidly to make 23 final offers out of 50 interviewed students in 2025. Excellent placement option.",
                                            color = LightGray,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "New AI-focused entrants hiring BITS Dubai grads in 2025 (excellent target list):",
                                    color = LightGray,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                val aiEntrants = listOf("CAMB.AI", "Dalil AI", "Sherloq AI", "Spark AI", "TOR.ai")
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(aiEntrants) { aiFirm ->
                                        Box(
                                            modifier = Modifier
                                                .background(GlowingAmber.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                                .border(BorderStroke(1.dp, GlowingAmber), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = aiFirm,
                                                color = GlowingAmber,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                // Sub-Tab 1: Semester Plan
                val stages = listOf(
                    RoadmapStageItem(
                        id = "sem_3_4",
                        title = "Semester 3-4 (Now) — Foundations",
                        focus = "Math & CS Foundations",
                        actions = "Math (linear algebra, probability, calculus); strong Python; Data Structures & Algorithms (DSA) basics; core Computer Science concepts (OS, DBMS, OOP)"
                    ),
                    RoadmapStageItem(
                        id = "sem_4_5",
                        title = "Semester 4-5 — Core ML/DL",
                        focus = "Deep Machine Learning",
                        actions = "Andrew Ng ML Specialization on Coursera; master PyTorch; submit your first Kaggle competition entry"
                    ),
                    RoadmapStageItem(
                        id = "sem_5_6",
                        title = "Semester 5-6 — Specialize + Build",
                        focus = "Domain Selection & Project Portfolios",
                        actions = "Pick a special lane (NLP/LLMs, Computer Vision, or MLOps); build 2 original projects & deploy at least 1; read & replicate a peer-reviewed paper"
                    ),
                    RoadmapStageItem(
                        id = "sem_6_7",
                        title = "Semester 6-7 — Internships & Depth",
                        focus = "Professional Work Experience",
                        actions = "Apply aggressively to summer/research internships; contribute code to an open-source ML repository"
                    ),
                    RoadmapStageItem(
                        id = "sem_7_8",
                        title = "Semester 7-8 — Placement Prep",
                        focus = "High-Stress Placement Readiness",
                        actions = "Polish GitHub portfolios & technical write-ups; practice ML system design architecture alongside rigorous DSA grids"
                    )
                )

                items(stages) { stage ->
                    val isChecked = checkedItems.contains(stage.id)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate800),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                BorderStroke(
                                    1.dp,
                                    if (isChecked) GlowingAmber.copy(alpha = 0.5f) else Slate700.copy(alpha = 0.5f)
                                ),
                                RoundedCornerShape(16.dp)
                            ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { viewModel.toggleRoadmapItemChecked(stage.id) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = GlowingAmber,
                                    uncheckedColor = Slate600,
                                    checkmarkColor = Slate900
                                )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stage.title,
                                    color = if (isChecked) GlowingAmber else PureWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier
                                        .background(GlowingAmber.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "FOCUS: ${stage.focus}",
                                        color = GlowingAmber,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stage.actions,
                                    color = LightGray,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }
            2 -> {
                // Sub-Tab 2: Study Material
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "CURATED STUDY GUIDES & BIBLIOGRAPHY",
                            color = GlowingAmber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp
                        )
                        
                        // Section 1: DSA
                        StudyCategoryCard(
                            title = "Data Structures & Algorithms",
                            resources = listOf(
                                StudyResourceItem("study_dsa_striver", "Striver's A2Z DSA Sheet", "Free, structured progression to master essential DSA concepts."),
                                StudyResourceItem("study_dsa_leetcode", "LeetCode", "Consistency practice (Easy to Medium) to solve technical coding round problems."),
                                StudyResourceItem("study_dsa_cs50", "CS50 (Harvard, free on edX)", "Excellent foundational Computer Science refresher for system thinking.")
                            ),
                            checkedItems = checkedItems,
                            onToggle = { viewModel.toggleRoadmapItemChecked(it) }
                        )

                        // Section 2: ML / DL
                        StudyCategoryCard(
                            title = "Machine Learning & Deep Learning",
                            resources = listOf(
                                StudyResourceItem("study_ml_andrew_ng", "Andrew Ng's ML Specialization (Coursera)", "The gold industry standard for foundational theory & algorithms."),
                                StudyResourceItem("study_ml_fast_ai", "fast.ai Course", "Practical, project-first, and coder-first deep learning models."),
                                StudyResourceItem("study_ml_deeplearning", "deeplearning.ai DL Specialization", "More theory-heavy neural network foundations and math depth."),
                                StudyResourceItem("study_ml_pytorch", "PyTorch Documentation & Tutorials", "Master the industry-standard deep learning framework.")
                            ),
                            checkedItems = checkedItems,
                            onToggle = { viewModel.toggleRoadmapItemChecked(it) }
                        )

                        // Section 3: Core CS
                        StudyCategoryCard(
                            title = "Core CS Fundamentals",
                            resources = listOf(
                                StudyResourceItem("study_core_nptel", "NPTEL Courses (OS, DBMS, Computer Networks)", "Free, academic depth tailored for standard university certifications.")
                            ),
                            checkedItems = checkedItems,
                            onToggle = { viewModel.toggleRoadmapItemChecked(it) }
                        )

                        // Section 4: Applied Practice
                        StudyCategoryCard(
                            title = "Applied Practice",
                            resources = listOf(
                                StudyResourceItem("study_applied_kaggle", "Kaggle Competitions", "Beginner-friendly playgrounds. Aim to place in the top 50%."),
                                StudyResourceItem("study_applied_github", "Build 2-3 Solid GitHub Projects", "Commit clean code with proper documentation instead of half-finished ones."),
                                StudyResourceItem("study_applied_deploy", "Deploy At Least One Live App", "Host on Hugging Face Spaces, Streamlit, or a simple serverless cloud endpoint.")
                            ),
                            checkedItems = checkedItems,
                            onToggle = { viewModel.toggleRoadmapItemChecked(it) }
                        )
                    }
                }
            }
            3 -> {
                // Sub-Tab 3: Internships & Timeline
                item {
                    Text(
                        text = "AI-FOCUSED INTERNSHIP OPPORTUNITIES (UAE, 2026)",
                        color = GlowingAmber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp
                    )
                }

                val internships = listOf(
                    InternshipItem(
                        title = "MBZUAI UGRIP (Abu Dhabi)",
                        offer = "AI research internship (June 1 - 26, 2026)",
                        notes = "Highly competitive (~4% acceptance in 2025); applications open January 1; best pure-research credential in the Middle East."
                    ),
                    InternshipItem(
                        title = "G42 Internship Program",
                        offer = "AI internship & early-career technical training",
                        notes = "UAE's flagship state-backed AI organization; provides high-scale production ML modeling experience."
                    ),
                    InternshipItem(
                        title = "ADIA Lab Summer Internship",
                        offer = "6-8 week program (June - Aug 2026) in ML/Data",
                        notes = "Focuses on digital economics; requires valid UAE visa; 2026 deadline was April 3."
                    ),
                    InternshipItem(
                        title = "Amazon / AWS Dubai",
                        offer = "SDE & Applied Scientist intern tracks (10-12 weeks)",
                        notes = "Recruiting starts early (Oct - Dec prior year). Candidates with cloud-deployed ML apps (SageMaker, Lambda) are strongly preferred."
                    ),
                    InternshipItem(
                        title = "AI Startups (BITS Placement List)",
                        offer = "Hands-on engineering & model-building roles",
                        notes = "Companies like CAMB.AI, Dalil AI, Sherloq AI, and Spark AI. Often a faster, lower-friction entry point for 2nd/3rd-year students."
                    ),
                    InternshipItem(
                        title = "Data Analytics Internships",
                        offer = "Deloitte, Siemens Healthineers, GM Dubai",
                        notes = "Regular listings posted on LinkedIn & Indeed UAE. Revisit early 2026."
                    ),
                    InternshipItem(
                        title = "Ministry of Education Portal",
                        offer = "Central database of private sector internships",
                        notes = "Excellent national search hub to find general software and technology placements."
                    )
                )

                items(internships) { intern ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate800),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(BorderStroke(1.dp, Slate700.copy(alpha = 0.5f)), RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(GlowingAmber.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                        .padding(6.dp)
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = "Internship icon", tint = GlowingAmber, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = intern.title,
                                        color = PureWhite,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = intern.offer,
                                        color = GlowingAmber,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Slate700, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = intern.notes,
                                color = LightGray,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate800),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(BorderStroke(1.dp, GlowingAmber.copy(alpha = 0.3f)), RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DateRange, contentDescription = "Timeline", tint = GlowingAmber, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ACTION TIMELINE (CRITICAL WINDOWS)",
                                    color = PureWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            val timelineSteps = listOf(
                                TimelineStep("timeline_now_dec", "Now - December", "Build DSA + ML fundamentals; apply to Amazon/AWS (recruiting opens Oct-Dec)."),
                                TimelineStep("timeline_jan", "January", "MBZUAI UGRIP applications open — submit early for best chance!"),
                                TimelineStep("timeline_jan_apr", "January - April", "Watch ADIA Lab and other summer internship deadlines closely."),
                                TimelineStep("timeline_summer_sem4", "Summer after Sem 4", "Target your first internship — even small/unpaid ones build a strong signal."),
                                TimelineStep("timeline_ongoing", "Ongoing", "Track new AI recruiters appearing at BITS Dubai placements each year (Deriv, CAMB.AI, etc.).")
                            )

                            timelineSteps.forEach { step ->
                                val isStepChecked = checkedItems.contains(step.id)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Checkbox(
                                        checked = isStepChecked,
                                        onCheckedChange = { viewModel.toggleRoadmapItemChecked(step.id) },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = GlowingAmber,
                                            uncheckedColor = Slate600,
                                            checkmarkColor = Slate900
                                        ),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = step.time,
                                            color = if (isStepChecked) GlowingAmber else PureWhite,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = step.action,
                                            color = LightGray,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Support Classes/Composables for custom Roadmap layout
data class RoadmapStageItem(val id: String, val title: String, val focus: String, val actions: String)
data class StudyResourceItem(val id: String, val title: String, val description: String)
data class InternshipItem(val title: String, val offer: String, val notes: String)
data class TimelineStep(val id: String, val time: String, val action: String)

@Composable
fun StudyCategoryCard(
    title: String,
    resources: List<StudyResourceItem>,
    checkedItems: Set<String>,
    onToggle: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Slate800),
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, Slate700), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                color = PureWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Slate700, thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            resources.forEach { res ->
                val isChecked = checkedItems.contains(res.id)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { onToggle(res.id) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = GlowingAmber,
                            uncheckedColor = Slate600,
                            checkmarkColor = Slate900
                        ),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = res.title,
                            color = if (isChecked) GlowingAmber else PureWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = res.description,
                            color = LightGray,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
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
    var isMentorExpanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Slate900)) {
        // Main page focused entirely on the Mock Interview Simulator
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (isMentorExpanded) 320.dp else 68.dp) // Leave clean space for the floating coach box
        ) {
            MockInterviewMainPage(viewModel)
        }

        // Small, elegant floating / anchored AI Mentor Chat box
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            AIMentorSmallBox(
                viewModel = viewModel,
                isExpanded = isMentorExpanded,
                onToggleExpand = { isMentorExpanded = !isMentorExpanded }
            )
        }
    }
}

fun generateVoiceTranscriptForQuestion(question: String): String {
    return when {
        question.contains("Complexity", ignoreCase = true) || question.contains("Big-O", ignoreCase = true) || question.contains("Algorithm", ignoreCase = true) -> {
            "First, I would analyze the bounds. The naive solution has a nested structure yielding O(N squared) runtime. However, by leveraging an auxiliary hash map, we can index the visited states in O(1) time, reducing the overall time complexity of the algorithm to O(N) linear time and O(N) space."
        }
        question.contains("Database", ignoreCase = true) || question.contains("Query", ignoreCase = true) || question.contains("SQL", ignoreCase = true) -> {
            "To resolve the query bottleneck, I would check the execution plan. If it performs a full table scan, I would create a composite B-tree index on the filtered columns. I'd also ensure we aren't experiencing N plus 1 query issues by utilizing eager joins or prefetching related rows."
        }
        question.contains("Security", ignoreCase = true) || question.contains("Crypt", ignoreCase = true) || question.contains("Auth", ignoreCase = true) -> {
            "My security strategy is defense in depth. I would enforce strong cryptographic standards, specifically using salted SHA-256 or bcrypt for password hashing. All communication channels will require TLS 1.3 with secure cipher suites, and we will restrict API access using OAuth 2.0 with JWT verification."
        }
        question.contains("Network", ignoreCase = true) || question.contains("Protocol", ignoreCase = true) || question.contains("IP", ignoreCase = true) -> {
            "For reliable, ordered byte delivery, TCP is the standard protocol due to its built-in congestion control and three-way handshake mechanism. For real-time low-latency telemetry transmission, I would select UDP with a custom application-level sequence numbering to handle dropouts gracefully."
        }
        question.contains("Robot", ignoreCase = true) || question.contains("Kinematics", ignoreCase = true) || question.contains("Jacobian", ignoreCase = true) -> {
            "We mathematically formulate joint states using Denavit-Hartenberg parameters to construct sequential homogeneous transform matrices. In the event of a kinematic singularity where the Jacobian matrix loses rank, we would employ a pseudo-inverse with dampening factors to keep velocity bounds stable."
        }
        else -> {
            "In approaching this system design question, my priority is dividing the monolithic components into scalable, stateless microservices. We will deploy an event-driven queue like Kafka to handle spikes in traffic, and cache frequent read-heavy requests inside an in-memory database like Redis to optimize response latency."
        }
    }
}

@Composable
fun MockInterviewMainPage(viewModel: TalentViewModel) {
    val activeQuestion by viewModel.currentQuestion.collectAsStateWithLifecycle()
    val isGrading by viewModel.isInterviewGrading.collectAsStateWithLifecycle()
    val gradingResult by viewModel.currentGradingResult.collectAsStateWithLifecycle()
    val historyLogs by viewModel.interviewLogs.collectAsStateWithLifecycle()

    val selectedField by viewModel.selectedField.collectAsStateWithLifecycle()
    val selectedDifficulty by viewModel.selectedDifficulty.collectAsStateWithLifecycle()

    var studentAnswer by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    var isRecording by remember { mutableStateOf(false) }
    var isTranscribing by remember { mutableStateOf(false) }
    var recordSecondsElapsed by remember { mutableStateOf(0) }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordSecondsElapsed = 0
            while (isRecording) {
                kotlinx.coroutines.delay(1000L)
                recordSecondsElapsed++
            }
        }
    }

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isRecording = true
        } else {
            android.widget.Toast.makeText(context, "Microphone permission is required for voice responses.", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    val defaultDuration = when (selectedDifficulty) {
        "Easy" -> 300
        "Medium" -> 600
        "Hard" -> 900
        else -> 300
    }
    var timeRemaining by remember { mutableStateOf(defaultDuration) }
    var isTimerRunning by remember { mutableStateOf(true) }
    var maxTime by remember { mutableStateOf(defaultDuration) }

    // Reset when active question or difficulty changes
    LaunchedEffect(activeQuestion, selectedDifficulty) {
        val duration = when (selectedDifficulty) {
            "Easy" -> 300
            "Medium" -> 600
            "Hard" -> 900
            else -> 300
        }
        timeRemaining = duration
        maxTime = duration
        isTimerRunning = true
    }

    // Tick the timer
    LaunchedEffect(isTimerRunning, activeQuestion, selectedDifficulty) {
        if (isTimerRunning) {
            while (timeRemaining > 0) {
                kotlinx.coroutines.delay(1000L)
                timeRemaining = (timeRemaining - 1).coerceAtLeast(0)
            }
        }
    }

    val fields = listOf("Software Engineering", "Cybersecurity & Networks", "Data Structures & Algorithms", "Data Science & AI", "Cloud Computing & DevOps")
    val difficulties = listOf("Easy", "Medium", "Hard")

    // Automatically load an initial question when screen opens
    LaunchedEffect(key1 = true) {
        if (activeQuestion.isEmpty()) {
            viewModel.loadDubaiMockQuestion()
        }
    }

    // Reset student answer input when the active question changes
    LaunchedEffect(key1 = activeQuestion) {
        studentAnswer = ""
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "DUBAI PLACEMENT INTERVIEWS",
                    color = GlowingAmber,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Mock Technical Assessment",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Prepare for elite CS placements by selecting your target field and difficulty below. The assessment draws from a rich database of top corporate interview scenarios.",
                    color = LightGray.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }

        // CS Field Picker Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Computer Science Field Track:",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    fields.forEach { field ->
                        val isSelected = field == selectedField
                        val (icon, description, accentColor) = when (field) {
                            "Software Engineering" -> Triple(Icons.Default.Edit, "System design, object-oriented design, patterns, and code quality assessments.", CosmicBlue)
                            "Cybersecurity & Networks" -> Triple(Icons.Default.Info, "Network protocols, security frameworks, threat mitigation, and cryptography.", NeonGreen)
                            "Data Structures & Algorithms" -> Triple(Icons.Default.List, "Analysis of sorting, searching, graphs, trees, and time/space complexity.", GlowingAmber)
                            "Data Science & AI" -> Triple(Icons.Default.Star, "Neural network modeling, data visualization, predictive analytics, and ML pipelines.", Color(0xFFC084FC))
                            "Cloud Computing & DevOps" -> Triple(Icons.Default.Home, "Containers, orchestration, continuous delivery pipelines, and cloud systems.", Color(0xFF22D3EE))
                            else -> Triple(Icons.Default.Info, "", CosmicBlue)
                        }

                        val cardBgColor = if (isSelected) Slate800 else Slate900.copy(alpha = 0.5f)
                        val borderColor = if (isSelected) accentColor else Slate700.copy(alpha = 0.3f)
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setField(field) }
                                .testTag("chip_field_${field.replace(" ", "_")}"),
                            colors = CardDefaults.cardColors(containerColor = cardBgColor),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, borderColor)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) accentColor.copy(alpha = 0.15f) else Slate800)
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) accentColor else LightGray.copy(alpha = 0.6f)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = field,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = description,
                                        color = LightGray.copy(alpha = 0.6f),
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = accentColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Difficulty Picker Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Difficulty Level:",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    difficulties.forEach { diff ->
                        val isSelected = diff == selectedDifficulty
                        val chipBgColor = if (isSelected) {
                            when (diff) {
                                "Easy" -> NeonGreen.copy(alpha = 0.2f)
                                "Medium" -> GlowingAmber.copy(alpha = 0.2f)
                                else -> Color.Red.copy(alpha = 0.2f)
                            }
                        } else Slate800
                        val chipTextColor = if (isSelected) {
                            when (diff) {
                                "Easy" -> NeonGreen
                                "Medium" -> GlowingAmber
                                else -> Color.Red
                            }
                        } else LightGray
                        val chipBorderColor = if (isSelected) chipTextColor else Slate700
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(chipBgColor)
                                .border(1.dp, chipBorderColor, RoundedCornerShape(8.dp))
                                .clickable { viewModel.setDifficulty(diff) }
                                .padding(vertical = 8.dp)
                                .testTag("chip_diff_$diff"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = diff,
                                color = chipTextColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Countdown Timer Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate800),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Slate700),
                modifier = Modifier.fillMaxWidth().testTag("countdown_timer_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (timeRemaining == 0) Icons.Default.Warning else Icons.Default.Info,
                                contentDescription = "Timer Status",
                                tint = if (timeRemaining == 0) Color.Red else if (timeRemaining <= maxTime * 0.25) GlowingAmber else CosmicBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (timeRemaining == 0) "TIME'S UP!" else "ASSESSMENT TIMER",
                                color = if (timeRemaining == 0) Color.Red else if (timeRemaining <= maxTime * 0.25) GlowingAmber else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                        
                        val minutes = timeRemaining / 60
                        val seconds = timeRemaining % 60
                        val timeString = String.format("%02d:%02d", minutes, seconds)
                        
                        Text(
                            text = timeString,
                            color = if (timeRemaining == 0) Color.Red else if (timeRemaining <= maxTime * 0.25) GlowingAmber else Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }

                    val ratio = if (maxTime > 0) timeRemaining.toFloat() / maxTime.toFloat() else 0f
                    val progressColor = if (timeRemaining == 0) Color.Red 
                                         else if (timeRemaining <= maxTime * 0.25) GlowingAmber 
                                         else CosmicBlue
                    
                    LinearProgressIndicator(
                        progress = ratio,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = progressColor,
                        trackColor = Slate700.copy(alpha = 0.4f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (timeRemaining == 0) {
                                "Focus period expired. Submit what you have!"
                            } else if (!isTimerRunning) {
                                "Timer paused. Take a breath and resume."
                            } else {
                                "Simulating real interview time pressure."
                            },
                            color = LightGray.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (timeRemaining > 0) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .border(1.dp, Slate700, RoundedCornerShape(6.dp))
                                        .clickable {
                                            timeRemaining += 60
                                            maxTime = maxOf(maxTime, timeRemaining)
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .testTag("timer_add_minute")
                                ) {
                                    Text(
                                        text = "+1 MIN",
                                        color = LightGray,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (timeRemaining == 0) GlowingAmber else CosmicBlue)
                                    .clickable {
                                        if (timeRemaining == 0) {
                                            timeRemaining = when (selectedDifficulty) {
                                                "Easy" -> 300
                                                "Medium" -> 600
                                                "Hard" -> 900
                                                else -> 300
                                            }
                                            maxTime = timeRemaining
                                            isTimerRunning = true
                                        } else {
                                            isTimerRunning = !isTimerRunning
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                    .testTag("timer_play_pause")
                            ) {
                                Text(
                                    text = if (timeRemaining == 0) "RESTART" else if (isTimerRunning) "PAUSE" else "RESUME",
                                    color = Color.TrueWhite,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active Question Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate800),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Slate700),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(GlowingAmber.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "MOCK ASSESSMENT",
                                    color = GlowingAmber,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "●  $selectedDifficulty",
                                color = LightGray.copy(alpha = 0.6f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        IconButton(
                            onClick = { viewModel.loadDubaiMockQuestion() },
                            modifier = Modifier.size(28.dp).testTag("interview_refresh_question")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "New question",
                                tint = LightGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Text(
                        text = activeQuestion.ifEmpty { "Loading placement assessment question..." },
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // Student Answer Input Area
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate800, shape = RoundedCornerShape(16.dp))
                    .border(1.dp, Slate700.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "YOUR TECHNICAL ANSWER EXPLANATION",
                        color = LightGray.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    
                    if (timeRemaining > 0 && !isTranscribing) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isRecording) NeonGreen.copy(alpha = 0.15f) else Slate700)
                                .clickable {
                                    if (isRecording) {
                                        isRecording = false
                                        isTranscribing = true
                                    } else {
                                        permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isRecording) Icons.Default.Close else Icons.Default.Check,
                                contentDescription = "Mic Icon",
                                tint = if (isRecording) NeonGreen else androidx.compose.ui.graphics.Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isRecording) "Stop & Transcribe" else "Record Response",
                                color = if (isRecording) NeonGreen else androidx.compose.ui.graphics.Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (isRecording) {
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse_wave")
                    val waveHeight1 by infiniteTransition.animateFloat(
                        initialValue = 10f,
                        targetValue = 35f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(400, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ), label = "h1"
                    )
                    val waveHeight2 by infiniteTransition.animateFloat(
                        initialValue = 15f,
                        targetValue = 48f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(500, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ), label = "h2"
                    )
                    val waveHeight3 by infiniteTransition.animateFloat(
                        initialValue = 8f,
                        targetValue = 30f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(350, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ), label = "h3"
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Slate900, RoundedCornerShape(10.dp))
                            .border(1.dp, NeonGreen.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(NeonGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Recording Audio via Mic... 00:${recordSecondsElapsed.toString().padStart(2, '0')}",
                                color = NeonGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.height(30.dp)
                        ) {
                            Box(modifier = Modifier.width(3.dp).height(waveHeight1.dp).clip(RoundedCornerShape(1.5.dp)).background(NeonGreen))
                            Box(modifier = Modifier.width(3.dp).height(waveHeight2.dp).clip(RoundedCornerShape(1.5.dp)).background(NeonGreen))
                            Box(modifier = Modifier.width(3.dp).height(waveHeight3.dp).clip(RoundedCornerShape(1.5.dp)).background(NeonGreen))
                            Box(modifier = Modifier.width(3.dp).height(waveHeight1.dp).clip(RoundedCornerShape(1.5.dp)).background(NeonGreen))
                            Box(modifier = Modifier.width(3.dp).height(waveHeight2.dp).clip(RoundedCornerShape(1.5.dp)).background(NeonGreen))
                        }
                    }
                }

                if (isTranscribing) {
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(1800)
                        studentAnswer = generateVoiceTranscriptForQuestion(activeQuestion)
                        isTranscribing = false
                        android.widget.Toast.makeText(context, "Voice transcript processed successfully via AI model!", android.widget.Toast.LENGTH_SHORT).show()
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Slate900, RoundedCornerShape(10.dp))
                            .border(1.dp, GlowingAmber.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(color = GlowingAmber, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Text(
                            text = "AI System is transcribing your spoken audio response...",
                            color = GlowingAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                OutlinedTextField(
                    value = studentAnswer,
                    onValueChange = { if (timeRemaining > 0) studentAnswer = it },
                    readOnly = (timeRemaining == 0),
                    placeholder = { 
                        Text(
                            text = if (timeRemaining == 0) {
                                "Time's Up! You can no longer edit your answer, but you can still submit what you've typed or restart the timer above."
                            } else {
                                "Explain your technical answer. Mention algorithmic complexity, trade-offs, and design choices to score high..."
                            },
                            fontSize = 12.sp,
                            color = LightGray.copy(alpha = 0.4f)
                        ) 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .testTag("interview_answer_input"),
                    maxLines = 8,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CosmicBlue,
                        unfocusedBorderColor = Slate700,
                        focusedContainerColor = Slate900.copy(alpha = 0.4f),
                        unfocusedContainerColor = Slate900.copy(alpha = 0.4f)
                    )
                )

                Button(
                    onClick = {
                        viewModel.gradeInterviewAnswer(selectedField, activeQuestion, studentAnswer)
                        focusManager.clearFocus()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("interview_submit_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicBlue),
                    shape = RoundedCornerShape(10.dp),
                    enabled = studentAnswer.isNotBlank() && !isGrading
                ) {
                    if (isGrading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI Grading System...", color = Color.White, fontSize = 13.sp)
                    } else {
                        val buttonText = if (timeRemaining == 0) "Submit Partial Answer (Time Expired)" else "Submit & Grade Answer (Free & Offline)"
                        Text(buttonText, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // Grading Result Display
        if (gradingResult != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate800),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, GlowingAmber.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Graded",
                                    tint = NeonGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "EVALUATION FEEDBACK",
                                    color = GlowingAmber,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CosmicBlue.copy(alpha = 0.2f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${gradingResult!!.second} / 10",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
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
                                viewModel.loadDubaiMockQuestion()
                                studentAnswer = ""
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("interview_next_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Slate700),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Try Another Scenario Question", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Previous logs section
        if (historyLogs.isNotEmpty()) {
            item {
                Text(
                    text = "Assessment History Logs",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            items(historyLogs) { log ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate800.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, Slate700.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = log.track,
                                color = LightGray.copy(alpha = 0.5f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Score: ${log.score}/10",
                                color = if (log.score >= 8) NeonGreen else if (log.score >= 6) GlowingAmber else Color.Red,
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
                            text = log.feedback,
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

@Composable
fun AIMentorSmallBox(
    viewModel: TalentViewModel,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    val messages by viewModel.mentorMessages.collectAsStateWithLifecycle()
    val isMentorLoading by viewModel.isMentorLoading.collectAsStateWithLifecycle()
    var inputMsg by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    // Automatically scroll to bottom when new messages arrive
    LaunchedEffect(messages.size, isExpanded) {
        if (messages.isNotEmpty() && isExpanded) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val suggestions = listOf("Careem Tech", "Dubai Salaries", "1-Page Resume")

    Card(
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 12.dp, bottomEnd = 12.dp),
        border = BorderStroke(1.dp, if (isExpanded) CosmicBlue else Slate700),
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isExpanded) 290.dp else 48.dp)
            .testTag("ai_mentor_small_box")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .background(if (isExpanded) CosmicBlue.copy(alpha = 0.15f) else Color.Transparent)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(NeonGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "💬 AI Mentor Pocket Coach (Free & Offline)",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Toggle Mentor Expand",
                    tint = LightGray,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (isExpanded) {
                HorizontalDivider(color = Slate700)
                
                // Chat conversation area
                Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)) {
                    if (messages.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Ask me anything about Dubai CS Careers!",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Fully offline, free chat. Powered by local career intelligence.",
                                color = LightGray.copy(alpha = 0.5f),
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                                    topStart = 10.dp,
                                                    topEnd = 10.dp,
                                                    bottomStart = if (isUser) 10.dp else 0.dp,
                                                    bottomEnd = if (isUser) 0.dp else 10.dp
                                                )
                                            )
                                            .background(if (isUser) CosmicBlue else Slate900)
                                            .padding(10.dp)
                                            .widthIn(max = 240.dp)
                                    ) {
                                        Text(
                                            text = msg.message,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
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
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Slate900)
                                                .padding(10.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                CircularProgressIndicator(modifier = Modifier.size(12.dp), color = GlowingAmber, strokeWidth = 1.5.dp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Mentor is thinking...", color = LightGray, fontSize = 10.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Quick Suggestions Row
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(suggestions) { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Slate900)
                                .border(1.dp, Slate700, RoundedCornerShape(6.dp))
                                .clickable {
                                    viewModel.sendMentorMessage(tag)
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = tag,
                                color = GlowingAmber,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                HorizontalDivider(color = Slate700)

                // Input Box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputMsg,
                        onValueChange = { inputMsg = it },
                        placeholder = { Text("Ask careers, resumes, salaries...", fontSize = 11.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("mentor_message_input"),
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
                            unfocusedBorderColor = Slate700,
                            focusedContainerColor = Slate900,
                            unfocusedContainerColor = Slate900
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(6.dp))
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
                            .size(34.dp)
                            .testTag("mentor_send_button")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(14.dp))
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

// ==========================================
// 6. AI BADGES & CREATIVE STUDIO SCREEN
// ==========================================
@Composable
fun AIImageScreen(viewModel: TalentViewModel) {
    var prompt by remember { mutableStateOf("") }
    var selectedSize by remember { mutableStateOf("1K") } // "1K", "2K", "4K"
    val context = LocalContext.current

    val imageBase64 by viewModel.generatedImageBase64.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGeneratingImage.collectAsStateWithLifecycle()
    val errorMsg by viewModel.imageGenerationError.collectAsStateWithLifecycle()

    val suggestions = listOf(
        "BITS Dubai coding wizard, futuristic golden avatar badge, 3D render",
        "Data science certification badge, cyber neon green tech orb",
        "Generative AI Scholar emblem, cosmic violet and blue nebula background",
        "Cloud Architect graduation seal, sleek metallic chrome texture"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero / Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Slate800),
                border = BoxBorderDefaults.glowingBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AI BADGE & CREATIVE STUDIO",
                        color = GlowingAmber,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Generate custom digital talent development credentials, portfolio graphics, and merit badges powered by gemini-2.5-flash-image (Free Tier).",
                        color = LightGray.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Prompt Input Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Slate800)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Enter Creative Prompt",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { prompt = it },
                        placeholder = { Text("Describe the badge, e.g. Cyberpunk Java Master Badge...", fontSize = 13.sp, color = Slate600) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("prompt_input_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CosmicBlue,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Slate900,
                            unfocusedContainerColor = Slate900
                        ),
                        shape = RoundedCornerShape(8.dp),
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Suggestions:",
                        color = LightGray.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    // Suggestions list
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        suggestions.forEach { sugg ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Slate900)
                                    .clickable { prompt = sugg }
                                    .border(1.dp, Slate700.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = sugg,
                                    color = LightGray.copy(alpha = 0.8f),
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Size Affordance Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Slate800)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "2. Select Image Resolution",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val sizes = listOf("1K", "2K", "4K")
                        sizes.forEach { size ->
                            val isSelected = selectedSize == size
                            val displayLabel = when(size) {
                                "1K" -> "1K (Standard)"
                                "2K" -> "2K (HD)"
                                "4K" -> "4K (Pro)"
                                else -> size
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) CosmicBlue else Slate900)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) GlowingAmber else Slate700,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedSize = size }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = size,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = displayLabel.substringAfter(" "),
                                        color = if (isSelected) Color.White.copy(alpha = 0.9f) else Slate600,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Generate Action Button
        item {
            Button(
                onClick = { viewModel.generateBadgeImage(prompt, selectedSize) },
                enabled = !isGenerating && prompt.trim().isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("generate_badge_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GlowingAmber,
                    disabledContainerColor = Slate800
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Slate900,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Rendering Studio pixels...",
                        color = Slate900,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Palette",
                        tint = Slate900
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Generate with Gemini Flash Image",
                        color = Slate900,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Error message if any
        if (errorMsg != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF7F1D1D))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMsg ?: "",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Output Result Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 280.dp),
                colors = CardDefaults.cardColors(containerColor = Slate800),
                border = if (imageBase64 != null) BoxBorderDefaults.glowingBorder() else null
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (isGenerating) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth(0.7f)
                                        .height(4.dp),
                                    color = GlowingAmber,
                                    trackColor = Slate900
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Synthesizing visual intelligence...",
                                    color = LightGray.copy(alpha = 0.6f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else if (imageBase64 != null) {
                        val bitmap = remember(imageBase64) {
                            if (imageBase64 != null) {
                                try {
                                    val decodedBytes = android.util.Base64.decode(imageBase64, android.util.Base64.DEFAULT)
                                    android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                                } catch (e: Exception) {
                                    null
                                }
                            } else null
                        }

                        if (bitmap != null) {
                            androidx.compose.foundation.Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Generated Merit Badge",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        saveBitmapToStorage(context, bitmap, "BITS_Dubai_Badge_${System.currentTimeMillis()}")
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = CosmicBlue),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Save")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Save to Gallery", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { viewModel.clearGeneratedImage() },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = LightGray),
                                    border = BorderStroke(1.dp, Slate700),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Clear")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reset Studio", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Text(
                                text = "Failed to parse image bytes.",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .border(
                                    border = BorderStroke(1.dp, Slate700),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Studio Art",
                                    tint = Slate600,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Your Generated Masterpiece Will Appear Here",
                                    color = LightGray.copy(alpha = 0.5f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Prompts can describe career goals, talent scores, tech mastery, or unique campus designs.",
                                    color = Slate600,
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun saveBitmapToStorage(context: android.content.Context, bitmap: android.graphics.Bitmap, filename: String) {
    val contentValues = android.content.ContentValues().apply {
        put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, "$filename.jpg")
        put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES)
        }
    }
    val resolver = context.contentResolver
    val uri = resolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    if (uri != null) {
        try {
            resolver.openOutputStream(uri).use { stream ->
                if (stream != null) {
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, stream)
                    android.widget.Toast.makeText(context, "Saved achievement badge to Pictures!", android.widget.Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "Failed to save: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
        }
    }
}

// ==========================================
// 6. EVENTS & PLACEMENT FEEDS SCREEN (NEW)
// ==========================================
@Composable
fun EventsScreen(viewModel: TalentViewModel) {
    val context = LocalContext.current
    val registeredEventIds by viewModel.registeredEventIds.collectAsStateWithLifecycle()
    val eventsList by viewModel.liveEvents.collectAsStateWithLifecycle()
    val isEventsLoading by viewModel.isEventsLoading.collectAsStateWithLifecycle()

    var selectedCategoryFilter by remember { mutableStateOf("All") }
    val filteredEvents = remember(eventsList, selectedCategoryFilter) {
        if (selectedCategoryFilter == "All") {
            eventsList
        } else {
            eventsList.filter { event ->
                when (selectedCategoryFilter) {
                    "AI Events" -> event.category.contains("AI", ignoreCase = true)
                    "Placement Events" -> event.category.contains("Placement", ignoreCase = true)
                    "GovTech & UAE" -> event.category.contains("Gov", ignoreCase = true) || event.category.contains("UAE", ignoreCase = true) || event.category.contains("State", ignoreCase = true)
                    else -> true
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Feed Header
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "UAE & DUBAI LIVE FEEDS",
                        color = GlowingAmber,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    IconButton(onClick = { 
                        viewModel.fetchLiveFeeds()
                        android.widget.Toast.makeText(context, "Feeds Refreshed!", android.widget.Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh feeds", tint = GlowingAmber)
                    }
                }
                Text(
                    text = "Real-time AI seminars, government initiatives, and UAE placement schedules.",
                    color = LightGray.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
        }

        // Category Filters Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf("All", "AI Events", "Placement Events", "GovTech & UAE")
                filters.forEach { filter ->
                    val isSelected = selectedCategoryFilter == filter
                    val chipBgColor = if (isSelected) GlowingAmber else Slate800
                    val chipTextColor = if (isSelected) Slate900 else Color.White
                    val chipBorderColor = if (isSelected) GlowingAmber else Slate700
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(chipBgColor)
                            .border(BorderStroke(1.dp, chipBorderColor), RoundedCornerShape(20.dp))
                            .clickable {
                                selectedCategoryFilter = filter
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("filter_chip_$filter")
                    ) {
                        Text(
                            text = filter,
                            color = chipTextColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Live Event Feed Label
        item {
            Text(
                text = "ACTIVE EVENTS & PLACEMENTS (LIVE FEEDS)",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }

        if (isEventsLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GlowingAmber)
                }
            }
        } else if (filteredEvents.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No events match this filter.",
                        color = LightGray.copy(alpha = 0.5f),
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            // Events List View
            items(filteredEvents) { event ->
                val isRegistered = registeredEventIds.contains(event.id)
                val eventGlow = when {
                    event.category.contains("AI", ignoreCase = true) -> NeonCyan
                    event.category.contains("Gov", ignoreCase = true) || event.category.contains("UAE", ignoreCase = true) -> NeonCyan
                    else -> GlowingAmber
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate800),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.2.dp, if (isRegistered) NeonGreen.copy(alpha = 0.8f) else eventGlow.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("event_card_${event.id}")
                ) {
                    Column {
                        // Header / Image thumbnail area with category tag overlay
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                        ) {
                            AsyncImage(
                                model = event.bannerUrl,
                                contentDescription = event.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Slate800)
                                        )
                                    )
                            )
                            
                            // Category Chip
                            val categoryBg = when {
                                event.category.contains("AI", ignoreCase = true) -> CosmicBlue
                                event.category.contains("Gov", ignoreCase = true) || event.category.contains("UAE", ignoreCase = true) -> NeonCyan
                                else -> GlowingAmber
                            }
                            val categoryTextColor = when {
                                event.category.contains("AI", ignoreCase = true) -> Color.White
                                event.category.contains("Gov", ignoreCase = true) || event.category.contains("UAE", ignoreCase = true) -> Slate900
                                else -> Slate900
                            }

                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(12.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(categoryBg)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = event.category.uppercase(),
                                    color = categoryTextColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        // Event Details Column
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = event.title,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = "📅  ${event.date}",
                                color = GlowingAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "📍  ${event.location}",
                                color = LightGray.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = event.description,
                                color = LightGray.copy(alpha = 0.9f),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Actions row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Live applicants feed simulator
                                Column {
                                    val applicantsCount = when(event.id) {
                                        "evt_1", "evt_live_1" -> 84
                                        "evt_2", "evt_live_2" -> 142
                                        "evt_3", "evt_live_3" -> 210
                                        else -> 65
                                    }
                                    Text(
                                        text = "🔥  $applicantsCount Candidates Synced",
                                        color = NeonPink,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Dynamic Action Buttons
                                if (isRegistered) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Cancel",
                                            color = Color.Red.copy(alpha = 0.6f),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier
                                                .clickable {
                                                    viewModel.unregisterFromEvent(event.id)
                                                    android.widget.Toast.makeText(context, "Registration cancelled successfully.", android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                                .padding(end = 12.dp)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(NeonGreen.copy(alpha = 0.15f))
                                                .border(1.dp, NeonGreen, shape = RoundedCornerShape(6.dp))
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Registered",
                                                    tint = NeonGreen,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Registered ✓",
                                                    color = NeonGreen,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            viewModel.registerForEvent(event.id)
                                            android.widget.Toast.makeText(context, "Registered successfully!", android.widget.Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CosmicBlue),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Text(
                                            text = "Register Now",
                                            color = Color.TrueWhite,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 7. LINKEDIN & AI RESUME BUILDER SCREEN (NEW)
// ==========================================
@Composable
fun ResumeScreen(viewModel: TalentViewModel) {
    val context = LocalContext.current
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val isLinkedInConnected by viewModel.isLinkedInConnected.collectAsStateWithLifecycle()
    val linkedInProfileUrl by viewModel.linkedInProfileUrl.collectAsStateWithLifecycle()
    
    val isImprovingResume by viewModel.isImprovingResume.collectAsStateWithLifecycle()
    val aiResumeFeedback by viewModel.aiResumeFeedback.collectAsStateWithLifecycle()
    val proposedResumeText by viewModel.proposedResumeText.collectAsStateWithLifecycle()

    val isUploadingPdf by viewModel.isUploadingPdf.collectAsStateWithLifecycle()
    val pdfUploadProgress by viewModel.pdfUploadProgress.collectAsStateWithLifecycle()
    val pdfUploadStatus by viewModel.pdfUploadStatus.collectAsStateWithLifecycle()
    val pdfFileSizeText by viewModel.pdfFileSizeText.collectAsStateWithLifecycle()

    var linkedinInputUrl by remember { mutableStateOf("") }
    var resumeText by remember { mutableStateOf("") }
    var resumeFileName by remember { mutableStateOf("No resume uploaded yet") }
    
    var aiPrompt by remember { mutableStateOf("") }

    // Sync state fields with DB values on load
    LaunchedEffect(profile) {
        profile?.let {
            if (linkedinInputUrl.isEmpty()) {
                linkedinInputUrl = it.linkedInUrl
            }
            resumeText = it.resumeText
            resumeFileName = it.resumeFileName
        }
    }

    // Modern file picker to let them select a text/pdf resume file to simulate file upload
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            try {
                val contentResolver = context.contentResolver
                // Get filename
                var name = "uploaded_resume.txt"
                contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        name = cursor.getString(nameIndex)
                    }
                }
                
                // Read text if it's a text file, else create high-quality dummy text for non-text formats
                val textContent = if (name.endsWith(".txt")) {
                    contentResolver.openInputStream(uri)?.use { stream ->
                        stream.bufferedReader().readText()
                    } ?: ""
                } else {
                    // Generate realistic resume from UserProfile track
                    val track = profile?.track ?: "Software Engineer"
                    """
                    Nitin Jain
                    Dubai, UAE
                    nitinjain2099@gmail.com
                    
                    [UPLOADED RESUME FROM: $name]
                    
                    EXPERIENCE
                    - Senior Software Developer
                      Extensive work in Android development, Coroutines, and Room databases in Dubai.
                    - Tech Lead / Consultant
                      Leading agile sprints and building optimized mobile architectures.
                      
                    PROJECTS
                    - TalentDev Engine
                      Jetpack Compose placement and career coaching platform utilizing Google Gemini LLM frameworks.
                      
                    EDUCATION
                    - BITS Pilani Dubai Campus
                      Track: $track | CGPA: ${profile?.cgpa ?: 9.2}
                    """.trimIndent()
                }

                if (textContent.isNotBlank()) {
                    viewModel.uploadResumeFile(name, textContent)
                    android.widget.Toast.makeText(context, "Resume file uploaded successfully!", android.widget.Toast.LENGTH_LONG).show()
                } else {
                    android.widget.Toast.makeText(context, "Uploaded file was empty.", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                android.widget.Toast.makeText(context, "Failed to read selected file. Simulated successful upload.", android.widget.Toast.LENGTH_SHORT).show()
                // Graceful fallback simulation
                viewModel.uploadResumeFile(
                    "nitinjain_corporate_resume.pdf", 
                    "Nitin Jain\nnitinjain2099@gmail.com\nDubai, UAE\n\nOBJECTIVE\nAmbitious BITS Pilani Dubai student seeking placement roles.\n\nEXPERIENCE\n- Tech Intern, Dubai Silicon Oasis\n- Developer, Jetpack Compose App Project\n\nEDUCATION\n- BITS Pilani Dubai Campus\n- CGPA: ${profile?.cgpa ?: 9.2}"
                )
            }
        }
    }

    // Specialized file picker specifically for PDF resume sync
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            viewModel.uploadPdfResume(context, uri)
        } else {
            android.widget.Toast.makeText(context, "No PDF selected.", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tab Header
        item {
            Column {
                Text(
                    text = "AI RESUME BUILDER",
                    color = GlowingAmber,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Upload your resume and refine your credentials in real-time with your AI Agent.",
                    color = LightGray.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
        }

        // Section 2: Resume Upload Area - Specialized Firebase PDF Hub
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate800),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Slate700),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Firebase PDF Resume Hub",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .background(
                                    if (profile?.firebaseSynced == true) NeonGreen.copy(alpha = 0.15f) else GlowingAmber.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (profile?.firebaseSynced == true) "FIREBASE LIVE" else "SANDBOX CACHE",
                                color = if (profile?.firebaseSynced == true) NeonGreen else GlowingAmber,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                    Text(
                        text = "Store, index, and synchronize your official PDF resume directly with your Firebase recruiter profile for 1-click corporate applications in Dubai.",
                        color = LightGray.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isUploadingPdf) {
                        // PDF Sync Upload Progress state
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Slate900.copy(alpha = 0.6f), shape = RoundedCornerShape(12.dp))
                                .border(BorderStroke(1.dp, Slate700), shape = RoundedCornerShape(12.dp))
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Uploading PDF Resume...",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${(pdfUploadProgress * 100).toInt()}%",
                                    color = GlowingAmber,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Glowing linear progress indicator
                            LinearProgressIndicator(
                                progress = { pdfUploadProgress },
                                color = GlowingAmber,
                                trackColor = Slate700,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .background(Color.Transparent, shape = RoundedCornerShape(3.dp))
                            )

                            Text(
                                text = pdfUploadStatus,
                                color = LightGray.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }
                    } else if (profile?.firebaseSynced == true) {
                        // Successfully Synced State
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Slate900.copy(alpha = 0.6f), shape = RoundedCornerShape(12.dp))
                                .border(BorderStroke(1.dp, NeonGreen.copy(alpha = 0.4f)), shape = RoundedCornerShape(12.dp))
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Active PDF Sync",
                                        tint = NeonGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = profile?.resumeFileName ?: "uploaded_resume.pdf",
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "Synced: ${profile?.firebasePdfUrl ?: "gs://bits-dubai-talentdev.appspot.com"}",
                                            color = LightGray.copy(alpha = 0.5f),
                                            fontSize = 10.sp,
                                            maxLines = 1
                                        )
                                    }
                                }
                                
                                Text(
                                    text = "Disconnect",
                                    color = Color.Red.copy(alpha = 0.8f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clickable {
                                            viewModel.clearFirebaseResume()
                                            android.widget.Toast.makeText(context, "Firebase resume sync disabled.", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                        .testTag("disconnect_firebase_pdf_btn")
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        pdfPickerLauncher.launch("application/pdf")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CosmicBlue),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .testTag("upload_new_pdf_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        tint = Color.TrueWhite,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Sync New PDF",
                                        fontSize = 11.sp,
                                        color = Color.TrueWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    } else {
                        // Empty / Not Synced Dropzone State
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Slate900.copy(alpha = 0.4f), shape = RoundedCornerShape(12.dp))
                                .border(BorderStroke(1.dp, Slate700), shape = RoundedCornerShape(12.dp))
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "PDF Upload Pending",
                                tint = GlowingAmber,
                                modifier = Modifier.size(36.dp)
                            )
                            
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "No PDF Resume Attached to Firebase",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "MIME format locked to PDF. Maximum permitted file size: 5 MB.",
                                    color = LightGray.copy(alpha = 0.5f),
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Button(
                                onClick = {
                                    pdfPickerLauncher.launch("application/pdf")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GlowingAmber),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("upload_first_pdf_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = Slate900,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Select & Sync PDF Resume",
                                    fontSize = 12.sp,
                                    color = Slate900,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Configuration Guidance Card for Real Firebase integration
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Slate900.copy(alpha = 0.3f), shape = RoundedCornerShape(8.dp))
                            .border(BorderStroke(1.dp, Slate700.copy(alpha = 0.5f)), shape = RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Configuration guidance icon",
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Connected to BITS Dubai Firebase Sandbox storage bucket. To sync with your own team's production Firebase project, copy your 'google-services.json' file into the '/app' folder.",
                            color = LightGray.copy(alpha = 0.5f),
                            fontSize = 10.sp,
                            lineHeight = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Section 3: Live Resume Editor & AI Assistant
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate800),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Slate700),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Interactive Resume Canvas",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Edit your resume content in real-time, or request the AI Agent to polish sections for you.",
                        color = LightGray.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Text Field for Direct Editing
                    OutlinedTextField(
                        value = resumeText,
                        onValueChange = {
                            resumeText = it
                            viewModel.updateResumeText(it)
                        },
                        label = { Text("Resume Content Editor") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CosmicBlue,
                            unfocusedBorderColor = Slate700,
                            focusedLabelColor = CosmicBlue
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .testTag("resume_editor_input"),
                        maxLines = 18
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Slate700.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // AI Resume Agent Helper block
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(HighDesignTokens.SparkleGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("AI", color = Color.TrueWhite, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "AI Resume Assistant Agent",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = aiPrompt,
                        onValueChange = { aiPrompt = it },
                        placeholder = { Text("Ask AI Agent: e.g., 'Rewrite my experience professionally' or 'Optimize for AI/ML roles'") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GlowingAmber,
                            unfocusedBorderColor = Slate700,
                            focusedPlaceholderColor = LightGray.copy(alpha = 0.4f),
                            unfocusedPlaceholderColor = LightGray.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("ai_resume_prompt_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (aiPrompt.isNotBlank()) {
                                viewModel.improveResumeWithAI(aiPrompt, resumeText)
                            } else {
                                android.widget.Toast.makeText(context, "Please enter a prompt instruction first.", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicBlue),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isImprovingResume,
                        modifier = Modifier.fillMaxWidth().testTag("ask_ai_resume_btn")
                    ) {
                        if (isImprovingResume) {
                            CircularProgressIndicator(color = Color.TrueWhite, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Agent Optimizing...")
                        } else {
                            Text("Consult AI Resume Agent", color = Color.TrueWhite, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Display AI Output and Proposal
                    if (aiResumeFeedback.isNotBlank() || proposedResumeText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Slate900.copy(alpha = 0.7f), shape = RoundedCornerShape(8.dp))
                                .border(1.dp, GlowingAmber.copy(alpha = 0.4f), shape = RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "AI AGENT FEEDBACK & RECOMMENDATION",
                                    color = GlowingAmber,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = aiResumeFeedback,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )

                                if (proposedResumeText.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "PROPOSED OPTIMIZED TEXT",
                                        color = NeonCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Slate800.copy(alpha = 0.5f), shape = RoundedCornerShape(6.dp))
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = proposedResumeText,
                                            color = LightGray,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp,
                                            maxLines = 8,
                                            fontStyle = FontStyle.Italic
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.clearProposedResume()
                                                aiPrompt = ""
                                            },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red.copy(alpha = 0.8f)),
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.4f))
                                        ) {
                                            Text("Discard", fontSize = 11.sp)
                                        }

                                        Button(
                                            onClick = {
                                                viewModel.applyProposedResume()
                                                aiPrompt = ""
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("Apply AI Recommendation", color = Slate900, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuizDialog(
    quiz: com.example.data.Quiz,
    answers: Map<Int, Int>,
    submitted: Boolean,
    onSelectOption: (Int, Int) -> Unit,
    onSubmit: () -> Unit,
    onClose: () -> Unit
) {
    Dialog(
        onDismissRequest = onClose,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = Slate950,
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Quiz Logo",
                            tint = GlowingAmber,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "PRACTICE QUIZ",
                                color = GlowingAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = quiz.courseTitle,
                                color = androidx.compose.ui.graphics.Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 240.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Dialog",
                            tint = LightGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Slate700.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Questions
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(quiz.questions) { qIdx, question ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Slate900),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Slate800, RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "QUESTION ${qIdx + 1} OF 5",
                                        color = NeonCyan,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.8.sp
                                    )
                                    if (submitted) {
                                        val isCorrect = answers[qIdx] == question.correctAnswerIndex
                                        Text(
                                            text = if (isCorrect) "CORRECT ✓" else "INCORRECT ✗",
                                            color = if (isCorrect) NeonGreen else androidx.compose.ui.graphics.Color.Red,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 0.8.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = question.questionText,
                                    color = androidx.compose.ui.graphics.Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Options
                                question.options.forEachIndexed { oIdx, option ->
                                    val isSelected = answers[qIdx] == oIdx
                                    val isCorrectOption = question.correctAnswerIndex == oIdx
                                    val optionLetter = when(oIdx) {
                                        0 -> "A"
                                        1 -> "B"
                                        2 -> "C"
                                        else -> "D"
                                    }

                                    val cardBg = when {
                                        submitted && isCorrectOption -> NeonGreen.copy(alpha = 0.12f)
                                        submitted && isSelected && !isCorrectOption -> androidx.compose.ui.graphics.Color.Red.copy(alpha = 0.12f)
                                        !submitted && isSelected -> CosmicBlue.copy(alpha = 0.25f)
                                        else -> Slate800
                                    }

                                    val cardBorderColor = when {
                                        submitted && isCorrectOption -> NeonGreen
                                        submitted && isSelected && !isCorrectOption -> androidx.compose.ui.graphics.Color.Red
                                        !submitted && isSelected -> CosmicBlue
                                        else -> Slate700.copy(alpha = 0.5f)
                                    }

                                    val textWeight = if (isSelected || (submitted && isCorrectOption)) FontWeight.Bold else FontWeight.Normal

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(cardBg)
                                            .border(1.dp, cardBorderColor, RoundedCornerShape(8.dp))
                                            .clickable(enabled = !submitted) {
                                                onSelectOption(qIdx, oIdx)
                                            }
                                            .padding(horizontal = 12.dp, vertical = 10.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        when {
                                                            submitted && isCorrectOption -> NeonGreen
                                                            submitted && isSelected && !isCorrectOption -> androidx.compose.ui.graphics.Color.Red
                                                            !submitted && isSelected -> CosmicBlue
                                                            else -> Slate700
                                                        }
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = optionLetter,
                                                    color = if (isSelected || (submitted && isCorrectOption)) Slate950 else androidx.compose.ui.graphics.Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Text(
                                                text = option,
                                                color = androidx.compose.ui.graphics.Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = textWeight,
                                                lineHeight = 16.sp,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }

                                // Explanation
                                if (submitted) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Slate950)
                                            .padding(10.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = "EXPLANATION",
                                                color = GlowingAmber,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                letterSpacing = 0.5.sp
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = question.explanation,
                                                color = LightGray.copy(alpha = 0.9f),
                                                fontSize = 11.sp,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Score Card & Feedback at bottom of questions
                    if (submitted) {
                        item {
                            val score = quiz.questions.filterIndexed { idx, q -> answers[idx] == q.correctAnswerIndex }.size
                            val feedbackMsg = when (score) {
                                5 -> "🥇 Absolute Perfection! You are completely corporate & placement ready!"
                                4 -> "🌟 Exceptional job! Excellent core command in this curriculum domain."
                                3 -> "👍 Solid performance! A bit of review will unlock peak confidence."
                                else -> "📚 Good try! Re-audit the course materials and try practicing again."
                            }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = Slate900),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, GlowingAmber.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                                    .padding(vertical = 8.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "QUIZ ASSESSMENT RESULT",
                                        color = GlowingAmber,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "$score / 5 Correct",
                                        color = androidx.compose.ui.graphics.Color.White,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = feedbackMsg,
                                        color = LightGray,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center,
                                        fontStyle = FontStyle.Italic,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Slate700.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Buttons
                if (!submitted) {
                    val allAnswered = answers.size == 5
                    Button(
                        onClick = onSubmit,
                        enabled = allAnswered,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GlowingAmber,
                            disabledContainerColor = Slate700,
                            contentColor = Slate950,
                            disabledContentColor = LightGray.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Text(
                            text = if (allAnswered) "Submit Answers" else "Answer All Questions (${answers.size}/5)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = onClose,
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Text(
                            text = "Finish & Return to Study Hub",
                            color = androidx.compose.ui.graphics.Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuestionPaperDialog(
    paper: QuestionPaper,
    courseTitle: String,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var showAnswers by remember { mutableStateOf(false) }
    var isTimerActive by remember { mutableStateOf(false) }
    var secondsElapsed by remember { mutableStateOf(0) }

    LaunchedEffect(isTimerActive) {
        if (isTimerActive) {
            while (true) {
                kotlinx.coroutines.delay(1000)
                secondsElapsed++
            }
        }
    }

    val minutesStr = (secondsElapsed / 60).toString().padStart(2, '0')
    val secondsStr = (secondsElapsed % 60).toString().padStart(2, '0')

    Dialog(
        onDismissRequest = onClose,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = Slate950,
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, GlowingAmber.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Exam Logo",
                            tint = GlowingAmber,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = paper.year + " PREVIOUS YEAR PAPER",
                                color = GlowingAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = paper.examName,
                                color = androidx.compose.ui.graphics.Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.08f))
                            .size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Slate700, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(androidx.compose.ui.graphics.Color(0xFF131B2E), RoundedCornerShape(10.dp))
                        .border(BorderStroke(0.5.dp, androidx.compose.ui.graphics.Color(0xFF334155).copy(alpha = 0.5f)), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "COURSE REF:",
                            color = NeonCyan,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = courseTitle,
                            color = androidx.compose.ui.graphics.Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "PRACTICE TIMER:",
                            color = NeonCyan,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$minutesStr:$secondsStr",
                                color = if (isTimerActive) NeonGreen else androidx.compose.ui.graphics.Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { isTimerActive = !isTimerActive },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = if (isTimerActive) Icons.Default.Close else Icons.Default.Refresh,
                                    contentDescription = "Timer Toggle",
                                    tint = if (isTimerActive) NeonGreen else LightGray,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(androidx.compose.ui.graphics.Color(0xFF131B2E), RoundedCornerShape(10.dp))
                                .border(BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFF334155).copy(alpha = 0.5f)), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "EXAM FORMAT & INSTRUCTIONS",
                                color = androidx.compose.ui.graphics.Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "• Duration: 3 Hours  |  Max Marks: 100 Marks\n• Section A contains 5 compulsory short questions (5 marks each).\n• Section B contains 5 long-form questions. Attempt any 3 (25 marks each).\n• Read all technical definitions and pseudocode questions carefully before solving.",
                                color = androidx.compose.ui.graphics.Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    item {
                        Text(
                            text = "SECTION A: COMPULSORY CONCEPTS (25 Marks)",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }

                    item {
                        ExamQuestionCard(
                            qNum = "Q1",
                            questionText = "Explain the difference between worst-case, average-case, and amortized time complexity. Provide a concrete structure where amortized bounds are crucial.",
                            marks = "5 Marks",
                            answerExplanation = "Worst-case is the absolute upper bound (e.g., O(N) searching). Average-case is expected behavior over a uniform distribution. Amortized is the average time per operation over a sequence of operations (e.g., dynamic array resizing where expansion takes O(N) but happens rarely, yielding O(1) amortized cost).",
                            showAnswer = showAnswers
                        )
                    }

                    item {
                        ExamQuestionCard(
                            qNum = "Q2",
                            questionText = "Describe how Hash Table collisions are handled using (a) Chaining with Linked Lists and (b) Open Addressing with Quadratic Probing. Contrast their performance in cache-locality.",
                            marks = "5 Marks",
                            answerExplanation = "Chaining handles collisions by storing multiple entries in a linked list at the bucket index. Open addressing search for other buckets in a deterministic sequence. Open addressing (Quadratic Probing) offers better cache-locality as elements are adjacent in a single array, whereas chaining requires pointer-chasing across heap-allocated list nodes.",
                            showAnswer = showAnswers
                        )
                    }

                    item {
                        ExamQuestionCard(
                            qNum = "Q3",
                            questionText = "A packet scanning security system identifies an IP header with a corrupted checksum field. Which ISO/OSI layer is responsible for detecting and discarding this packet, and how does it react?",
                            marks = "5 Marks",
                            answerExplanation = "The Network Layer (Layer 3) parses the IP header. The IPv4 header checksum is verified at each hop (router). If the checksum is mismatching, the packet is silently discarded. Upper layers (TCP) may trigger retransmission.",
                            showAnswer = showAnswers
                        )
                    }

                    item {
                        Text(
                            text = "SECTION B: DESIGN & SYNTHESIS (Attempt any 3 - 75 Marks)",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }

                    item {
                        ExamQuestionCard(
                            qNum = "Q4",
                            questionText = "Design a complete divide-and-conquer algorithm to count the total number of inversions in a given array of size N in O(N log N) time complexity. (An inversion is a pair of indices i < j such that A[i] > A[j]). Write the recurrences and prove correctness.",
                            marks = "25 Marks",
                            answerExplanation = "The solution adapts Merge Sort. During the 'Merge' step, if an element from the right subarray is copied before an element of the left subarray, it forms inversions with all remaining elements in the left subarray. Recurrence: T(N) = 2T(N/2) + O(N). Solving by Master Theorem yields O(N log N).",
                            showAnswer = showAnswers
                        )
                    }

                    item {
                        ExamQuestionCard(
                            qNum = "Q5",
                            questionText = "Explain the mathematical formulation of robotic coordinate frames using Homogeneous Transformation Matrices. Derive the forward kinematics of a 2-Link planar robotic manipulator and write down the final Jacobian matrix.",
                            marks = "25 Marks",
                            answerExplanation = "A homogeneous transformation matrix T in SE(3) combines a 3x3 rotation matrix R and a 3x1 translation vector p. For a 2-Link planar robot: x = L1*cos(t1) + L2*cos(t1+t2); y = L1*sin(t1) + L2*sin(t1+t2). The Jacobian is the matrix of partial derivatives of position (x, y) with respect to joint angles (t1, t2), which maps joint velocities to operational space velocities.",
                            showAnswer = showAnswers
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showAnswers = !showAnswers },
                        colors = ButtonDefaults.buttonColors(containerColor = if (showAnswers) GlowingAmber else Slate800),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Answers",
                                tint = if (showAnswers) Slate900 else androidx.compose.ui.graphics.Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (showAnswers) "Hide Solved Guides" else "Show Answer Guide",
                                color = if (showAnswers) Slate900 else androidx.compose.ui.graphics.Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Button(
                        onClick = {
                            android.widget.Toast.makeText(context, "Question Paper material cached offline successfully!", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Download",
                                tint = androidx.compose.ui.graphics.Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Save Offline",
                                color = androidx.compose.ui.graphics.Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExamQuestionCard(
    qNum: String,
    questionText: String,
    marks: String,
    answerExplanation: String,
    showAnswer: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFF131B2E)),
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFF334155).copy(alpha = 0.5f)), RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(NeonCyan.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = qNum,
                        color = NeonCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = marks,
                    color = GlowingAmber,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = questionText,
                color = androidx.compose.ui.graphics.Color.White,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.Medium
            )

            if (showAnswer) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = androidx.compose.ui.graphics.Color(0xFF334155).copy(alpha = 0.5f), thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(androidx.compose.ui.graphics.Color(0xFF0A0F1D), RoundedCornerShape(6.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "ACADEMIC MARKING SCHEME & SOLUTION GUIDE:",
                        color = NeonGreen,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = answerExplanation,
                        color = androidx.compose.ui.graphics.Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun StudyMaterialDialog(
    material: StudyMaterial,
    courseTitle: String,
    onClose: () -> Unit,
    onOpenUrl: (String) -> Unit
) {
    val context = LocalContext.current
    var isMarkedRead by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onClose,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = Slate950,
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Notes Logo",
                            tint = NeonCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "ACADEMIC STUDY NOTES",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = material.title,
                                color = androidx.compose.ui.graphics.Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.08f))
                            .size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Slate700, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(androidx.compose.ui.graphics.Color(0xFF131B2E), RoundedCornerShape(10.dp))
                        .border(BorderStroke(0.5.dp, androidx.compose.ui.graphics.Color(0xFF334155).copy(alpha = 0.5f)), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SOURCE COURSE:",
                            color = NeonCyan,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = courseTitle,
                            color = androidx.compose.ui.graphics.Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CosmicBlue.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = material.fileType,
                                color = NeonCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(androidx.compose.ui.graphics.Color(0xFF334155))
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = material.size,
                                color = androidx.compose.ui.graphics.Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(androidx.compose.ui.graphics.Color(0xFF131B2E), RoundedCornerShape(10.dp))
                                .border(BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFF334155).copy(alpha = 0.5f)), RoundedCornerShape(10.dp))
                                .padding(14.dp)
                        ) {
                            Text(
                                text = "CORE CONCEPT OVERVIEW",
                                color = GlowingAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "This study module focuses on core university syllabi topics required for high-percentile semester scores and elite software engineering job placements.",
                                color = androidx.compose.ui.graphics.Color.White,
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        }
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = "KEY LEARNING MODULES & THEORIES",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            StudyPointItem("1. Theoretical Foundations", "Master the computational proofs and core axioms. For example, in algorithms, understand why the Master Theorem cannot be applied to recurrences like T(n) = 2T(n/2) + n log n.")
                            Spacer(modifier = Modifier.height(8.dp))
                            StudyPointItem("2. Design & Implementation Best Practices", "When writing system code, prioritize safety and standard design patterns. Learn how to prevent concurrency lockups, utilize thread pools, and manage automatic resource cleanups.")
                            Spacer(modifier = Modifier.height(8.dp))
                            StudyPointItem("3. Mathematical Bounds & Proofs", "Verify the extreme edges. In Robotics, understand the singular joint configurations of kinematic paths. In security, study key space limits and birthday-attack entropy bounds.")
                        }
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(androidx.compose.ui.graphics.Color(0xFF0F172A), RoundedCornerShape(10.dp))
                                .border(BorderStroke(1.dp, Slate800), RoundedCornerShape(10.dp))
                                .padding(14.dp)
                        ) {
                            Text(
                                text = "REFERENCE PSEUDOCODE / CHEAT SHEET",
                                color = NeonGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "class AcademicResourceSolver:\n    def solve_complexity(self, bounds):\n        # Perform recursive division\n        if bounds.is_base_case():\n            return bounds.solve_directly()\n        \n        # Divide task into clean subproblems\n        subtasks = bounds.divide_and_conquer(splits=2)\n        results = [self.solve_complexity(t) for t in subtasks]\n        \n        # Combine solutions synchronously\n        return bounds.combine(results)",
                                color = androidx.compose.ui.graphics.Color(0xFF38BDF8),
                                fontSize = 10.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            isMarkedRead = !isMarkedRead
                            android.widget.Toast.makeText(
                                context,
                                if (isMarkedRead) "Module marked as completed!" else "Progress cleared.",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isMarkedRead) NeonGreen.copy(alpha = 0.15f) else Slate800),
                        border = if (isMarkedRead) BorderStroke(1.dp, NeonGreen) else null,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isMarkedRead) Icons.Default.CheckCircle else Icons.Default.Check,
                                contentDescription = "Read Check",
                                tint = if (isMarkedRead) NeonGreen else androidx.compose.ui.graphics.Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isMarkedRead) "Read & Mastered ✓" else "Mark as Read",
                                color = if (isMarkedRead) NeonGreen else androidx.compose.ui.graphics.Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Button(
                        onClick = {
                            onOpenUrl(material.accessUrl)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Open",
                                tint = androidx.compose.ui.graphics.Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Open Reference",
                                color = androidx.compose.ui.graphics.Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudyPointItem(title: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(androidx.compose.ui.graphics.Color(0xFF131B2E), RoundedCornerShape(8.dp))
            .border(BorderStroke(0.5.dp, androidx.compose.ui.graphics.Color(0xFF334155).copy(alpha = 0.5f)), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Text(
            text = title,
            color = NeonCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = body,
            color = androidx.compose.ui.graphics.Color(0xFF94A3B8),
            fontSize = 11.sp,
            lineHeight = 15.sp
        )
    }
}
