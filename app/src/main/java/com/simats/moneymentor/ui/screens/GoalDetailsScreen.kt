package com.simats.moneymentor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.moneymentor.data.GoalRepository
import com.simats.moneymentor.ui.theme.*

@Composable
fun GoalDetailsScreen(
    onBackClick: () -> Unit,
    onEditGoalClick: () -> Unit,
    viewModel: com.simats.moneymentor.ui.viewmodels.GoalViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val currentGoalIndex = com.simats.moneymentor.data.GoalRepository.currentGoalIndex
    val currentGoal = if (currentGoalIndex in viewModel.goals.indices) {
        viewModel.goals[currentGoalIndex]
    } else {
        com.simats.moneymentor.data.GoalRepository.getSelectedGoal()
    }
    
    var selectedTab by remember { mutableStateOf("Add") }
    var amount by remember { mutableStateOf("") }

    val uiMessage by viewModel.uiMessage.collectAsState()

    LaunchedEffect(Unit) {
        if (currentGoal.id != 0) {
            viewModel.fetchSingleGoal(currentGoal.id)
        }
    }

    LaunchedEffect(uiMessage) {
        uiMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            if (it.contains("successfully", ignoreCase = true)) {
                amount = ""
            }
            viewModel.clearMessage()
        }
    }

    fun updateSavings() {
        val inputAmount = amount.toDoubleOrNull() ?: 0.0
        if (inputAmount > 0) {
            if (selectedTab == "Add") {
                viewModel.addMoney(currentGoal.id, inputAmount)
            } else {
                viewModel.withdrawMoney(currentGoal.id, inputAmount)
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFFF0FAF9) // Light Teal Background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp) // Taller header to accommodate content
                        .background(Color(0xFF00BFA5)) // Teal
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable { onBackClick() }
                            )
                            
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "Edit",
                                tint = Color.White, // White to match header
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable { onEditGoalClick() }
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Update Savings",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentGoal.name,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 16.sp
                        )
                    }

                     // Floating Icon
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = 40.dp) // Overlap
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00BFA5)) // Teal
                            .border(4.dp, Color(0xFFE0F2F1), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(50.dp)) // Space for overlapping icon

                // Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                ) {
                    // Progress Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Current Progress", color = TextSecondary, fontWeight = FontWeight.Bold)
                                Text("${(currentGoal.progress * 100).toInt()}%", color = Color(0xFF00BFA5), fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { currentGoal.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                color = Color(0xFF1DE9B6),
                                trackColor = Color(0xFFE0F2F1),
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(currentGoal.formattedCurrentAmount, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("of ${currentGoal.formattedTargetAmount}", fontSize = 14.sp, color = TextSecondary, modifier = Modifier.padding(bottom = 2.dp))
                                }
                                
                                val remaining = (currentGoal.targetAmount - currentGoal.currentAmount)
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFF5F6FA), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                     val remainingText = if (remaining > 0) {
                                         if (remaining >= 100000) "↗ ₹%.2fL left".format(remaining / 100000).replace(".00", "")
                                         else "↗ ₹%.0f left".format(remaining)
                                     } else "Goal Reached!"
                                     
                                     Text(
                                         remainingText, 
                                         fontSize = 12.sp, 
                                         color = TextSecondary, 
                                         fontWeight = FontWeight.Bold
                                     )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            // Tabs
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF5F6FA), RoundedCornerShape(12.dp))
                                    .padding(4.dp)
                            ) {
                                TabButton(
                                    text = "Add",
                                    icon = Icons.Rounded.Add,
                                    selected = selectedTab == "Add",
                                    onClick = { selectedTab = "Add" },
                                    modifier = Modifier.weight(1f)
                                )
                                TabButton(
                                    text = "Withdraw",
                                    icon = Icons.Rounded.Remove,
                                    selected = selectedTab == "Withdraw",
                                    onClick = { selectedTab = "Withdraw" },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Amount Input
                            Text(
                                text = if (selectedTab == "Add") "Amount to Add" else "Amount to Withdraw", 
                                color = TextPrimary, 
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = amount,
                                onValueChange = { amount = it },
                                placeholder = { Text("Enter amount", color = TextHint) },
                                leadingIcon = { 
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(if(selectedTab == "Add") Color(0xFFE0F2F1) else Color(0xFFFFEBEE)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                         Text(
                                             "₹", 
                                             fontWeight = FontWeight.Bold, 
                                             color = if(selectedTab == "Add") Color(0xFF00695C) else Color(0xFFC62828),
                                             fontSize = 14.sp
                                         )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if(selectedTab == "Add") Color(0xFF1DE9B6) else Color(0xFFEF5350),
                                    unfocusedBorderColor = Color(0xFFE0E0E0), // Light Grey
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Quick Add
                            Text(
                                text = if(selectedTab == "Add") "Quick Add" else "Quick Withdraw", 
                                color = TextSecondary, 
                                fontSize = 12.sp, 
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                QuickAmountButton("₹1k") { amount = "1000" }
                                QuickAmountButton("₹5k") { amount = "5000" }
                                QuickAmountButton("₹10k") { amount = "10000" }
                                QuickAmountButton("₹25k") { amount = "25000" }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Main Button (Gradient & Enlarge)
                    val gradientBrush = if(selectedTab == "Add") {
                        Brush.horizontalGradient(listOf(Color(0xFF00BFA5), Color(0xFF1DE9B6))) // Teal Gradient
                    } else {
                        Brush.horizontalGradient(listOf(Color(0xFFE53935), Color(0xFFEF5350))) // Red Gradient
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp) // Enlarged height from 64.dp to 80.dp
                            .padding(bottom = 24.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(gradientBrush)
                            .clickable { updateSavings() },
                        contentAlignment = Alignment.Center
                    ) {
                         Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if(selectedTab == "Add") Icons.Rounded.TrendingUp else Icons.Rounded.Remove,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp) // Slightly larger icon
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (selectedTab == "Add") "Update Savings" else "Withdraw",
                                fontSize = 24.sp, // Enlarged font from 20.sp to 24.sp
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

// Helper extension for formatting doubles
fun Double.format(digits: Int) = "%.${digits}f".format(this)

@Composable
fun TabButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Color.White else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if(selected) {
                 Icon(
                    imageVector = icon, 
                    contentDescription = null, 
                    tint = if(text == "Add") Color(0xFF00BFA5) else Color(0xFFE53935),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                color = if (selected) (if(text == "Add") Color(0xFF00BFA5) else Color(0xFFE53935)) else TextSecondary,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun QuickAmountButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(60.dp)
            .height(36.dp)
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp)) // Light Grey Border
            .background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp)) // Very Light Grey Bg
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF004D40)) // Dark Teal Text
    }
}

@Preview
@Composable
fun GoalDetailsPreview() {
    GoalDetailsScreen(onBackClick = {}, onEditGoalClick = {})
}
