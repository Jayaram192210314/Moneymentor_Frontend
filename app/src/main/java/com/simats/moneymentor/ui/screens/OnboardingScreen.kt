package com.simats.moneymentor.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.moneymentor.R
import com.simats.moneymentor.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    val currentPage = pagerState.currentPage
    val backgroundColor = when(currentPage) {
        0 -> Onboarding1Bg
        1 -> Onboarding2Bg
        else -> Onboarding3Bg
    }
    val primaryColor = when(currentPage) {
        0 -> Onboarding1Primary
        1 -> Onboarding2Primary
        else -> Onboarding3Primary
    }
    
    val gradientBrush = when(currentPage) {
        0 -> androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(GreenGradientStart, GreenGradientEnd))
        1 -> androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(BrightBlueGradientStart, BrightBlueGradientEnd))
        else -> androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(OrangeGradientStart, OrangeGradientEnd))
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Skip Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                Text(
                    text = "Skip",
                    color = primaryColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable { onFinished() }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                OnboardingPageContent(page = page, primaryColor = primaryColor)
            }

            Spacer(modifier = Modifier.weight(1f))

            // Indicators
            Row(
                modifier = Modifier
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(3) { iteration ->
                    val color = if (pagerState.currentPage == iteration) primaryColor else Color.LightGray.copy(alpha = 0.5f)
                    val width = if (pagerState.currentPage == iteration) 32.dp else 8.dp
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .height(8.dp)
                            .width(width)
                            .clip(RoundedCornerShape(4.dp))
                            .background(color)
                    )
                }
            }
            
            // Next/Get Started Button
            Button(
                onClick = {
                    if (pagerState.currentPage < 2) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        onFinished()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 32.dp)
                    .height(56.dp)
                    .background(gradientBrush, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (currentPage == 2) "Get Started" else "Next",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Text(
                text = "Powered by SIMATS ENGINEERING",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor,
                modifier = Modifier
                    .padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
fun OnboardingPageContent(page: Int, primaryColor: Color) {
    val title = when(page) {
        0 -> "Smart Budgeting"
        1 -> "AI Financial Advisor"
        else -> "Achieve Your Goals"
    }
    
    val description = when(page) {
        0 -> "Track your expenses effortlessly and set realistic budgets that work for your lifestyle."
        1 -> "Get personalized financial advice powered by AI to make smarter money decisions."
        else -> "Set financial goals and track your progress with visual milestones and reminders."
    }
    
    val iconRes = when(page) {
        0 -> R.drawable.ic_chart_pie
        1 -> R.drawable.ic_robot
        else -> R.drawable.ic_target
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 32.dp)
    ) {
        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(140.dp),
                colorFilter = ColorFilter.tint(primaryColor)
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = description,
            fontSize = 16.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}
