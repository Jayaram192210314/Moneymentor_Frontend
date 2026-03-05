package com.simats.moneymentor.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.moneymentor.ui.theme.*

@Composable
fun CreateGoalScreen(
    onBackClick: () -> Unit,
    onGoalCreated: () -> Unit,
    viewModel: com.simats.moneymentor.ui.viewmodels.GoalViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var goalName by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var currentAmount by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }

    val uiMessage by viewModel.uiMessage.collectAsState()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.initRepository(context)
    }

    LaunchedEffect(uiMessage) {
        uiMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
            if (it.contains("successfully", ignoreCase = true)) {
                viewModel.clearMessage()
                onGoalCreated()
            } else {
                viewModel.clearMessage()
            }
        }
    }
    
    // Date Picker Logic
    val calendar = java.util.Calendar.getInstance()
    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
             // Save in yyyy-MM-dd format for backend
             deadline = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
        },
        calendar.get(java.util.Calendar.YEAR),
        calendar.get(java.util.Calendar.MONTH),
        calendar.get(java.util.Calendar.DAY_OF_MONTH)
    )

    BackHandler(onBack = onBackClick)

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFF0F5)) // Light Pinkish Background
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp) // Extended header
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFFE91E63), Color(0xFFFF9800))
                            )
                        )
                )
            }

            // Main Content Card (Overlapping header)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()) // Enable scrolling
                    .padding(top = 150.dp), // Check overlap
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Floating Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE91E63))
                        .border(4.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.TrackChanges,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Form
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        // Goal Name
                        Text("Goal Name", color = TextPrimary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = goalName,
                            onValueChange = { goalName = it },
                            placeholder = { Text("e.g. New Car, Wedding Fund", color = TextHint) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFE91E63).copy(alpha = 0.5f),
                                unfocusedBorderColor = InputBorder,
                                focusedContainerColor = InputFieldBg,
                                unfocusedContainerColor = InputFieldBg,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Target Amount
                        Text("Target Amount (₹)", color = TextPrimary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = targetAmount,
                            onValueChange = { targetAmount = it },
                            placeholder = { Text("e.g. 500000", color = TextHint) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFE91E63).copy(alpha = 0.5f),
                                unfocusedBorderColor = InputBorder,
                                focusedContainerColor = InputFieldBg,
                                unfocusedContainerColor = InputFieldBg,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        // Saved Amount (Optional/Initial)
                        Text("Already Saved (₹)", color = TextPrimary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = currentAmount,
                            onValueChange = { currentAmount = it },
                            placeholder = { Text("e.g. 10000 (Optional)", color = TextHint) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFE91E63).copy(alpha = 0.5f),
                                unfocusedBorderColor = InputBorder,
                                focusedContainerColor = InputFieldBg,
                                unfocusedContainerColor = InputFieldBg,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Deadline
                        Text("Deadline", color = TextPrimary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = deadline,
                            onValueChange = { }, // Read only, set via dialog
                            placeholder = { Text("Click to select date", color = TextHint) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { datePickerDialog.show() }, // Make the whole field clickable
                            enabled = false, // Disable typing
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = { 
                                Icon(
                                    Icons.Rounded.CalendarToday, 
                                    null, 
                                    tint = TextPrimary,
                                    modifier = Modifier.clickable { datePickerDialog.show() }
                                ) 
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledBorderColor = InputBorder,
                                disabledContainerColor = InputFieldBg,
                                disabledTextColor = Color.Black,
                                disabledPlaceholderColor = TextHint
                            )
                        )
                        if (deadline.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            val displayDeadline = try {
                                val trimmed = deadline.trim()
                                val parsed = listOf(
                                    "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd", "MMM yyyy", "MMMM yyyy", "dd MMM yyyy"
                                ).firstNotNullOfOrNull { format ->
                                    try { java.text.SimpleDateFormat(format, java.util.Locale.ENGLISH).parse(trimmed) } catch (e: Exception) { null }
                                }
                                if (parsed != null) java.text.SimpleDateFormat("dd MMM yyyy, EEEE", java.util.Locale.ENGLISH).format(parsed) else deadline
                            } catch (e: Exception) { deadline }
                            
                            Text("Deadline: $displayDeadline", color = Color(0xFFE91E63), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp)) // Fixed spacing instead of weight

                // Action Buttons
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // Gradient Create Goal Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFE91E63), Color(0xFFFF9800))
                                )
                            )
                            .clickable {
                                if (goalName.isNotEmpty() && targetAmount.isNotEmpty() && deadline.isNotEmpty()) {
                                    try {
                                        val target = targetAmount.toDouble()
                                        val initialSaved = if (currentAmount.isEmpty()) 0.0 else currentAmount.toDouble()
                                        viewModel.addGoal(goalName, target, deadline, initialSaved)
                                    } catch (e: NumberFormatException) {
                                        android.widget.Toast.makeText(context, "Invalid amount", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    android.widget.Toast.makeText(context, "Please fill all fields", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Create Goal", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = onBackClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F6FA)),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text("Cancel", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    }
                }
            }

            // Header Overlay (Always on top for clickability)
            Column(modifier = Modifier.padding(24.dp)) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onBackClick() }
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "New Goal",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Set a new financial target",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Preview
@Composable
fun CreateGoalPreview() {
    CreateGoalScreen({}, {})
}
