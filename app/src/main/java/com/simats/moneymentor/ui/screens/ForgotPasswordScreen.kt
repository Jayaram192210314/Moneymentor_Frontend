package com.simats.moneymentor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
fun ForgotPasswordScreen(
    viewModel: AuthViewModel,
    onSendOtpSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    val authState by viewModel.authState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                onSendOtpSuccess()
                viewModel.resetState()
            }
            is AuthState.Error -> {
                android.widget.Toast.makeText(context, (authState as AuthState.Error).message, android.widget.Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

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
                        colors = listOf(PinkGradientStart, PinkGradientEnd) // Pink/Orange gradient for this screen based on image
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
                    text = "Reset Password",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Recover your account",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            // Icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFF0F5)), // Light pink bg
                 contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_email),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color(0xFFDD2476)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Forgot Password?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Enter your email address to receive a 4-digit verification code.",
                fontSize = 16.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            InputLabel(text = "Email Address")
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("alex@example.com", color = TextHint) },
                leadingIcon = {
                    Icon(painter = painterResource(id = R.drawable.ic_email), contentDescription = null, tint = Color(0xFFDD2476))
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = InputFieldBg,
                    focusedContainerColor = InputFieldBg,
                    unfocusedBorderColor = InputBorder,
                    focusedBorderColor = Color(0xFFDD2476),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { 
                    if (email.isNotBlank()) {
                        viewModel.forgotPassword(email)
                    } else {
                        android.widget.Toast.makeText(context, "Please enter your email", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                            colors = listOf(PinkGradientStart, PinkGradientEnd)
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
                        text = "Send OTP",
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
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = "Back to Sign In",
                color = TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { onBackClick() }
                    .padding(16.dp)
            )
        }
    }
}

@Preview
@Composable
fun ForgotPasswordPreview() {
    val authRepository = AuthRepository(RetrofitClient.authService)
    val authViewModel = AuthViewModel(authRepository)
    ForgotPasswordScreen(authViewModel, {}, {})
}
