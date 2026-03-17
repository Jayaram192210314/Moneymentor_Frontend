package com.simats.moneymentor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onSignInSuccess: () -> Unit,
    onSignUpClick: () -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val authState by viewModel.authState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                viewModel.loginResponse?.user?.id?.let { userId ->
                    val prefs = context.getSharedPreferences("user_profile_prefs", android.content.Context.MODE_PRIVATE)
                    prefs.edit().putInt("user_id", userId).apply()
                }
                onSignInSuccess()
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
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(PurpleGradientStart, PurpleGradientEnd)
                )
            )
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppLogo(
                size = 80.dp,
                cornerRadius = 20.dp,
                backgroundColor = Color.White.copy(alpha = 0.9f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Money Mentor",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Text(
                text = "Your financial journey starts here",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }

        // Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .background(Color.White)
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome Back",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Text(
                    text = "Please sign in to your account",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(top = 8.dp, bottom = 24.dp)
                )

                // Email Input
                Text(
                    text = "Email",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("Email", color = TextHint) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_email),
                            contentDescription = "Email",
                            tint = TextSecondary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = inputColors(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password Input
                Text(
                    text = "Password",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("........", color = TextHint) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_lock),
                            contentDescription = "Password",
                            tint = TextSecondary
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                painter = painterResource(
                                    if (passwordVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off
                                ),
                                contentDescription = if (passwordVisible) "Hide Password" else "Show Password",
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

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Forgot Password?",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = LinkColor,
                    modifier = Modifier
                        .align(Alignment.End)
                        .clickable { onForgotPasswordClick() }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Sign In Button (Gradient)
                Button(
                    onClick = { 
                        if (email.isNotBlank() && password.isNotBlank()) {
                            viewModel.login(email, password)
                        } else {
                            android.widget.Toast.makeText(context, "Please enter email and password", android.widget.Toast.LENGTH_SHORT).show()
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
                            text = "Sign In",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_forward),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color.White
                        )
                    }
               }
               
               Spacer(modifier = Modifier.height(32.dp))
               
               Row(
                   verticalAlignment = Alignment.CenterVertically
               ) {
                   Text(
                       text = "Don't have an account? ",
                       color = TextSecondary,
                       fontSize = 14.sp
                   )
                   Text(
                       text = "Create Account",
                       color = LinkColor,
                       fontSize = 14.sp,
                       fontWeight = FontWeight.Bold,
                       modifier = Modifier.clickable { onSignUpClick() }
                   )
               }

               Spacer(modifier = Modifier.weight(1f))

               Text(
                   text = "Powered by SIMATS ENGINEERING",
                   fontSize = 12.sp,
                   fontWeight = FontWeight.Bold,
                   color = PurpleGradientEnd,
                   modifier = Modifier
                       .align(Alignment.CenterHorizontally)
                       .padding(bottom = 8.dp)
               )
            }
        }
    }
}

@Preview
@Composable
fun LoginScreenPreview() {
    val authRepository = AuthRepository(RetrofitClient.authService)
    val authViewModel = AuthViewModel(authRepository)
    LoginScreen(authViewModel, {}, {}, {})
}
