package com.simats.moneymentor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.moneymentor.ui.theme.*
import com.simats.moneymentor.ui.viewmodels.InsightViewModel
import com.simats.moneymentor.ui.viewmodels.InsightState
import com.simats.moneymentor.data.model.DailyTipResponse
import com.simats.moneymentor.data.model.DailyTermResponse

@Composable
fun DailyInsightsScreen(
    viewModel: InsightViewModel,
    onBackClick: () -> Unit = {}
) {
    val tipState by viewModel.tipState.collectAsState()
    val termState by viewModel.termState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchDailyInsights()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB)) // Light gray background
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFFEF5350), Color(0xFFFFCA28)) // Red to Yellow/Orange
                    )
                )
                .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Daily Insights",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Learn something new",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Tip of the Day Card
            TipOfTheDayCard(tipState)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Term of the Day Card
            TermOfTheDayCard(termState)
            
            Spacer(modifier = Modifier.height(24.dp))
            
        }
    }
}

@Composable
fun TipOfTheDayCard(state: InsightState<DailyTipResponse>) {
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFEC407A), Color(0xFFFF7043)) // Pink to Orange
                    )
                )
        ) {
            // Background Lightbulb Watermark
            Icon(
                imageVector = Icons.Rounded.Lightbulb,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.1f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 20.dp, y = (-20).dp)
                    .size(180.dp)
            )

            Column(modifier = Modifier.padding(24.dp)) {
                // Badge
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(100.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "TIP OF THE DAY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                when (state) {
                    is InsightState.Loading -> {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    }
                    is InsightState.Success -> {
                        Text(
                            text = "Smart Tip",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = state.data.dailyTip ?: "No Tip content available",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            lineHeight = 24.sp
                        )
                    }
                    is InsightState.Error -> {
                        Text(text = "Error: ${state.message}", color = Color.White)
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
fun TermOfTheDayCard(state: InsightState<DailyTermResponse>) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, Color(0xFFFFE0B2), RoundedCornerShape(24.dp)) // Light Orange Border
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "TERM OF THE DAY",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF9800), // Orange
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            when (state) {
                is InsightState.Loading -> {
                    CircularProgressIndicator(color = Color(0xFFFF9800), modifier = Modifier.size(24.dp))
                }
                is InsightState.Success -> {
                    Text(
                        text = "Daily Term", // Default title
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF263238)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = state.data.dailyTerm ?: "No Definition available",
                        fontSize = 15.sp,
                        color = Color(0xFF546E7A),
                        lineHeight = 22.sp
                    )
                }
                is InsightState.Error -> {
                    Text(text = "Error: ${state.message}", color = Color.Red)
                }
                else -> {}
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DailyInsightsPreview() {
    val insightService = com.simats.moneymentor.data.network.RetrofitClient.insightService
    val insightRepository = com.simats.moneymentor.data.InsightRepository(insightService)
    val insightViewModel = com.simats.moneymentor.ui.viewmodels.InsightViewModel(insightRepository)
    DailyInsightsScreen(viewModel = insightViewModel)
}
