package com.simats.moneymentor.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.moneymentor.ui.theme.MoneyMentorTheme

import com.simats.moneymentor.data.UserProfile

@Composable
fun ProfileScreen(
    userProfile: UserProfile,    notificationViewModel: com.simats.moneymentor.ui.viewmodels.NotificationViewModel? = null,

    onSaveProfile: (UserProfile) -> Unit,
    onBackClick: () -> Unit = {},
    onLogOutClick: () -> Unit = {}
) {
    // 1. Local state initialized from passed profile
    // We needed a local copy for immediate UI updates while editing, 
    // but the source of truth is now passed in.
    // However, since PersonalInfoContent handles its own edit state and calls onSave,
    // we can just pass the current userProfile down.


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        ProfileHeader(onBackClick)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Info (Pass current profile state)
            ProfileInfoSection(userProfile)

            Spacer(modifier = Modifier.height(32.dp))

            // Account Section
            SectionHeader("PERSONAL INFORMATION")
            Spacer(modifier = Modifier.height(8.dp))
            
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    PersonalInfoContent(
                        currentProfile = userProfile,
                        onSave = { newProfile ->
                            onSaveProfile(newProfile)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Settings Section
            SectionHeader("SETTINGS")
            Spacer(modifier = Modifier.height(8.dp))
            ExpandableCard(
                title = "Notification Center",
                icon = Icons.Rounded.Notifications,
                iconBg = Color(0xFFFFF8E1),
                iconTint = Color(0xFFFFCA28)
            ) {
                NotificationContent(notificationViewModel)
            }


            Spacer(modifier = Modifier.height(16.dp))

            ExpandableCard(
                title = "Privacy Policy",
                icon = Icons.Rounded.Security,
                iconBg = Color(0xFFE8F5E9),
                iconTint = Color(0xFF4CAF50)
            ) {
                PrivacyPolicyContent()
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Log Out Button
            Button(
                onClick = onLogOutClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFEBEE),
                    contentColor = Color(0xFFEF5350)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(1.dp, Color(0xFFFFCDD2), RoundedCornerShape(16.dp)),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Icon(Icons.Rounded.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            
             Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// Data Model
// Data Model moved to MainActivity.kt

@Composable
fun ProfileHeader(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF7E57C2), Color(0xFFAB47BC)) // Deep Purple to Purple
                )
            )
            .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
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
                    text = "Profile",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Settings & Preferences",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun ProfileInfoSection(profile: UserProfile) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Avatar
        Box(
            modifier = Modifier
                .size(120.dp)
                .border(2.dp, Brush.linearGradient(listOf(Color(0xFFAB47BC), Color(0xFF7E57C2))), CircleShape)
                .padding(4.dp)
                .clip(CircleShape)
                .background(Color(0xFFF3E5F5)),
            contentAlignment = Alignment.Center
        ) {
            // Get Initials from name
            val initials = profile.fullName.split(" ")
                .take(2)
                .mapNotNull { it.firstOrNull()?.toString() }
                .joinToString("")
                .uppercase()
            
            Text(
                text = if (initials.isNotEmpty()) initials else "U",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8E24AA)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = profile.fullName,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF37474F)
        )
        Text(
            text = profile.email,
            fontSize = 14.sp,
            color = Color(0xFF78909C)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .background(Color(0xFFEDE7F6), RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Free Plan",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF673AB7)
            )
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF90A4AE),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun ExpandableCard(
    title: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(targetValue = if (expanded) 180f else 0f)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // Flat look as per design
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if(expanded) Color(0xFFE1BEE7) else Color.Transparent, RoundedCornerShape(16.dp)) // Subtle border when expanded
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF37474F))
                }
                Icon(
                    Icons.Rounded.KeyboardArrowDown,
                    null,
                    modifier = Modifier.rotate(rotationState),
                    tint = Color(0xFFB0BEC5)
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    HorizontalDivider(color = Color(0xFFEEEEEE))
                    Spacer(modifier = Modifier.height(16.dp))
                    content()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoContent(
    currentProfile: UserProfile,
    onSave: (UserProfile) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    
    // Temporary state for editing
    var editName by remember { mutableStateOf(currentProfile.fullName) }
    var editEmail by remember { mutableStateOf(currentProfile.email) }
    var editPhone by remember { mutableStateOf(currentProfile.phone) }
    var editDob by remember { mutableStateOf(currentProfile.dob) }

    // State for Material3 DatePicker
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = try {
            val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
            dateFormat.parse(editDob)?.time
        } catch (e: Exception) {
            null
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                        editDob = dateFormat.format(java.util.Date(millis))
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Update temp state when view mode connects
    LaunchedEffect(isEditing) {
        if (!isEditing) {
            editName = currentProfile.fullName
            editEmail = currentProfile.email
            editPhone = currentProfile.phone
            editDob = currentProfile.dob
        }
    }

    Column {
        if (isEditing) {
            // Edit Mode: Input Fields
            val textFieldColors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF7E57C2),
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                cursorColor = Color.Black
            )

            OutlinedTextField(
                value = editName,
                onValueChange = { editName = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = editEmail,
                onValueChange = { editEmail = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = editPhone,
                onValueChange = { editPhone = it },
                label = { Text("Phone") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            // Date of Birth Field with Date Picker
            Box(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }) {
                OutlinedTextField(
                    value = editDob, 
                    onValueChange = {}, 
                    readOnly = true, 
                    enabled = true, 
                    label = { Text("Date of Birth") }, 
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors,
                    trailingIcon = {
                        Icon(Icons.Rounded.CalendarToday, contentDescription = "Select Date")
                    }
                )
                Box(modifier = Modifier.matchParentSize().clickable { showDatePicker = true })
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Action Buttons
            Row(modifier = Modifier.fillMaxWidth()) {
                // Cancel Button
                Button(
                    onClick = { isEditing = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF5F5F5),
                        contentColor = Color(0xFF757575)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(48.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("Cancel", fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Save Button
                Button(
                    onClick = {
                        val updatedProfile = currentProfile.copy(
                            fullName = editName,
                            email = editEmail,
                            phone = editPhone,
                            dob = editDob
                        )
                        onSave(updatedProfile)
                        isEditing = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7E57C2), // Purple
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(48.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Icon(Icons.Rounded.Check, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            }
            
        } else {
            // View Mode: Static Info Rows
            InfoRow(label = "Full Name", value = currentProfile.fullName)
            InfoRow(label = "Email", value = currentProfile.email)
            InfoRow(label = "Phone", value = currentProfile.phone)
            InfoRow(label = "Date of Birth", value = currentProfile.dob)
            
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { isEditing = true },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7E57C2),
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Rounded.Edit, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Info")
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color(0xFF90A4AE), fontSize = 14.sp)
        Text(text = value, color = Color(0xFF455A64), fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

@Composable
fun SecurityContent() {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // State for password fields
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Visibility States
    var isCurrentPasswordVisible by remember { mutableStateOf(false) }
    var isNewPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    Column {
        Text("CHANGE PASSWORD", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF90A4AE))
        Spacer(modifier = Modifier.height(12.dp))
        
        // Helper for colors
        val passwordFieldColors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF7E57C2),
            unfocusedBorderColor = Color(0xFFE0E0E0),
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            cursorColor = Color.Black
        )

        // Current Password
        OutlinedTextField(
            value = currentPassword,
            onValueChange = { currentPassword = it },
            label = { Text("Current Password") },
            leadingIcon = { Icon(Icons.Rounded.Lock, null) },
            trailingIcon = {
                val image = if (isCurrentPasswordVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff
                IconButton(onClick = { isCurrentPasswordVisible = !isCurrentPasswordVisible }) {
                    Icon(image, "Toggle Password Visibility")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = passwordFieldColors,
            visualTransformation = if (isCurrentPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None 
                                   else androidx.compose.ui.text.input.PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        // New Password
         OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = { Text("New Password") },
            leadingIcon = { Icon(Icons.Rounded.Lock, null) },
            trailingIcon = {
                val image = if (isNewPasswordVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff
                IconButton(onClick = { isNewPasswordVisible = !isNewPasswordVisible }) {
                    Icon(image, "Toggle Password Visibility")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = passwordFieldColors,
            visualTransformation = if (isNewPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None 
                                   else androidx.compose.ui.text.input.PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        // Confirm Password
         OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            leadingIcon = { Icon(Icons.Rounded.Lock, null) },
            trailingIcon = {
                val image = if (isConfirmPasswordVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff
                IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                    Icon(image, "Toggle Password Visibility")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = passwordFieldColors,
            visualTransformation = if (isConfirmPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None 
                                   else androidx.compose.ui.text.input.PasswordVisualTransformation()
        )
        
        
        Spacer(modifier = Modifier.height(16.dp))

        // Password Constraints Checklist
        if (newPassword.isNotEmpty() || confirmPassword.isNotEmpty()) {
            PasswordConstraintsList(newPassword, confirmPassword)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = { 
                // Validation Logic
                if (newPassword.length < 8) {
                    android.widget.Toast.makeText(context, "Password must be at least 8 characters.", android.widget.Toast.LENGTH_SHORT).show()
                } else if (!newPassword.any { it.isDigit() }) {
                    android.widget.Toast.makeText(context, "Password must contain at least one number.", android.widget.Toast.LENGTH_SHORT).show()
                } else if (!newPassword.any { !it.isLetterOrDigit() }) {
                     android.widget.Toast.makeText(context, "Password must contain at least one special character.", android.widget.Toast.LENGTH_SHORT).show()
                } else if (newPassword != confirmPassword) {
                    android.widget.Toast.makeText(context, "Passwords do not match.", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    android.widget.Toast.makeText(context, "Password Updated Successfully!", android.widget.Toast.LENGTH_SHORT).show()
                    currentPassword = ""
                    newPassword = ""
                    confirmPassword = ""
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF7E57C2),
                contentColor = Color.White
            )
        ) {
            Text("Update Password")
        }
    }
}

@Composable
fun SavingsReminderSettings() {
    var showReminderSettings by remember { mutableStateOf(false) }
    var selectedDay by remember { mutableStateOf(1) } // Default 1st of month

    Column {
        Text("SAVINGS REMINDER", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF90A4AE))
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE3F2FD)) // Light Blue
                .clickable { showReminderSettings = true }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.CalendarToday,
                    contentDescription = null,
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Monthly Savings Reminder",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0)
                )
                Text(
                    text = "Remind me on day $selectedDay of every month",
                    fontSize = 12.sp,
                    color = Color(0xFF546E7A)
                )
            }
            Icon(
                Icons.Rounded.Edit,
                contentDescription = "Edit",
                tint = Color(0xFF1565C0),
                modifier = Modifier.size(16.dp)
            )
        }

        if (showReminderSettings) {
            AlertDialog(
                onDismissRequest = { showReminderSettings = false },
                title = { Text("Set Reminder Date") },
                text = {
                    Column {
                        Text("Select the day of the month for your savings reminder:")
                        Spacer(modifier = Modifier.height(16.dp))
                        // Simple number picker UI
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(31) { index ->
                                val day = index + 1
                                val isSelected = day == selectedDay
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color(0xFF2196F3) else Color(0xFFF5F5F5))
                                        .clickable { selectedDay = day },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$day",
                                        color = if (isSelected) Color.White else Color.Black,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showReminderSettings = false }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showReminderSettings = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun PasswordConstraintsList(password: String, confirmPassword: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ConstraintRow("At least 8 characters", password.length >= 8)
            ConstraintRow("Contains a number", password.any { it.isDigit() })
            ConstraintRow("Contains a special character", password.any { !it.isLetterOrDigit() })
            ConstraintRow("Passwords match", password.isNotEmpty() && password == confirmPassword)
        }
    }
}

@Composable
fun ConstraintRow(label: String, isValid: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = if (isValid) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isValid) Color(0xFF7E57C2) else Color(0xFFB0BEC5), // Purple if valid, Gray if not
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            color = if (isValid) Color(0xFF455A64) else Color(0xFF90A4AE)
        )
    }
}



@Composable
fun PrivacyPolicyContent() {
    Column {
        Text(
            text = "Your privacy is important to us. MoneyMentor is committed to protecting your personal and financial data.",
            fontSize = 14.sp,
            color = Color(0xFF455A64),
            lineHeight = 20.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        PrivacyItem(
            title = "Data Collection",
            description = "We only collect data necessary to provide you with financial insights and goal tracking."
        )
        
        PrivacyItem(
            title = "Data Security",
            description = "All your data is encrypted and stored securely. We do not sell your personal information."
        )
        
        PrivacyItem(
            title = "User Control",
            description = "You have full control over your data and can request deletion or export at any time."
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "For more information, please contact our support team.",
            fontSize = 12.sp,
            color = Color(0xFF90A4AE),
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
        )
    }
}

@Composable
fun PrivacyItem(title: String, description: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(Color(0xFF4CAF50), CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF37474F)
            )
        }
        Text(
            text = description,
            fontSize = 13.sp,
            color = Color(0xFF78909C),
            modifier = Modifier.padding(start = 16.dp, top = 2.dp)
        )
    }
}



@Composable
fun NotificationContent(viewModel: com.simats.moneymentor.ui.viewmodels.NotificationViewModel? = null) {
    val userId = com.simats.moneymentor.data.GoalRepository.currentUserId
    val uiState by viewModel?.uiState?.collectAsState() ?: remember { mutableStateOf(null) }
    val context = androidx.compose.ui.platform.LocalContext.current

    val dailyEnabled by viewModel?.dailyEnabled?.collectAsState() ?: remember { mutableStateOf(true) }
    val dailyTime by viewModel?.dailyTime?.collectAsState() ?: remember { mutableStateOf("09:00 AM") }
    val monthlyEnabled by viewModel?.monthlyEnabled?.collectAsState() ?: remember { mutableStateOf(true) }
    val monthlyDay by viewModel?.monthlyDay?.collectAsState() ?: remember { mutableStateOf("1") }
    val monthlyTime by viewModel?.monthlyTime?.collectAsState() ?: remember { mutableStateOf("10:00 AM") }

    Column {
        NotificationOption(
            title = "Daily Financial Tip",
            subtitle = "Get a smart money tip every day",
            icon = Icons.Rounded.Lightbulb,
            iconBg = Color(0xFFFFF8E1),
            iconTint = Color(0xFFFFB300),
            switchColor = Color(0xFFFFD700), // Gold
            checked = dailyEnabled,
            hasTimePicker = true,
            time = dailyTime,
            onToggle = { isEnabled, time, _ ->
                if (isEnabled) viewModel?.enableDaily(userId, time)
                else viewModel?.disableNotification(userId, "daily")
            },
            onSettingsChanged = { time, _ -> viewModel?.enableDaily(userId, time) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
         NotificationOption(
            title = "Monthly Savings Reminder",
            subtitle = "Reminder to update your savings goal",
            icon = Icons.Rounded.Savings,
            iconBg = Color(0xFFE0F2F1),
            iconTint = Color(0xFF00BFA5),
            switchColor = Color(0xFF4CAF50), // Green
            checked = monthlyEnabled,
            hasDayPicker = true,
            day = monthlyDay,
            hasTimePicker = true,
            time = monthlyTime,
            onToggle = { isEnabled, time, day ->
                if (isEnabled) viewModel?.enableMonthly(userId, day.toIntOrNull() ?: 1, time)
                else viewModel?.disableNotification(userId, "monthly")
            },
            onSettingsChanged = { time, day -> viewModel?.enableMonthly(userId, day.toIntOrNull() ?: 1, time) }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        Text(
             text = "Daily tips arrive every day. Savings reminders arrive on the selected day of each month.",
             fontSize = 12.sp,
             color = Color(0xFF90A4AE),
             textAlign = TextAlign.Center,
             modifier = Modifier.fillMaxWidth(),
             lineHeight = 16.sp
        )
    }
}

@Composable
fun NotificationOption(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    switchColor: Color = MaterialTheme.colorScheme.primary,
    checked: Boolean,
    hasTimePicker: Boolean = false,
    hasDayPicker: Boolean = false,
    time: String = "",
    day: String = "",
    onToggle: (Boolean, String, String) -> Unit = { _, _, _ -> },
    onSettingsChanged: (String, String) -> Unit = { _, _ -> }
) {
    var isChecked by remember { mutableStateOf(checked) }
    var selectedTime by remember { mutableStateOf(time) }
    var selectedDay by remember { mutableStateOf(day) }
    val context = androidx.compose.ui.platform.LocalContext.current
    
    fun showTimePicker() {
        android.app.TimePickerDialog(context, { _, h, m ->
            val amPm = if (h < 12) "AM" else "PM"
            val displayHour = if (h == 0) 12 else if (h > 12) h - 12 else h
            val displayMinute = if (m < 10) "0$m" else "$m"
            selectedTime = "$displayHour:$displayMinute $amPm"
            onSettingsChanged(selectedTime, selectedDay)
        }, 9, 0, false).show()
    }

    var showDayPickerDialog by remember { mutableStateOf(false) }
    if (showDayPickerDialog) {
         AlertDialog(
            onDismissRequest = { showDayPickerDialog = false },
            title = { Text("Set Reminder Date") },
            text = {
                Column {
                    Text("Select day of month:")
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(31) { index ->
                            val d = index + 1
                            val isSelected = d.toString() == selectedDay
                            Box(
                                modifier = Modifier.size(40.dp).clip(CircleShape).background(if (isSelected) Color(0xFF2196F3) else Color(0xFFF5F5F5))
                                    .clickable { 
                                        selectedDay = d.toString()
                                        onSettingsChanged(selectedTime, selectedDay)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "$d", color = if (isSelected) Color.White else Color.Black)
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showDayPickerDialog = false }) { Text("Save") } }
        )
    }

    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                 Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(iconBg), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text(text = subtitle, fontSize = 12.sp, color = Color(0xFF90A4AE))
                }
                Switch(
                    checked = isChecked,
                    onCheckedChange = { isChecked = it; onToggle(it, selectedTime, selectedDay) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = switchColor,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color.LightGray
                    )
                )
            }
            if (isChecked && (hasTimePicker || hasDayPicker)) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        if (hasDayPicker) NotificationSettingRow(Icons.Rounded.CalendarToday, "Remind me on day", selectedDay, iconTint, iconBg) { showDayPickerDialog = true }
                        if (hasTimePicker) NotificationSettingRow(Icons.Rounded.Schedule, "Remind me at", selectedTime, iconTint, iconBg) { showTimePicker() }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationSettingRow(icon: ImageVector, label: String, value: String, iconTint: Color, iconBg: Color, onClick: () -> Unit) {
    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, fontSize = 14.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, fontWeight = FontWeight.Bold, color = iconTint, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.background(iconBg, RoundedCornerShape(6.dp)).clickable { onClick() }.padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text("Edit", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = iconTint)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    MoneyMentorTheme {
        ProfileScreen(
            userProfile = com.simats.moneymentor.data.UserProfile("Alex", "email", "phone", "dob"),
            onSaveProfile = {}
        )
    }
}
