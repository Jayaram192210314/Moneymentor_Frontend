package com.simats.moneymentor.ui.screens

import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.moneymentor.R
import com.simats.moneymentor.ui.theme.*
import com.simats.moneymentor.ui.components.*

import com.simats.moneymentor.ui.viewmodels.AuthState
import com.simats.moneymentor.ui.viewmodels.AuthViewModel
import com.simats.moneymentor.data.AuthRepository
import com.simats.moneymentor.data.network.RetrofitClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    viewModel: AuthViewModel,
    onSignInClick: () -> Unit,
    onBackClick: () -> Unit,
    onSignUpSuccess: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                onSignUpSuccess()
                viewModel.resetState()
            }
            is AuthState.Error -> {
                android.widget.Toast.makeText(context, (authState as AuthState.Error).message, android.widget.Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    // Validation
    val isLengthValid = password.length >= 8
    val hasNumber = password.any { it.isDigit() }
    val hasSpecialChar = password.any { !it.isLetterOrDigit() }
    val passwordsMatch = password.isNotEmpty() && password == confirmPassword

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(PurpleGradientStart, PurpleGradientEnd)
                )
            )
    ) {
        // App Bar / Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, bottom = 24.dp, start = 16.dp, end = 16.dp)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            
            Column(
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 48.dp)
            ) {
                Text(
                    text = "Create Account",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Start your journey",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        // Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .background(Color.White)
                .padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 24.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Full Name
                InputLabel(text = "Full Name")
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    placeholder = { Text("Name", color = TextHint) },
                    leadingIcon = {
                        Icon(painter = painterResource(id = R.drawable.ic_person), contentDescription = null, tint = TextSecondary)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = inputColors(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // Email
                InputLabel(text = "Email Address")
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("Email", color = TextHint) },
                    leadingIcon = {
                        Icon(painter = painterResource(id = R.drawable.ic_email), contentDescription = null, tint = TextSecondary)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = inputColors(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Mobile Number
                InputLabel(text = "Mobile Number")
                OutlinedTextField(
                    value = mobileNumber,
                    onValueChange = { mobileNumber = it },
                    placeholder = { Text("+91 00000 00000", color = TextHint) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Rounded.Phone, contentDescription = null, tint = TextSecondary)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = inputColors(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Date of Birth
                InputLabel(text = "Date of Birth")
                OutlinedTextField(
                    value = dob,
                    onValueChange = { },
                    readOnly = true,
                    placeholder = { Text("DD/MM/YYYY", color = TextHint) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.CalendarToday, 
                            contentDescription = null, 
                            tint = TextSecondary,
                            modifier = Modifier.clickable { showDatePicker = true }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = inputColors(),
                    singleLine = true
                )

                if (showDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    dob = dateFormatter.format(Date(millis))
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

                Spacer(modifier = Modifier.height(16.dp))

                // Password
                InputLabel(text = "Password")
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("........", color = TextHint) },
                    leadingIcon = {
                        Icon(painter = painterResource(id = R.drawable.ic_lock), contentDescription = null, tint = TextSecondary)
                    },
                    trailingIcon = {
                         IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                painter = painterResource(if (passwordVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off),
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = inputColors(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Confirm Password
                InputLabel(text = "Confirm Password")
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = { Text("........", color = TextHint) },
                    leadingIcon = {
                        Icon(painter = painterResource(id = R.drawable.ic_check_circle), contentDescription = null, tint = if (passwordsMatch && confirmPassword.isNotEmpty()) SuccessGreen else TextSecondary)
                    },
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                painter = painterResource(if (confirmPasswordVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off),
                                contentDescription = if (confirmPasswordVisible) "Hide Password" else "Show Password",
                                tint = TextSecondary
                            )
                        }
                    },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = inputColors(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Password Requirements
                RequirementItem(isValid = isLengthValid, text = "At least 8 characters")
                RequirementItem(isValid = hasNumber, text = "Contains a number")
                RequirementItem(isValid = hasSpecialChar, text = "Contains a special character")
                RequirementItem(isValid = passwordsMatch, text = "Passwords match")

                Spacer(modifier = Modifier.height(32.dp))

                // Create Account Button
                Button(
                    onClick = { 
                        if (fullName.isNotBlank() && email.isNotBlank() && password.isNotBlank() && passwordsMatch) {
                            // Convert DD/MM/YYYY to YYYY-MM-DD for the server
                            val formattedDob = try {
                                val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                inputFormat.parse(dob)?.let { outputFormat.format(it) } ?: dob
                            } catch (e: Exception) {
                                dob
                            }
                            
                            viewModel.register(fullName, email, password, confirmPassword, mobileNumber, formattedDob)
                        } else if (!passwordsMatch) {
                            android.widget.Toast.makeText(context, "Passwords do not match", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            android.widget.Toast.makeText(context, "Please fill all fields", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(PurpleGradientStart, PurpleGradientEnd)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    enabled = authState !is AuthState.Loading
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = "Create Account",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                             color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Already have an account? ", color = TextSecondary, fontSize = 14.sp)
                    Text(
                        text = "Sign In",
                        color = LinkColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onSignInClick() }
                    )
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun SignUpScreenPreview() {
    val authRepository = AuthRepository(RetrofitClient.authService)
    val authViewModel = AuthViewModel(authRepository)
    SignUpScreen(
        viewModel = authViewModel,
        onSignInClick = {},
        onBackClick = {},
        onSignUpSuccess = {}
    )
}
