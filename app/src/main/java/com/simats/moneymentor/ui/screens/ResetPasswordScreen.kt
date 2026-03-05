package com.simats.moneymentor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.moneymentor.R
import com.simats.moneymentor.ui.theme.*
import com.simats.moneymentor.ui.components.*
import com.simats.moneymentor.data.AuthRepository
import com.simats.moneymentor.data.network.RetrofitClient

import com.simats.moneymentor.ui.viewmodels.AuthState
import com.simats.moneymentor.ui.viewmodels.AuthViewModel

@Composable
fun ResetPasswordScreen(
    viewModel: AuthViewModel,
    onResetSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    
    val authState by viewModel.authState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                android.widget.Toast.makeText(context, (authState as AuthState.Success).message, android.widget.Toast.LENGTH_SHORT).show()
                onResetSuccess()
                viewModel.resetState()
            }
            is AuthState.Error -> {
                android.widget.Toast.makeText(context, (authState as AuthState.Error).message, android.widget.Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }
    
    // Teal Theme
    val TealPrimary = Color(0xFF80CBC4) // Lighter teal/mint for this one ? Actually image 4 looks like Green/Teal similar to Verify OTP but maybe slightly different or same. Let's use 0xFF80CBC4 or 0xFF4DB6AC.  Wait, image 5 is Confirm Password screen which matches Verify OTP color basically. Let's use TealPrimary 0xFF80CBC4 from image.

    val ScreenColor = Color(0xFF4DB6AC) // Mint green

    // Validation (Reusing logic for visual consistency)
    val isLengthValid = password.length >= 8
    val hasNumber = password.any { it.isDigit() }
    val hasSpecialChar = password.any { !it.isLetterOrDigit() }
    val passwordsMatch = password.isNotEmpty() && password == confirmPassword

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
         // Custom App Bar
         Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                     brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(TealGradientStart, TealGradientEnd)
                    )
                )
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
                    text = "New Password",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Secure your account",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
             Spacer(modifier = Modifier.height(48.dp))
            
            // Icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0F2F1)), // Light teal bg
                 contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lock),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = ScreenColor
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Set New Password",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Create a strong password to protect your account.",
                fontSize = 16.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            InputLabel(text = "New Password")
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("........", color = TextHint) },
                    leadingIcon = {
                        Icon(painter = painterResource(id = R.drawable.ic_lock), contentDescription = null, tint = ScreenColor)
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
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = InputFieldBg,
                        focusedContainerColor = InputFieldBg,
                        unfocusedBorderColor = InputBorder,
                        focusedBorderColor = ScreenColor,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                InputLabel(text = "Confirm Password")
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = { Text("........", color = TextHint) },
                    leadingIcon = {
                        Icon(painter = painterResource(id = R.drawable.ic_lock), contentDescription = null, tint = ScreenColor)
                    },
                    trailingIcon = {
                        IconButton(onClick = { confirmVisible = !confirmVisible }) {
                            Icon(
                                painter = painterResource(if (confirmVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off),
                                contentDescription = if (confirmVisible) "Hide Password" else "Show Password",
                                tint = TextSecondary
                            )
                        }
                    },
                    visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = InputFieldBg,
                        focusedContainerColor = InputFieldBg,
                        unfocusedBorderColor = InputBorder,
                        focusedBorderColor = ScreenColor,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    singleLine = true
                )
                
            Spacer(modifier = Modifier.height(24.dp))
            
             // Password Requirements
            RequirementItem(isValid = isLengthValid, text = "At least 8 characters")
            RequirementItem(isValid = hasNumber, text = "Contains a number")
            RequirementItem(isValid = hasSpecialChar, text = "Contains a special character")
            RequirementItem(isValid = passwordsMatch, text = "Passwords match")
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { 
                    if (isLengthValid && hasNumber && hasSpecialChar && passwordsMatch) {
                        viewModel.resetPassword(password, confirmPassword)
                    } else if (!passwordsMatch) {
                        android.widget.Toast.makeText(context, "Passwords do not match", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        android.widget.Toast.makeText(context, "Please meet all password requirements", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                            colors = listOf(TealGradientStart, TealGradientEnd)
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
                        text = "Reset Password",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_forward),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ResetPasswordPreview() {
    val authRepository = AuthRepository(RetrofitClient.authService)
    val authViewModel = AuthViewModel(authRepository)
    ResetPasswordScreen(authViewModel, {}, {})
}
