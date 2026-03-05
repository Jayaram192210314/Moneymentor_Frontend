package com.simats.moneymentor.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.animateFloatAsState
import com.simats.moneymentor.ui.theme.*

@Composable
fun AnimatedBottomNavigation(
    currentTab: String,
    onTabSelected: (String) -> Unit
) {
    val items = listOf(
        Triple("Home", Icons.Rounded.Home, Color(0xFFFF9800)),  // Orange
        Triple("Learn", Icons.Rounded.School, Color(0xFF9C27B0)), // Purple
        Triple("Chat", Icons.Rounded.ChatBubble, Color(0xFF00BFA5)), // Teal
        Triple("Goals", Icons.Rounded.PieChart, GoalPrimary),     // Orange
        Triple("Tools", Icons.Rounded.Calculate, Color(0xFF2196F3)) // Light Blue
    )

    val selectedIndex = items.indexOfFirst { it.first == currentTab }
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val tabWidth = screenWidth / items.size

    // Spring Animation Spec
    val springSpec = spring<androidx.compose.ui.unit.Dp>(
        dampingRatio = 0.8f,
        stiffness = 350f
    )

    // Animated Offset for the Pill
    val indicatorOffset by animateDpAsState(
        targetValue = tabWidth * selectedIndex,
        animationSpec = springSpec,
        label = "pillOffset"
    )

    // Animated Color for the Pill
    val targetColor = items.getOrNull(selectedIndex)?.third ?: Color.Blue
    val indicatorColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = spring<Color>(stiffness = Spring.StiffnessLow),
        label = "pillColor"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Color.White)
    ) {
        // Sliding Pill (Background Layer)
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(tabWidth)
                .fillMaxHeight()
                .padding(top = 12.dp), // Align with icons
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(indicatorColor.copy(alpha = 0.15f))
            )
        }

        // Foreground Layer (Icons & Labels)
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, (label, icon, color) ->
                val selected = index == selectedIndex
                
                // Color Transition for Icon/Text
                val itemColor by animateColorAsState(
                    targetValue = if (selected) color else Color(0xFF94A3B8), // State-400 for inactive
                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                    label = "itemColor-$label"
                )

                val isHome = label == "Home"
                val gradientAlpha by animateFloatAsState(
                    targetValue = if (selected && isHome) 1f else 0f,
                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                    label = "gradientAlpha"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null // Disable ripple for cleaner look
                        ) { onTabSelected(label) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val iconModifier = Modifier
                            .size(24.dp)
                            .let { modifier ->
                                if (isHome) {
                                    modifier
                                        .graphicsLayer(alpha = 0.99f)
                                        .drawWithCache {
                                            val brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    Color(0xFFFF9800), // Orange
                                                    Color(0xFFFFB74D)  // Light Orange
                                                ),
                                                startY = 0f,
                                                endY = size.height
                                            )
                                            onDrawWithContent {
                                                drawContent()
                                                if (gradientAlpha > 0f) {
                                                    drawRect(
                                                        brush = brush,
                                                        blendMode = BlendMode.SrcAtop,
                                                        alpha = gradientAlpha
                                                    )
                                                }
                                            }
                                        }
                                } else {
                                    modifier
                                }
                            }
                            
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = itemColor,
                            modifier = iconModifier
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            color = itemColor,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
