package com.simats.moneymentor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.moneymentor.ui.theme.*

@Composable
fun ArticleDetailScreen(
    article: com.simats.moneymentor.data.LearningArticle,
    moduleName: String,
    moduleId: Int,
    moduleColor: Color,
    currentIndex: Int,
    totalArticles: Int,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onMarkAsReadClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F6FA))
    ) {
        // 1. Header with Gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp) // Height including status bar area
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(moduleColor, moduleColor.copy(alpha = 0.8f))
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = moduleName,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Article ${currentIndex + 1} of $totalArticles",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // 2. Content Area (Scrollable) - weight(1f) to take available space
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(24.dp)
        ) {
            // Article Title
            Text(
                text = article.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D3436)
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            // Metadata Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.AccessTime,
                    contentDescription = null,
                    tint = Color(0xFFB2BEC3),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = article.duration,
                    color = Color(0xFFB2BEC3),
                    fontSize = 12.sp
                )


                Spacer(modifier = Modifier.width(16.dp))

                Icon(
                    imageVector = Icons.Rounded.MenuBook,
                    contentDescription = null,
                    tint = moduleColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Module $moduleId", 
                    color = moduleColor,
                    fontSize = 12.sp
                )
                
                Spacer(modifier = Modifier.width(16.dp))

                if (article.isCompleted) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF00C853), // Green
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Read",
                        color = Color(0xFF00C853),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Body Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    val annotatedContent = parseMarkdown(article.content)
                    Text(
                        text = annotatedContent,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        color = Color(0xFF535c68)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // "Mark as Read" Button
             Button(
                onClick = onMarkAsReadClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (article.isCompleted) Color(0xFFE8F5E9) else Color(0xFF4CAF50), // Green when not completed
                    contentColor = if (article.isCompleted) Color(0xFF00C853) else Color.White // White when not completed
                ),
                border = BorderStroke(1.dp, if (article.isCompleted) Color(0xFF00C853).copy(alpha=0.3f) else Color(0xFF4CAF50).copy(alpha=0.3f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                 Icon(if (article.isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.Check, null, modifier = Modifier.size(20.dp), tint = if (article.isCompleted) Color(0xFF00C853) else Color.White)
                 Spacer(modifier = Modifier.width(8.dp))
                Text(if (article.isCompleted) "Read Again" else "Mark as Read", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (article.isCompleted) Color(0xFF00C853) else Color.White)
            }
            
            Spacer(modifier = Modifier.height(24.dp)) // Extra space at bottom of scroll
        }

        // 3. Bottom Navigation Footer (Fixed)
        Surface(
            color = Color.White,
            shadowElevation = 16.dp, // Add shadow for "floating" effect
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous Button
                if (currentIndex > 0) {
                    OutlinedButton(
                        onClick = onPreviousClick,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                         colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF9C2CF3)),
                         border = BorderStroke(1.dp, Color(0xFF9C2CF3).copy(alpha=0.2f))
                    ) {
                        Icon(Icons.Rounded.ArrowBack, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Previous")
                    }
                } else {
                     Spacer(modifier = Modifier.weight(1f))
                }
    
                Spacer(modifier = Modifier.width(16.dp))
    
                // Next Button
                Button(
                    onClick = onNextClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = moduleColor,
                        contentColor = Color.White // TASK-1: White font
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(if (currentIndex < totalArticles - 1) "Next" else "Back to Modules", color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Rounded.ArrowForward, null, modifier = Modifier.size(18.dp), tint = Color.White)
                }
            }
        }
    }
}

// Simple Helper to parse bold (**text**) and bullet points
@Composable
fun parseMarkdown(text: String): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        val lines = text.split("\n")
        lines.forEach { line ->
            val trimmedLine = line.trim()
            if (trimmedLine.startsWith("•")) {
                 withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.Black)) {
                    append("• ")
                }
                append(parseBold(trimmedLine.substring(1).trim()))
            } else if (trimmedLine.startsWith("**")) {
                 // Line starts with bold, likely a header
                 append(parseBold(trimmedLine))
            } else {
                append(parseBold(line))
            }
            append("\n")
        }
    }
}

@Composable
fun parseBold(text: String): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        val parts = text.split("**")
        parts.forEachIndexed { index, part ->
            if (index % 2 == 1) { // Odd indices are inside **...**
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.Black)) {
                    append(part)
                }
            } else {
                append(part)
            }
        }
    }
}
