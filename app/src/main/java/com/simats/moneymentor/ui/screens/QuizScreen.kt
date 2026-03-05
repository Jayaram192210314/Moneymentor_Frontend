package com.simats.moneymentor.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.moneymentor.data.LearningRepository
import com.simats.moneymentor.data.QuizQuestion
import com.simats.moneymentor.ui.theme.MoneyMentorTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    moduleId: Int,
    onBack: () -> Unit
) {
    val module = remember { LearningRepository.getModule(moduleId) }
    val quiz = remember { com.simats.moneymentor.data.QuizRepository.getQuizByModuleId(moduleId) }
    
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableStateOf(0) }
    var isQuizFinished by remember { mutableStateOf(false) }
    var showExplanation by remember { mutableStateOf(false) }

    val goldenGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFF59E0B), // Amber 500
            Color(0xFFEAB308), // Yellow 500
            Color(0xFFFBBF24)  // Amber 400
        )
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        module?.title ?: "Module Quiz",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FA))
        ) {
            if (!isQuizFinished && quiz != null && currentQuestionIndex < quiz.questions.size) {
                val currentQuestion = quiz.questions[currentQuestionIndex]
                
                // Progress Bar
                LinearProgressIndicator(
                    progress = (currentQuestionIndex + 1).toFloat() / quiz.questions.size,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = Color(0xFFF59E0B),
                    trackColor = Color(0xFFFEF3C7)
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        "Question ${currentQuestionIndex + 1} of ${quiz.questions.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.Gray
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        currentQuestion.question,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    currentQuestion.options.forEachIndexed { index, option ->
                        QuizOptionItem(
                            text = option,
                            isSelected = selectedOptionIndex == index,
                            isCorrect = if (showExplanation) index == currentQuestion.correctAnswerIndex else null,
                            isWrong = if (showExplanation) selectedOptionIndex == index && index != currentQuestion.correctAnswerIndex else null,
                            onClick = {
                                if (!showExplanation) {
                                    selectedOptionIndex = index
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Button(
                        onClick = {
                            if (!showExplanation) {
                                if (selectedOptionIndex != null) {
                                    if (selectedOptionIndex == currentQuestion.correctAnswerIndex) {
                                        score++
                                    }
                                    showExplanation = true
                                }
                            } else {
                                if (currentQuestionIndex < quiz.questions.size - 1) {
                                    currentQuestionIndex++
                                    selectedOptionIndex = null
                                    showExplanation = false
                                } else {
                                    isQuizFinished = true
                                    // Mark quiz as completed in repository
                                    module?.articles?.find { it.type == com.simats.moneymentor.data.ArticleType.Quiz }?.let {
                                        LearningRepository.markArticleAsCompleted(moduleId, it.id)
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = selectedOptionIndex != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E3A8A),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            if (!showExplanation) "Check Answer" else if (currentQuestionIndex < quiz.questions.size - 1) "Next Question" else "See Results",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            } else if (isQuizFinished) {
                // Result View
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            if (score >= 3) Icons.Rounded.EmojiEvents else Icons.Rounded.School,
                            contentDescription = null,
                            modifier = Modifier.size(120.dp),
                            tint = if (score >= 3) Color(0xFFF59E0B) else Color.Gray
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            if (score >= 3) "Congratulations!" else "Keep Learning!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            "You scored $score out of ${quiz?.questions?.size ?: 5}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                        
                        Spacer(modifier = Modifier.height(48.dp))
                        
                        Button(
                            onClick = onBack,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1E3A8A),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Finish", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // No Quiz Content
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No quiz content available for this module yet.")
                }
            }
        }
    }
}

@Composable
fun QuizOptionItem(
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean?,
    isWrong: Boolean?,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isCorrect == true -> Color(0xFFDCFCE7)
        isWrong == true -> Color(0xFFFEE2E2)
        isSelected -> Color(0xFFFEF3C7)
        else -> Color.White
    }
    
    val borderColor = when {
        isCorrect == true -> Color(0xFF22C55E)
        isWrong == true -> Color(0xFFEF4444)
        isSelected -> Color(0xFFF59E0B)
        else -> Color(0xFFE5E7EB)
    }

    val icon = when {
        isCorrect == true -> Icons.Rounded.CheckCircle
        isWrong == true -> Icons.Rounded.Cancel
        else -> null
    }

    val iconTint = when {
        isCorrect == true -> Color(0xFF22C55E)
        isWrong == true -> Color(0xFFEF4444)
        else -> Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = Color(0xFF374151)
        )
        
        if (icon != null) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun QuizScreenPreview() {
    MoneyMentorTheme {
        QuizScreen(
            moduleId = 1,
            onBack = {}
        )
    }
}
