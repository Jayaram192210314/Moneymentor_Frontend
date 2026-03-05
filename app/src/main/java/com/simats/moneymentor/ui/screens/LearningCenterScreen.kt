package com.simats.moneymentor.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.CurrencyBitcoin
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.FactCheck
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.QueryStats
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.simats.moneymentor.ui.theme.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.moneymentor.ui.theme.MoneyMentorTheme

import com.simats.moneymentor.data.LearningArticle
import com.simats.moneymentor.data.LearningLevel
import com.simats.moneymentor.data.LearningModule
import com.simats.moneymentor.data.ModuleStatus
import com.simats.moneymentor.data.ArticleType

@Composable
fun LearningCenterScreen(
    onArticleClick: (Int, LearningArticle) -> Unit = { _, _ -> },
    onQuizClick: (Int) -> Unit = {}
) {
    // Use Repository Data
    val allModules = com.simats.moneymentor.data.LearningRepository.modules
    
    var serverProgress by remember { mutableStateOf<com.simats.moneymentor.data.model.ProgressResponse?>(null) }
    var isSyncing by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        if (!isSyncing) {
            isSyncing = true
            try {
                com.simats.moneymentor.data.LearningRepository.syncProgressFromServer()
                val userId = com.simats.moneymentor.data.LearningRepository.currentUserId
                if (userId > 0) {
                    serverProgress = com.simats.moneymentor.data.network.RetrofitClient.learningService.getProgress(userId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isSyncing = false
            }
        }
    }
    
    // Filter State
    var selectedLevel by remember { mutableStateOf<com.simats.moneymentor.data.LearningLevel?>(null) }
    // Expansion State
    var expandedModuleId by remember { mutableStateOf<Int?>(null) }
    
    // Filtered Modules
    val displayedModules = if (selectedLevel == null) {
        allModules
    } else {
        allModules.filter { it.level == selectedLevel }
    }
    
    val groupedModules = displayedModules.groupBy { it.level }

    // Simplified Progress Stats using Repository
    val totalArticles = serverProgress?.total ?: com.simats.moneymentor.data.LearningRepository.totalArticlesCount
    val completedArticles = serverProgress?.completed ?: com.simats.moneymentor.data.LearningRepository.completedArticlesCount
    val overallProgress = serverProgress?.progressPercent?.let { (it / 100.0).toFloat() } ?: com.simats.moneymentor.data.LearningRepository.overallProgressValue
    
    // Calculate Level Progress using Repository
    fun getLevelProgress(level: com.simats.moneymentor.data.LearningLevel): Int {
        val levelArticles = allModules.filter { it.level == level }.flatMap { it.articles }
        val levelTotal = levelArticles.filter { it.type != com.simats.moneymentor.data.ArticleType.Quiz }.size
        val levelCompleted = levelArticles.filter { it.type != com.simats.moneymentor.data.ArticleType.Quiz && it.isCompleted }.size
        return if (levelTotal > 0) ((levelCompleted.toFloat() / levelTotal) * 100).toInt() else 0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgVioletWhite)
    ) {
        // Scrollable Content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Header & Progress Card
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    LearningHeader()
                    Spacer(modifier = Modifier.height(16.dp)) 
                    OverallProgressCard(
                        totalArticles = totalArticles,
                        completedArticles = completedArticles,
                        overallProgress = overallProgress,
                        getLevelProgress = ::getLevelProgress
                    )
                     Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Filter Chips
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedLevel == null,
                            onClick = { selectedLevel = null },
                            label = { Text("All") },
                            leadingIcon = { Icon(Icons.Rounded.FilterList, contentDescription = null) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF7C4DFF),
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White,
                                labelColor = Color.Gray // Grey when unselected
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedLevel == null,
                                selectedBorderColor = Color(0xFF7C4DFF),
                                selectedBorderWidth = 1.dp
                            )
                        )
                    }
                    items(com.simats.moneymentor.data.LearningLevel.values()) { level ->
                        FilterChip(
                            label = { Text(level.displayName.substringAfter(": ")) },
                            selected = selectedLevel == level,
                            onClick = { selectedLevel = level },
                            leadingIcon = { Icon(level.icon, contentDescription = null, tint = if (selectedLevel == level) Color.White else level.color) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF7C4DFF),
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White,
                                labelColor = Color.Gray
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedLevel == level,
                                selectedBorderColor = Color(0xFF7C4DFF),
                                selectedBorderWidth = 1.dp
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Modules Grouped by Level
            groupedModules.forEach { (level, levelModules) ->
                 item {
                     Row(
                         verticalAlignment = Alignment.CenterVertically,
                         modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 16.dp)
                     ) {
                         Icon(
                             imageVector = level.icon,
                             contentDescription = null,
                             tint = level.color,
                             modifier = Modifier.size(28.dp)
                         )
                         Spacer(modifier = Modifier.width(12.dp))
                         Text(
                             text = level.displayName,
                             fontSize = 20.sp,
                             fontWeight = FontWeight.Bold,
                             color = Color(0xFF1F1F1F)
                         )
                     }
                 }
                
                items(levelModules) { module ->
                     ModuleItem(
                         module = module,
                         isExpanded = module.id == expandedModuleId,
                         onExpandClick = { 
                             expandedModuleId = if (expandedModuleId == module.id) null else module.id 
                         },
                         onArticleClick = { article -> onArticleClick(module.id, article) },
                         onQuizClick = { onQuizClick(module.id) }
                     )
                     Spacer(modifier = Modifier.height(16.dp))
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

// ... helper composables like FilterChip, LearningHeader, OverallProgressCard remain mostly the same but need to handle new types ...

@Composable
fun ModuleItem(
    module: com.simats.moneymentor.data.LearningModule, 
    isExpanded: Boolean, 
    onExpandClick: () -> Unit, 
    onArticleClick: (LearningArticle) -> Unit,
    onQuizClick: () -> Unit
) {
    val arrowRotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f)

    // Separate Content and Quiz (Quiz removed from UI)
    val contentArticles = module.articles.filter { it.type != com.simats.moneymentor.data.ArticleType.Quiz }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clickable { onExpandClick() }
            .animateContentSize() 
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Color Strip (Left)
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(module.color)
            )

            Column(modifier = Modifier.weight(1f)) {
                // Header Content
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(module.color.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                module.icon,
                                contentDescription = null,
                                tint = module.color,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Module ${module.id}", 
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = module.color,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                // Article Count (Excluding Quiz)
                                Surface(
                                    color = module.color.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "${contentArticles.size} Articles",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = module.color,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = module.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF37474F)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = module.subtitle,
                                fontSize = 12.sp,
                                color = Color(0xFF78909C),
                                maxLines = 1
                            )
                        }

                        // Expand Icon
                        Icon(
                            Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "Expand",
                            tint = Color(0xFFCFD8DC),
                            modifier = Modifier.rotate(arrowRotation)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress Bar
                    LinearProgressIndicator(
                        progress = { module.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = module.color,
                        trackColor = module.color.copy(alpha = 0.2f),
                    )

                    Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.End) {
                        Text(
                            "${(module.progress * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = module.color
                        )
                    }
                }

                // Articles List (Expanded)
                if (isExpanded) {
                    Column(modifier = Modifier.padding(start = 22.dp, end = 16.dp, bottom = 16.dp)) { 
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Render Content Articles
                        contentArticles.forEach { article ->
                            ArticleItem(article, module.color, onClick = { onArticleClick(article) })
                        }

                        // Render Quiz Article (if any)
                        module.articles.find { it.type == com.simats.moneymentor.data.ArticleType.Quiz }?.let { quizArticle ->
                            Spacer(modifier = Modifier.height(8.dp))
                            QuizItem(
                                article = quizArticle,
                                isUnlocked = com.simats.moneymentor.data.LearningRepository.isQuizUnlocked(module.id),
                                onClick = onQuizClick
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuizItem(article: com.simats.moneymentor.data.LearningArticle, isUnlocked: Boolean, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (isUnlocked) Color(0xFFFFF3E0) else Color(0xFFEEEEEE)), // Grey if locked
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth().clickable(enabled = isUnlocked) { onClick() }
    ) {
         Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isUnlocked) Color(0xFFFFE0B2) else Color(0xFFBDBDBD)), 
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isUnlocked) Icons.Rounded.Quiz else Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = if (isUnlocked) Color(0xFFFF9800) else Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isUnlocked) article.title else "Quiz Locked",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isUnlocked) Color(0xFFE65100) else Color(0xFF757575)
                )
                 Spacer(modifier = Modifier.height(2.dp))
                 Text(
                    text = if (isUnlocked) article.duration else "Complete all articles to unlock", 
                    fontSize = 12.sp,
                    color = if (isUnlocked) Color(0xFFEF6C00) else Color(0xFF9E9E9E)
                )
            }
             if (isUnlocked) {
                 Icon(
                    Icons.Rounded.ChevronRight,
                    null,
                    tint = Color(0xFFFF9800)
                )
             }
        }
    }
}


@Composable
fun LearningHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp) // Resized to match Goals to 80dp as requested
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF6200EA), // Deep Violet
                        Color(0xFF7C4DFF)  // Lighter Violet
                    )
                )
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp), // Adjust padding for smaller height
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Container
            Box(
                modifier = Modifier
                    .size(40.dp) // Slightly smaller icon container
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                 Icon(
                    imageVector = Icons.Rounded.AccountBalanceWallet,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = "Learning Center",
                    fontSize = 20.sp, // Slightly smaller font
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Your financial journey",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun ArticleItem(
    article: com.simats.moneymentor.data.LearningArticle,
    moduleColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (article.isCompleted) Color(0xFFE8F5E9) else moduleColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (article.isCompleted) Icons.Rounded.Check else Icons.Rounded.Description,
                contentDescription = null,
                tint = if (article.isCompleted) Color(0xFF4CAF50) else moduleColor,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = article.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Text(
                text = article.duration,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun OverallProgressCard(
    totalArticles: Int,
    completedArticles: Int,
    overallProgress: Float,
    getLevelProgress: (LearningLevel) -> Int
) {
    val scope = rememberCoroutineScope()
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            // Removed negative offset to create padding between header and progress card
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF6200EA), // Deep Violet
                                Color(0xFF7C4DFF)  // Lighter Violet
                            )
                        )
                    )
            ) {
                // Watermark Icon
                 Icon(
                    imageVector = Icons.Rounded.MenuBook,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.05f),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(180.dp)
                        .offset(x = 40.dp, y = 20.dp)
                )

                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "OVERALL PROGRESS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.8f),
                            letterSpacing = 1.sp
                        )
                        
                        // Sync Button
                        IconButton(
                            onClick = {
                                // Trigger Manual Reset & Sync
                                scope.launch(Dispatchers.IO) {
                                    com.simats.moneymentor.data.LearningRepository.syncProgressFromServer()
                                }
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "Sync with Server",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Main Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${(overallProgress * 100).toInt()}%",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Column(horizontalAlignment = Alignment.End) {
                             Text(
                                text = "$completedArticles of $totalArticles",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = "Articles Done",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Progress Bar
                    LinearProgressIndicator(
                        progress = { overallProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Breakdown Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Beginner
                        LevelStat("BEGINNER", getLevelProgress(LearningLevel.Beginner))
                        LevelStat("INTERMEDIATE", getLevelProgress(LearningLevel.Intermediate))
                        LevelStat("ADVANCED", getLevelProgress(LearningLevel.Advanced))
                        LevelStat("EXPERT", getLevelProgress(LearningLevel.Expert))
                    }
                }
            }
        }
    }
}

@Composable
fun LevelStat(label: String, percent: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
         Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$percent%",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
