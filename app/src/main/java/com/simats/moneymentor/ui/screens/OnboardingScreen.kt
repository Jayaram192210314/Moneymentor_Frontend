package com.simats.moneymentor.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
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
    
    // Smoothly animate colors for the mesh gradient
    val primaryColor by animateColorAsState(
        targetValue = when(currentPage) {
            0 -> Onboarding1Primary
            1 -> Onboarding2Primary
            else -> Onboarding3Primary
        },
        animationSpec = tween(durationMillis = 1000),
        label = "primaryColor"
    )

    val secondaryColor by animateColorAsState(
        targetValue = when(currentPage) {
            0 -> Onboarding1Bg
            1 -> Onboarding2Bg
            else -> Onboarding3Bg
        },
        animationSpec = tween(durationMillis = 1000),
        label = "secondaryColor"
    )
    
    val gradientBrush = when(currentPage) {
        0 -> androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(GreenGradientStart, GreenGradientEnd))
        1 -> androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(BrightBlueGradientStart, BrightBlueGradientEnd))
        else -> androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(OrangeGradientStart, OrangeGradientEnd))
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(secondaryColor)
    ) {
        // 1. Dynamic Mesh Gradient Background
        MeshGradientBackground(primaryColor = primaryColor)
        
        // 2. Interactive Particle System
        ParticleSystem(primaryColor = primaryColor, pagerOffset = pagerState.currentPageOffsetFraction)

        // 3. Ultra-subtle Grain Texture
        GrainOverlay()

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Skip Button - Ultra elegant
            AnimatedVisibility(
                visible = pagerState.currentPage < 2,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Text(
                        text = "Skip",
                        color = primaryColor,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier
                            .graphicsLayer {
                                shadowElevation = 8f
                                shape = RoundedCornerShape(30.dp)
                                clip = true
                            }
                            .background(Color.White.copy(alpha = 0.4f))
                            .clickable { onFinished() }
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                
                OnboardingPageContent(
                    page = page, 
                    primaryColor = primaryColor,
                    isActive = pagerState.currentPage == page,
                    pageOffset = pageOffset
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // 4. Enhanced Fluid Indicators
            Row(
                modifier = Modifier.padding(bottom = 40.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { iteration ->
                    val isSelected = pagerState.currentPage == iteration
                    val width by animateDpAsState(
                        targetValue = if (isSelected) 40.dp else 12.dp,
                        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow),
                        label = "indicatorWidth"
                    )
                    val color by animateColorAsState(
                        targetValue = if (isSelected) primaryColor else primaryColor.copy(alpha = 0.15f),
                        label = "indicatorColor"
                    )
                    
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .height(10.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }
            
            // 5. Ultra-Premium Interactive Button
            GlowButton(
                onClick = {
                    if (pagerState.currentPage < 2) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        onFinished()
                    }
                },
                primaryColor = primaryColor,
                gradientBrush = gradientBrush,
                isLastPage = currentPage == 2
            )

            Text(
                text = "Powered by SIMATS ENGINEERING",
                fontSize = 12.sp,
                fontWeight = FontWeight.W800,
                color = primaryColor.copy(alpha = 0.6f),
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }
    }
}

@Composable
fun MeshGradientBackground(primaryColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "mesh")
    
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Simulating a morphing mesh with multiple blurred gradients
        val offset1 = androidx.compose.ui.geometry.Offset(
            x = w * 0.5f + (w * 0.4f * kotlin.math.cos(time * 2 * Math.PI)).toFloat(),
            y = h * 0.3f + (h * 0.2f * kotlin.math.sin(time * 2 * Math.PI)).toFloat()
        )
        val offset2 = androidx.compose.ui.geometry.Offset(
            x = w * 0.2f + (w * 0.3f * kotlin.math.sin(time * 2.5 * Math.PI)).toFloat(),
            y = h * 0.7f + (h * 0.25f * kotlin.math.cos(time * 1.8 * Math.PI)).toFloat()
        )

        drawCircle(
            brush = androidx.compose.ui.graphics.Brush.radialGradient(
                colors = listOf(primaryColor.copy(alpha = 0.12f), Color.Transparent),
                center = offset1,
                radius = w * 0.9f
            )
        )
        drawCircle(
            brush = androidx.compose.ui.graphics.Brush.radialGradient(
                colors = listOf(primaryColor.copy(alpha = 0.1f), Color.Transparent),
                center = offset2,
                radius = w * 0.8f
            )
        )
    }
}

@Composable
fun ParticleSystem(primaryColor: Color, pagerOffset: Float) {
    val particles = remember { List(25) { Particle() } }
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { p ->
            val x = (p.x + pagerOffset * p.speed * 200f) % size.width
            val y = (p.y + phase * size.height) % size.height
            drawCircle(
                color = primaryColor.copy(alpha = p.alpha * 0.3f),
                radius = p.size,
                center = androidx.compose.ui.geometry.Offset(if (x < 0) size.width + x else x, y)
            )
        }
    }
}

class Particle(
    var x: Float = (0..2000).random().toFloat(),
    var y: Float = (0..2000).random().toFloat(),
    var size: Float = (2..8).random().toFloat(),
    var alpha: Float = (1..10).random().toFloat() / 10f,
    var speed: Float = (1..5).random().toFloat() / 10f
)

@Composable
fun GrainOverlay() {
    // Simple way to add a premium "tactile" texture using a very light noise pattern or subtle lines
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().alpha(0.015f)) {
        val density = 10
        for (i in 0 until size.width.toInt() step density) {
            for (j in 0 until size.height.toInt() step density) {
                drawCircle(Color.Black, radius = 1f, center = androidx.compose.ui.geometry.Offset(i.toFloat(), j.toFloat()))
            }
        }
    }
}

@Composable
fun GlowButton(
    onClick: () -> Unit,
    primaryColor: Color,
    gradientBrush: androidx.compose.ui.graphics.Brush,
    isLastPage: Boolean
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "buttonScale"
    )

    Box(
        modifier = Modifier
            .padding(horizontal = 32.dp, vertical = 24.dp)
            .fillMaxWidth()
            .height(72.dp)
            .scale(scale)
            .graphicsLayer {
                shadowElevation = 30f
                shape = RoundedCornerShape(20.dp)
                clip = true
            }
            .background(gradientBrush)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Shine/Reflection effect on button
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val sheenWidth = size.width * 0.5f
            drawRect(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.15f), Color.Transparent),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(size.width, size.height)
                )
            )
        }

        AnimatedContent(
            targetState = isLastPage,
            transitionSpec = {
                (fadeIn(animationSpec = tween(500)) + scaleIn()).togetherWith(fadeOut(animationSpec = tween(500)) + scaleOut())
            },
            label = "buttonText"
        ) { pageIsLast ->
            Text(
                text = if (pageIsLast) "GET STARTED" else "NEXT PAGE",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
fun OnboardingPageContent(
    page: Int, 
    primaryColor: Color, 
    isActive: Boolean,
    pageOffset: Float
) {
    val items = when(page) {
        0 -> Triple("Smart Budgeting", "Track your expenses effortlessly and set realistic budgets.", R.drawable.ic_chart_pie)
        1 -> Triple("AI Financial Advisor", "Get personalized financial advice powered by AI.", R.drawable.ic_robot)
        else -> Triple("Achieve Your Goals", "Set goals and track your progress with milestones.", R.drawable.ic_target)
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "floating")
    val dy by infiniteTransition.animateFloat(
        initialValue = -20f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dy"
    )

    // Sheen/Refraction animation
    val sheenX by infiniteTransition.animateFloat(
        initialValue = -500f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sheen"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 40.dp)
    ) {
        // Refractive Glass Icon Container
        Box(
            modifier = Modifier
                .size(320.dp)
                .graphicsLayer {
                    translationY = dy
                    translationX = pageOffset * 250f
                    rotationZ = pageOffset * 15f
                    rotationY = pageOffset * 20f
                }
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.5f))
                .padding(4.dp)
                .background(Color.White.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Simulated Refraction
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.3f), Color.Transparent),
                        start = androidx.compose.ui.geometry.Offset(sheenX, 0f),
                        end = androidx.compose.ui.geometry.Offset(sheenX + 300f, size.height)
                    )
                )
            }

            Image(
                painter = painterResource(id = items.third),
                contentDescription = null,
                modifier = Modifier
                    .size(180.dp)
                    .scale(1f - kotlin.math.abs(pageOffset) * 0.2f)
                    .alpha(1f - kotlin.math.abs(pageOffset) * 0.5f),
                colorFilter = ColorFilter.tint(primaryColor)
            )
        }
        
        Spacer(modifier = Modifier.height(56.dp))
        
        // Advanced Animated Typography
        val letterSpacingVal by animateFloatAsState(
            targetValue = if (isActive) 2f else 6f,
            animationSpec = tween(800),
            label = "spacing"
        )
        
        Text(
            text = items.first.uppercase(),
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = TextPrimary,
            modifier = Modifier.graphicsLayer {
                translationX = pageOffset * 150f
                alpha = 1f - kotlin.math.abs(pageOffset)
            },
            textAlign = TextAlign.Center,
            letterSpacing = letterSpacingVal.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = items.second,
            fontSize = 18.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 30.sp,
            modifier = Modifier.graphicsLayer {
                translationX = pageOffset * 80f
                alpha = 1f - kotlin.math.abs(pageOffset)
            }
        )
    }
}
