package com.simats.moneymentor.ui.screens

import androidx.compose.animation.core.*

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.* // Using Rounded icons for a modern look
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.moneymentor.R
import com.simats.moneymentor.ui.theme.*


import com.simats.moneymentor.ui.components.AnimatedBottomNavigation
import androidx.activity.compose.BackHandler
import com.simats.moneymentor.data.LearningArticle

@Composable
fun HomeScreen(
    initialTab: String = "Home",
    userName: String = "Alex Johnson",
    insightViewModel: com.simats.moneymentor.ui.viewmodels.InsightViewModel? = null,
    onCreateGoalClick: () -> Unit = {},
    onEditGoalClick: () -> Unit = {},
    onGoalClick: () -> Unit = {},
    onDailyTipClick: () -> Unit = {},
    onQuickActionClick: (String) -> Unit = {},
    onViewAllGoalsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onArticleClick: (Int, LearningArticle) -> Unit = { _, _ -> },
    onQuizClick: (Int) -> Unit = {},
    onGoldCardClick: () -> Unit = {},
    onSilverCardClick: () -> Unit = {},
    silverViewModel: com.simats.moneymentor.ui.viewmodels.SilverViewModel? = null,
    notificationViewModel: com.simats.moneymentor.ui.viewmodels.NotificationViewModel? = null
) {
    var currentTab by remember(initialTab) { mutableStateOf(initialTab) }

    // Handle back press to go to Home tab if not already there
    BackHandler(enabled = currentTab != "Home") {
        currentTab = "Home"
    }

    Scaffold(
        bottomBar = { 
            AnimatedBottomNavigation(
                currentTab = currentTab,
                onTabSelected = { currentTab = it }
            ) 
        },
        containerColor = Color(0xFFFAFAF5)
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            val onTabAction = { action: String ->
                when (action) {
                    "Goals" -> currentTab = "Goals"
                    "Tools" -> currentTab = "Tools"
                    "AI" -> currentTab = "Chat"
                    "Learn" -> currentTab = "Learn"
                }
                onQuickActionClick(action)
            }

            when (currentTab) {
                "Home" -> {
                    HomeTabContent(
                        userName = userName,
                        insightViewModel = insightViewModel,
                        onNavigateToGoal = {
                            currentTab = "Goals"
                            onViewAllGoalsClick()
                        },
                        onNavigateToDailyInsights = onDailyTipClick,
                        onProfileClick = onProfileClick,
                        onQuickActionClick = onTabAction,
                        onGoldCardClick = onGoldCardClick,
                        onSilverCardClick = onSilverCardClick,
                        silverViewModel = silverViewModel,
                        notificationViewModel = notificationViewModel
                    )
                }
                "Goals" -> {
                    GoalsScreen(
                        onCreateGoalClick = onCreateGoalClick,
                        onEditGoalClick = onEditGoalClick,
                        onGoalClick = onGoalClick
                    )
                }
                "Tools" -> {
                    ToolsScreen()
                }
                "Learn" -> {
                    LearningCenterScreen(
                        onArticleClick = onArticleClick,
                        onQuizClick = onQuizClick
                    )
                }
                "Chat" -> {
                    ChatScreen()
                }
            }
        }
    }
}

@Composable
fun HomeTabContent(
    userName: String,
    insightViewModel: com.simats.moneymentor.ui.viewmodels.InsightViewModel? = null,
    onNavigateToGoal: () -> Unit,
    onNavigateToDailyInsights: () -> Unit,
    onProfileClick: () -> Unit,
    onQuickActionClick: (String) -> Unit,
    onGoldCardClick: () -> Unit = {},
    onSilverCardClick: () -> Unit = {},
    silverViewModel: com.simats.moneymentor.ui.viewmodels.SilverViewModel? = null,
    notificationViewModel: com.simats.moneymentor.ui.viewmodels.NotificationViewModel? = null
) {
    val scrollState = rememberScrollState()
    val context = androidx.compose.ui.platform.LocalContext.current

    // State to toggle Notification Bottom Sheet
    var showNotifications by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAFAF5))
                .verticalScroll(scrollState)
        ) {
            // Header
            HomeHeader(
                userName = userName,
                notificationViewModel = notificationViewModel,
                onProfileClick = onProfileClick,
                onNotificationClick = { showNotifications = true }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Overview Cards
            OverviewSection()

            Spacer(modifier = Modifier.height(24.dp))

            // Live Rates Section
            LiveRatesSection(
                onGoldCardClick = onGoldCardClick,
                onSilverCardClick = onSilverCardClick,
                silverViewModel = silverViewModel
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Actions
            QuickActionsSection(
                onInsightsClick = onNavigateToDailyInsights,
                onGoalsClick = onNavigateToGoal,
                onQuickActionClick = onQuickActionClick
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Daily Tip
            DailyTipSection(
                insightViewModel = insightViewModel,
                onReadMoreClick = onNavigateToDailyInsights
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Your Goals
            YourGoalsSection(onViewAllClick = onNavigateToGoal)

            Spacer(modifier = Modifier.height(32.dp))
        }

        if (showNotifications) {
            NotificationBottomSheet(
                viewModel = notificationViewModel,
                onDismiss = { showNotifications = false }
            )
        }
    }
}

@Composable
fun HomeHeader(
    userName: String,
    notificationViewModel: com.simats.moneymentor.ui.viewmodels.NotificationViewModel? = null,
    onProfileClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {}
) {
    val notifications by notificationViewModel?.notifications?.collectAsState() ?: remember { mutableStateOf(emptyList()) }
    val hasUnread = notifications.isNotEmpty() // Show dot if there are any notifications

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp) // Adjusted height for better spacing
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(HomeHeaderStart, HomeHeaderMid, HomeHeaderEnd)
                )
            )
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // App Icon + Greeting (Left)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AccountBalanceWallet, // App Icon
                        contentDescription = "App Logo",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Good Morning,",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 16.sp
                    )
                    Text(
                        text = userName,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Right Side: Notification & Profile
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Notification Bell
                IconButton(onClick = onNotificationClick) {
                    BadgedBox(
                        badge = {
                            if (hasUnread) {
                                Badge(containerColor = Color.Red)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Profile Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .clickable(onClick = onProfileClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = "Profile",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun OverviewSection() {
    val goals = com.simats.moneymentor.data.GoalRepository.goals
    val onTrackCount = goals.count { it.status == com.simats.moneymentor.data.GoalStatus.OnTrack }

    // Learning Stats from Repository
    val learningProgress = (com.simats.moneymentor.data.LearningRepository.overallProgressValue * 100).toInt()
    val totalModules = com.simats.moneymentor.data.LearningRepository.modules.size

    LaunchedEffect(Unit) {
        if (goals.isEmpty()) {
            com.simats.moneymentor.data.GoalRepository.fetchGoals(com.simats.moneymentor.data.GoalRepository.currentUserId)
        }
    }

    Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 8.dp)) {
        SectionTitle(title = "Dashboard", stripColor = Color(0xFF7E57C2)) // Deep Purple
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Learning Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(160.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Glowing Top Strip
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(CardIconTintLearning, CardIconTintLearning.copy(alpha = 0.0f))
                                )
                            )
                    )

                    Box(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                        // Background Watermark (Book)
                        Icon(
                            imageVector = Icons.Rounded.MenuBook,
                            contentDescription = null,
                            tint = CardIconTintLearning.copy(alpha = 0.1f),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(80.dp)
                                .offset(x = 20.dp, y = 20.dp)
                        )

                        // Icon
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(CardIconBgLearning),
                            contentAlignment = Alignment.Center
                        ) {
                             Icon(Icons.Rounded.MenuBook, null, tint = CardIconTintLearning, modifier = Modifier.size(24.dp))
                        }
                        
                        // Badge (Top Right)
                        Box(
                             modifier = Modifier
                                .align(Alignment.TopEnd)
                                .background(Color(0xFFF3E5F5), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                             Text(
                                text = "$totalModules modules",
                                fontSize = 10.sp,
                                color = CardIconTintLearning,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Content
                        Column(modifier = Modifier.align(Alignment.BottomStart)) {
                            Text(text = "Learning", fontSize = 14.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "$learningProgress%", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Active Goals Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(160.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                     // Glowing Top Strip
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(CardIconTintGoals, CardIconTintGoals.copy(alpha = 0.0f))
                                )
                            )
                    )

                    Box(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                        // Background Watermark (Target/Bullseye)
                        Icon(
                            imageVector = Icons.Rounded.TrackChanges,
                            contentDescription = null,
                            tint = CardIconTintGoals.copy(alpha = 0.1f),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(90.dp)
                                .offset(x = 20.dp, y = 20.dp)
                        )

                        // Icon
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(CardIconBgGoals),
                            contentAlignment = Alignment.Center
                        ) {
                             Icon(Icons.Rounded.TrackChanges, null, tint = CardIconTintGoals, modifier = Modifier.size(24.dp))
                        }
                        
                        // Badge with Up Arrow
                         Box(
                             modifier = Modifier
                                .align(Alignment.TopEnd)
                                .background(Color(0xFFE0F2F1), RoundedCornerShape(8.dp)) // Light Teal
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                             Text(
                                text = "↗ $onTrackCount on track",
                                fontSize = 10.sp,
                                color = Color(0xFF009688),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Content
                        Column(modifier = Modifier.align(Alignment.BottomStart)) {
                            Text(text = "Active Goals", fontSize = 14.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "${goals.size} Goals", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun LiveRatesSection(
    onGoldCardClick: () -> Unit = {},
    onSilverCardClick: () -> Unit = {},
    silverViewModel: com.simats.moneymentor.ui.viewmodels.SilverViewModel? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live_alpha"
    )

    val silverRateState by (silverViewModel?.rateState?.collectAsState() ?: remember { mutableStateOf(com.simats.moneymentor.ui.viewmodels.SilverRateState.Idle) })
    
    var goldPrice by remember { mutableStateOf("Loading...") }
    var goldChange by remember { mutableStateOf("Loading...") }
    var goldIsUp by remember { mutableStateOf(true) }

    var silverPrice by remember { mutableStateOf("Loading...") }
    var silverChange by remember { mutableStateOf("Loading...") }
    var silverIsUp by remember { mutableStateOf(true) }

    LaunchedEffect(silverRateState) {
        when (val state = silverRateState) {
            is com.simats.moneymentor.ui.viewmodels.SilverRateState.Success -> {
                silverPrice = "₹${state.response.silverPrice}"
                val change = state.response.todayChange?.replace("%", "")?.trim() ?: "+0.00"
                val isPos = !change.startsWith("-")
                val pfx = if (isPos && !change.startsWith("+")) "+" else ""
                silverIsUp = isPos
                silverChange = if (isPos) "↗ $pfx$change" else "↘ $change"
            }
            is com.simats.moneymentor.ui.viewmodels.SilverRateState.Error -> {
                silverPrice = "N/A"
                silverChange = "--"
                silverIsUp = false
                
                // Auto-retry after 2 seconds as requested
                kotlinx.coroutines.delay(2000)
                silverViewModel?.fetchRates(force = true)
            }
            else -> {
                silverPrice = "Loading..."
                silverChange = "Loading..."
                silverIsUp = true
            }
        }
    }

    LaunchedEffect(Unit) {
        // Reset prices to show loading immediately when screen is shown
        goldPrice = "Loading..."
        goldChange = "Loading..."
        silverPrice = "Loading..."
        silverChange = "Loading..."
        
        silverViewModel?.fetchRates(force = true)
        try {
            val goldResponse = com.simats.moneymentor.data.network.RetrofitClient.goldRateService.getGoldRates()
            goldPrice = "₹${goldResponse.goldPrice}"
            val c24k = goldResponse.change24kToday?.replace("%", "")?.trim() ?: "+0.00"
            val isPos = !c24k.startsWith("-")
            val pfx = if (isPos && !c24k.startsWith("+")) "+" else ""
            val formattedC24k = "$pfx$c24k"
            goldIsUp = isPos
            goldChange = if (isPos) "↗ $formattedC24k" else "↘ $formattedC24k"
        } catch (e: Exception) {
            goldPrice = "Error"
            e.printStackTrace()
        }
    }

     Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        // Updated Section Title to match Overview style but with badge
        // Updated Section Title with Badge beside it
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
             // Vertical Strip
            Box(
                modifier = Modifier
                    .height(24.dp)
                    .width(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFFFC107)) // Gold Strip
            )
            Spacer(modifier = Modifier.width(8.dp))
            
            // Title
            Text(
                text = "Live Rates",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.weight(1f))

            // Live Badge
            Box(
                modifier = Modifier
                    .background(Color(0xFFFFEBEE), RoundedCornerShape(4.dp))
                    .border(1.dp, LiveDotColor.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(LiveDotColor.copy(alpha = alpha)))
                    Spacer(Modifier.width(4.dp))
                    Text("LIVE", fontSize = 10.sp, color = LiveTextColor, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            RateCard(
                name = "GOLD 24K",
                unit = "per gram",
                price = goldPrice,
                change = goldChange,
                isUp = goldIsUp,
                bgColor = GoldCardBg,
                borderColor = GoldCardBorder,
                titleColor = GoldTextPrimary,
                subtitleColor = GoldTextSecondary,
                watermarkIcon = Icons.Rounded.EmojiEvents, // Trophy
                icon = Icons.Rounded.MonetizationOn, // Coin
                iconTint = Color(0xFFFFB300),
                modifier = Modifier.weight(1f),
                onClick = onGoldCardClick
            )
            Spacer(modifier = Modifier.width(16.dp))
            RateCard(
                name = "SILVER",
                unit = "per gram",
                price = silverPrice,
                change = silverChange,
                isUp = silverIsUp,
                bgColor = SilverCardBg,
                borderColor = SilverCardBorder,
                titleColor = SilverTextPrimary,
                subtitleColor = SilverTextSecondary,
                watermarkIcon = Icons.Rounded.WorkspacePremium, // Medal
                icon = Icons.Rounded.Savings, // Piggy bank/Savings as placeholder for Silver
                iconTint = Color(0xFF90A4AE),
                modifier = Modifier.weight(1f),
                onClick = onSilverCardClick
            )
        }
    }
}

@Composable
fun RateCard(
    name: String,
    unit: String,
    price: String,
    change: String,
    isUp: Boolean,
    bgColor: Color,
    borderColor: Color,
    titleColor: Color,
    subtitleColor: Color,
    watermarkIcon: ImageVector,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.height(180.dp).clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(2.dp, borderColor.copy(alpha = 0.8f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Watermark (Trophy/Medal)
            Icon(
                imageVector = watermarkIcon,
                contentDescription = null,
                tint = iconTint.copy(alpha = 0.15f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 12.dp, y = (-12).dp)
                    .size(64.dp)
                    .graphicsLayer(rotationZ = 15f)
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Icon and Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp) // Slightly larger
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                         Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column {
                    Text(text = name, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = titleColor)
                    Text(text = unit, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = subtitleColor)
                }
                
                // Price and Change
                Column {
                    Text(
                        text = price, 
                        fontSize = 24.sp, 
                        fontWeight = FontWeight.Black, 
                        color = Color(0xFF263238) // Dark Slate
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if(isUp) Icons.Rounded.TrendingUp else Icons.Rounded.TrendingDown,
                            contentDescription = null,
                            tint = if (isUp) Color(0xFF00C853) else Color(0xFFD50000),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = change, 
                            fontSize = 13.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = if (isUp) Color(0xFF00C853) else Color(0xFFD50000)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionsSection(
    onInsightsClick: () -> Unit = {},
    onGoalsClick: () -> Unit = {},
    onQuickActionClick: (String) -> Unit = {}
) {
     Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
        SectionTitle(title = "Quick Actions", stripColor = HomeHeaderStart) // Orange
        
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
             QuickActionButton(
                 icon = Icons.Rounded.Psychology, // Ask AI -> Psychology/Brain
                 text = "Ask AI",
                 bg = Color(0xFFE0F2F1), // Very light teal
                 iconColor = Color(0xFF009688),
                 modifier = Modifier.weight(1f),
                 onClick = { onQuickActionClick("AI") }
             )
             Spacer(modifier = Modifier.width(16.dp))
             QuickActionButton(
                 icon = Icons.Rounded.Calculate, // Tools -> Calculator
                 text = "Tools",
                 bg = Color(0xFFE3F2FD), // Very light blue
                 iconColor = Color(0xFF2196F3),
                 modifier = Modifier.weight(1f),
                 onClick = { onQuickActionClick("Tools") }
             )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
             QuickActionButton(
                 icon = Icons.Rounded.TrendingUp, // Goals -> Trending Up
                 text = "Goals",
                 bg = Color(0xFFFFF3E0), // Very light orange
                 iconColor = Color(0xFFFF9800),
                 modifier = Modifier.weight(1f),
                 onClick = { onQuickActionClick("Goals") }
             )
             Spacer(modifier = Modifier.width(16.dp))
             QuickActionButton(
                 icon = Icons.Rounded.School, // Learn -> School
                 text = "Learn",
                 bg = Color(0xFFF3E5F5), // Very light purple
                 iconColor = Color(0xFF9C27B0),
                 modifier = Modifier.weight(1f),
                 onClick = { onQuickActionClick("Learn") }
             )
        }
     }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    text: String,
    bg: Color,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.height(60.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
             Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = text, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        }
    }
}


@Composable
fun DailyTipSection(
    insightViewModel: com.simats.moneymentor.ui.viewmodels.InsightViewModel? = null,
    onReadMoreClick: () -> Unit = {}
) {
    val tipState = insightViewModel?.tipState?.collectAsState()?.value

    LaunchedEffect(Unit) {
        insightViewModel?.fetchDailyTip()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(DailyTipStart, DailyTipEnd)
                )
            )
            .clickable { onReadMoreClick() }
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                 Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                     Text(
                        text = "DAILY TIP",
                        fontSize = 10.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                
                when (tipState) {
                    is com.simats.moneymentor.ui.viewmodels.InsightState.Success -> {
                        Text(
                            text = "Smart Tip",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = tipState.data.dailyTip ?: "Loading tip...",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                    is com.simats.moneymentor.ui.viewmodels.InsightState.Loading -> {
                        Text(
                            text = "Fetching today's tip...",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    else -> {
                        // Fallback/Placeholder if error or idle
                        Text(
                            text = "Pay Yourself First",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Before paying bills, set aside savings. Treat it like a mandatory expense.",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))

            // Icon graphic
            Icon(
                imageVector = Icons.Rounded.Lightbulb,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.2f),
                modifier = Modifier.size(80.dp)
            )
            
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun YourGoalsSection(onViewAllClick: () -> Unit = {}) {
    val goals = com.simats.moneymentor.data.GoalRepository.goals
    val displayGoals = goals.sortedWith(
        compareBy<com.simats.moneymentor.data.Goal> { it.status == com.simats.moneymentor.data.GoalStatus.Done }
        .thenByDescending { it.progress }
    ).take(2)

     Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
        SectionTitle(title = "Your Goals", stripColor = Color(0xFF00BFA5)) // Teal
        Spacer(modifier = Modifier.height(16.dp))
        
        if (displayGoals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No goals set yet. Create one!",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
        } else {
            displayGoals.forEach { goal ->
                GoalItem(
                    title = goal.name,
                    progress = (goal.currentAmount / goal.targetAmount).toFloat(),
                    currentAmount = "₹${goal.currentAmount.toInt()}",
                    totalAmount = "₹${goal.targetAmount.toInt()}",
                    dueDate = goal.formattedDueDate,
                    color = goal.color
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
     }
}

@Composable
fun GoalItem(
    title: String,
    progress: Float,
    currentAmount: String,
    totalAmount: String,
    dueDate: String,
    color: Color
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
         modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxSize(), // Full size for row
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Glowing Strip
             Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(100.dp) // Height of the card roughly
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(color, color.copy(alpha = 0.3f))
                        )
                    )
            )
            
            Row(
                modifier = Modifier.padding(16.dp).weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Progress
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.size(60.dp),
                        color = color.copy(alpha = 0.1f), // Changed from grey to light goal color
                        strokeWidth = 6.dp,
                        trackColor = color.copy(alpha = 0.1f),
                    )
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(60.dp),
                        color = color,
                        strokeWidth = 6.dp,
                        strokeCap = StrokeCap.Round // Added round caps for better look
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        Text(text = currentAmount, fontWeight = FontWeight.Bold, color = color, fontSize = 14.sp)
                        Text(text = " of $totalAmount", color = TextSecondary, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                     Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DateRange, 
                            contentDescription = null, 
                            tint = TextSecondary, 
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = dueDate, color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String, actionText: String? = null, actionColor: Color = TextSecondary, stripColor: Color = SectionTitleBar, onActionClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(stripColor) // Use the dynamic strip color
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
        
        if (actionText != null) {
             Text(
                text = actionText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = actionColor,
                modifier = Modifier.clickable { }
            )
        }
    }
}



@Composable
fun CommoditiesSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CommodityCard(
            name = "Gold",
            price = "₹6,250 / gm",
            trend = "+0.45%",
            isPositive = true,
            icon = Icons.Rounded.MonetizationOn,
            color = Color(0xFFFFD700), // Gold
            modifier = Modifier.weight(1f)
        )
        CommodityCard(
            name = "Silver",
            price = "₹76.50 / gm",
            trend = "-0.12%",
            isPositive = false,
            // Using Savings icon as placeholder for Silver if a better one isn't available, or maybe Diamond/jewelry
            icon = Icons.Rounded.Savings, 
            color = Color(0xFFC0C0C0), // Silver
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun CommodityCard(
    name: String,
    price: String,
    trend: String,
    isPositive: Boolean,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = Modifier
            .then(modifier)
            .clickable { },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Icon Box
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = name,
                        tint = color.darker(0.3f), // Slightly darker for icon visibility
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Trend Pill
                Surface(
                    color = if (isPositive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isPositive) Icons.Rounded.TrendingUp else Icons.Rounded.TrendingDown,
                            contentDescription = null,
                            tint = if (isPositive) Color(0xFF4CAF50) else Color(0xFFE57373),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = trend,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPositive) Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = name,
                fontSize = 14.sp,
                color = Color(0xFF757575),
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = price,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
        }
    }
}

// Extension to darken color for icons
fun Color.darker(factor: Float): Color {
    return Color(
        red = (this.red * (1 - factor)).coerceIn(0f, 1f),
        green = (this.green * (1 - factor)).coerceIn(0f, 1f),
        blue = (this.blue * (1 - factor)).coerceIn(0f, 1f),
        alpha = this.alpha
    )
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}
