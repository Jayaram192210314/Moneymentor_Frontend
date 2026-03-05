package com.simats.moneymentor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.moneymentor.data.Goal
import com.simats.moneymentor.data.GoalRepository
import com.simats.moneymentor.data.GoalStatus
import com.simats.moneymentor.ui.theme.*

@Composable
fun GoalsScreen(
    onCreateGoalClick: () -> Unit = {},
    onEditGoalClick: () -> Unit = {},
    onGoalClick: () -> Unit = {},
    viewModel: com.simats.moneymentor.ui.viewmodels.GoalViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    var goalToDelete by remember { mutableStateOf<com.simats.moneymentor.data.Goal?>(null) }
    
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.initRepository(context)
        viewModel.fetchGoals()
    }

    LaunchedEffect(uiMessage) {
        uiMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearMessage()
        }
    }



    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF0F5)) // Light Pinkish White Background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            GoalsHeader()
            
            // Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp) // Space for FAB
            ) {
                // Summary Cards
                item {
                    GoalsSummary()
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                // Goal Items
                val sortedGoalsWithIndices = viewModel.goals.mapIndexed { index, goal -> index to goal }
                    .sortedWith(compareBy<Pair<Int, com.simats.moneymentor.data.Goal>> { it.second.status == com.simats.moneymentor.data.GoalStatus.Done }
                    .thenByDescending { it.second.progress })

                items(sortedGoalsWithIndices) { (originalIndex, goal) ->
                    GoalItemCard(
                        goal = goal,
                        onItemClick = {
                            com.simats.moneymentor.data.GoalRepository.currentGoalIndex = originalIndex
                            onGoalClick()
                        },
                        onEditClick = {
                            com.simats.moneymentor.data.GoalRepository.currentGoalIndex = originalIndex
                            onEditGoalClick()
                        },
                        onDeleteClick = {
                            goalToDelete = goal
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (!isLoading && viewModel.goals.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillParentMaxHeight(0.6f).fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Rounded.AccountBalanceWallet,
                                    null,
                                    tint = TextSecondary.copy(alpha = 0.3f),
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "No goals found",
                                    color = TextSecondary,
                                    fontSize = 16.sp
                                )
                                Text(
                                    "Tap + to create your first goal!",
                                    color = TextSecondary.copy(alpha = 0.7f),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

        }
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFE91E63))
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = onCreateGoalClick,
            containerColor = Color(0xFFE91E63), // Pink
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(64.dp)
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Add Goal", modifier = Modifier.size(32.dp))
        }

        // Delete Confirmation Dialog
        goalToDelete?.let { goal ->
            androidx.compose.ui.window.Dialog(onDismissRequest = { goalToDelete = null }) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = "Warning",
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Delete Goal?",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Are you sure you want to delete '${goal.name}'? This action cannot be undone.",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TextButton(onClick = { goalToDelete = null }) {
                                Text("Cancel", color = TextSecondary)
                            }
                            androidx.compose.material3.Button(
                                onClick = {
                                    viewModel.deleteGoal(goal.id)
                                    goalToDelete = null
                                },
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                            ) {
                                Text("Delete", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GoalsHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFE91E63), Color(0xFFFF9800)) // Pink to Orange
                )
            )
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Icon Container
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.AccountBalanceWallet, // Wallet/Goal Icon
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Goals",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Track your dreams",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun GoalsSummary() {
    val completedCount = GoalRepository.goals.count { it.status == GoalStatus.Done }
    val attentionCount = GoalRepository.goals.count { it.status == GoalStatus.Attention }
    val onTrackCount = GoalRepository.goals.count { it.status == GoalStatus.OnTrack }
    val totalCount = GoalRepository.goals.size

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // TOTAL
        SummaryCard(
            title = "TOTAL",
            value = totalCount.toString(),
            bgColor = Color.White,
            borderColor = Color(0xFFEEEEEE),
            textColor = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        // ON TRACK
        SummaryCard(
            title = "ON TRACK",
            value = onTrackCount.toString(),
            bgColor = Color(0xFFE0F2F1), // Light Teal
            borderColor = Color(0xFF26A69A),
            textColor = Color(0xFF00695C),
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        // ATTENTION
        SummaryCard(
            title = "ATTENTION",
            value = attentionCount.toString(),
            bgColor = Color(0xFFFFF3E0), // Light Orange
            borderColor = Color(0xFFFFB74D),
            textColor = Color(0xFFE65100),
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        // COMPLETED
        SummaryCard(
            title = "COMPLETED",
            value = completedCount.toString(),
            bgColor = Color(0xFFF3E5F5), // Light Purple
            borderColor = Color(0xFFBA68C8),
            textColor = Color(0xFF7B1FA2),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    bgColor: Color,
    borderColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextSecondary) // Smaller font for title
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor) // Adjusted font size
        }
    }
}

@Composable
fun GoalItemCard(
    goal: Goal,
    onItemClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit = {}
) {
    val isOverdue = goal.isOverdue
    val contentColor = if (isOverdue) Color.White else TextPrimary
    val secondaryColor = if (isOverdue) Color.White.copy(alpha = 0.8f) else TextSecondary
    val iconTint = if (isOverdue) Color.White else Color(0xFF1E88E5)
    
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverdue) Color.Transparent else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        val backgroundModifier = if (isOverdue) {
            Modifier.background(Brush.horizontalGradient(listOf(Color(0xFFE53935), Color(0xFFFF5252))))
        } else {
            Modifier
        }

        Box(modifier = Modifier.fillMaxWidth().then(backgroundModifier)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Color Strip
                val stripColor = if (isOverdue) Color.White.copy(alpha = 0.5f) else goal.color
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(6.dp)
                        .background(stripColor)
                )
                
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Circular Progress
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(60.dp)) {
                        CircularProgressIndicator(
                            progress = { goal.progress },
                            modifier = Modifier.fillMaxSize(),
                            color = if (isOverdue) Color.White else goal.color,
                            trackColor = (if (isOverdue) Color.White else goal.color).copy(alpha = 0.2f),
                            strokeWidth = 6.dp,
                            strokeCap = StrokeCap.Round,
                        )
                        Text(
                            text = "${(goal.progress * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = contentColor
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        // Title and Icons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = goal.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = contentColor
                            )
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (goal.status == GoalStatus.Attention) {
                                    Icon(
                                        Icons.Rounded.Info, 
                                        null, 
                                        tint = if (isOverdue) Color.White else Color(0xFFFF9800), 
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                } else if (goal.status == GoalStatus.Done) {
                                    Icon(
                                        Icons.Rounded.CheckCircle, 
                                        null, 
                                        tint = if (isOverdue) Color.White else Color(0xFF2E7D32), 
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                
                                val editBgColor = if (isOverdue) Color.White.copy(alpha = 0.2f) else Color(0xFFE3F2FD)
                                val deleteBgColor = if (isOverdue) Color.White.copy(alpha = 0.2f) else Color(0xFFFFEBEE)

                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(editBgColor)
                                        .clickable { onEditClick() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Rounded.Edit, null, tint = iconTint, modifier = Modifier.size(14.dp))
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(deleteBgColor)
                                        .clickable { onDeleteClick() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Rounded.Delete, null, tint = if (isOverdue) Color.White else Color(0xFFE53935), modifier = Modifier.size(14.dp))
                                }
                                
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Amount Info
                        Row {
                            Text(
                                text = goal.formattedCurrentAmount,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOverdue) Color.White else goal.color
                            )
                            Text(
                                text = " of ${goal.formattedTargetAmount}",
                                fontSize = 14.sp,
                                color = secondaryColor
                            )
                        }
                       
                        Spacer(modifier = Modifier.height(4.dp))
                       
                        // Status/Deadline
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (goal.status == GoalStatus.Done) {
                                Icon(Icons.Rounded.Event, null, tint = secondaryColor, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Goal Achieved!", fontSize = 12.sp, color = secondaryColor)
                            } else {
                                Icon(Icons.Rounded.Event, null, tint = secondaryColor, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                val deadlineText = if (isOverdue) "Overdue! ${goal.formattedDueDate}" else "Due ${goal.formattedDueDate}"
                                Text(
                                    text = deadlineText,
                                    fontSize = 12.sp,
                                    color = secondaryColor,
                                    fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                           
                            Spacer(modifier = Modifier.weight(1f))
                           
                            if (goal.status != GoalStatus.Done) {
                                val buttonBorderColor = if (isOverdue) Color.White else Color(0xFF00C853)
                                Box(
                                    modifier = Modifier
                                        .border(1.dp, buttonBorderColor, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                        .clickable { onItemClick() }
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.Add, null, tint = buttonBorderColor, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("Add", fontSize = 10.sp, color = buttonBorderColor, fontWeight = FontWeight.Bold)
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
