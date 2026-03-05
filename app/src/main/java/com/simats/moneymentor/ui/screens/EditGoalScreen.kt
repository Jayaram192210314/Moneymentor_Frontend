package com.simats.moneymentor.ui.screens

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
import com.simats.moneymentor.data.GoalRepository
import com.simats.moneymentor.ui.theme.*

@Composable
fun EditGoalScreen(
    onBackClick: () -> Unit,
    onGoalUpdated: () -> Unit,
    viewModel: com.simats.moneymentor.ui.viewmodels.GoalViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val initialGoal = remember { com.simats.moneymentor.data.GoalRepository.getSelectedGoal() }

    // State for fields
    var goalName by remember { mutableStateOf(initialGoal.name) }
    var targetAmount by remember { mutableStateOf(initialGoal.targetAmount.toInt().toString()) }
    var alreadySavedAmount by remember { mutableStateOf(initialGoal.currentAmount.toInt().toString()) }
    // Initialize deadline safely parsed to yyyy-MM-dd if possible
    val initialDeadlineRaw = initialGoal.dueDate ?: ""
    val parsedInitialDeadline = try {
        val formats = listOf(
            "EEE, dd MMM yyyy HH:mm:ss zzz", // e.g. "Wed, 25 Feb 2026 00:00:00 GMT"
            "yyyy-MM-dd HH:mm:ss", 
            "yyyy-MM-dd", 
            "dd MMM yyyy"
        )
        val parsed = formats.firstNotNullOfOrNull { format ->
            try { java.text.SimpleDateFormat(format, java.util.Locale.ENGLISH).parse(initialDeadlineRaw.trim()) } 
            catch(e: Exception) { null }
        }
        if (parsed != null) java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ENGLISH).format(parsed) else initialDeadlineRaw
    } catch(e: Exception) { initialDeadlineRaw }

    var deadline by remember { mutableStateOf(parsedInitialDeadline) }

    val uiMessage by viewModel.uiMessage.collectAsState()

    LaunchedEffect(uiMessage) {
        uiMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            if (it.contains("successfully", ignoreCase = true)) {
                viewModel.clearMessage()
                onGoalUpdated()
            } else {
                viewModel.clearMessage()
            }
        }
    }

    // Date Picker
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

    fun saveChanges() {
        val target = targetAmount.toDoubleOrNull() ?: initialGoal.targetAmount
        val alreadySaved = alreadySavedAmount.toDoubleOrNull() ?: initialGoal.currentAmount
        
        if (!goalName.matches(Regex("^[A-Za-z\\s]+$"))) {
            android.widget.Toast.makeText(context, "Goal name must contain only letters & spaces", android.widget.Toast.LENGTH_LONG).show()
            return
        }
        
        if (deadline.isNotEmpty() && !deadline.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) {
            android.widget.Toast.makeText(context, "Cannot update: parsed deadline ($deadline) is not YYYY-MM-DD. Please re-pick the date.", android.widget.Toast.LENGTH_LONG).show()
            return
        }

        viewModel.updateGoal(initialGoal.id, goalName, target, deadline, alreadySaved)
    }

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
                        .height(180.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFFE91E63), Color(0xFFFF9800))
                            )
                        )
                )
            }

            // Main Content Card
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 150.dp),
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

                        // Already Saved Amount
                        Text("Already Saved (₹)", color = TextPrimary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = alreadySavedAmount,
                            onValueChange = { alreadySavedAmount = it },
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
                            onValueChange = { }, // Read only
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { datePickerDialog.show() },
                            enabled = false,
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

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // Gradient Save Button
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
                            .clickable { saveChanges() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Save Changes", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
                    text = "Edit Goal",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Update your goal details",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Preview
@Composable
fun EditGoalPreview() {
    EditGoalScreen({}, {})
}
