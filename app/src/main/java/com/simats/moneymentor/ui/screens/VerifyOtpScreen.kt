package com.simats.moneymentor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.moneymentor.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.simats.moneymentor.ui.theme.*
import com.simats.moneymentor.data.AuthRepository
import com.simats.moneymentor.data.network.RetrofitClient

import com.simats.moneymentor.ui.viewmodels.AuthState
import com.simats.moneymentor.ui.viewmodels.AuthViewModel

@Composable
fun VerifyOtpScreen(
    viewModel: AuthViewModel,
    onVerifySuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    var otp1 by remember { mutableStateOf("") }
    var otp2 by remember { mutableStateOf("") }
    var otp3 by remember { mutableStateOf("") }
    var otp4 by remember { mutableStateOf("") }
    
    val authState by viewModel.authState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                onVerifySuccess()
                viewModel.resetState()
            }
            is AuthState.Error -> {
                android.widget.Toast.makeText(context, (authState as AuthState.Error).message, android.widget.Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }
    
    val focusRequester1 = remember { FocusRequester() }
    val focusRequester2 = remember { FocusRequester() }
    val focusRequester3 = remember { FocusRequester() }
    val focusRequester4 = remember { FocusRequester() }

    // Teal Theme
    val TealPrimary = Color(0xFF00BFA5)

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
                    text = "Verify OTP",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Check your email",
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
                    .background(Color(0xFFE0F2F1)), // Light teal bg
                 contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_security),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = TealPrimary
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Enter Code",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "We've sent a 4-digit verification code to your email address.",
                fontSize = 16.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OtpBox(
                    value = otp1,
                    focusRequester = focusRequester1,
                    onValueChange = { input ->
                        if (input.length > 1) {
                            val digits = input.filter { it.isDigit() }
                            if (digits.isNotEmpty()) otp1 = digits[0].toString()
                            if (digits.length >= 2) otp2 = digits[1].toString()
                            if (digits.length >= 3) otp3 = digits[2].toString()
                            if (digits.length >= 4) otp4 = digits[3].toString()
                            
                            when {
                                digits.length >= 4 -> focusRequester4.requestFocus()
                                digits.length == 3 -> focusRequester4.requestFocus()
                                digits.length == 2 -> focusRequester3.requestFocus()
                                digits.length == 1 -> focusRequester2.requestFocus()
                            }
                        } else {
                            otp1 = input
                            if (input.isNotEmpty()) focusRequester2.requestFocus()
                        }
                    },
                    onBackspace = { }
                )
                OtpBox(
                    value = otp2,
                    focusRequester = focusRequester2,
                    onValueChange = { input ->
                        if (input.length <= 1) {
                            otp2 = input
                            if (input.isNotEmpty()) focusRequester3.requestFocus()
                        }
                    },
                    onBackspace = {
                        if (otp2.isEmpty()) focusRequester1.requestFocus()
                    }
                )
                OtpBox(
                    value = otp3,
                    focusRequester = focusRequester3,
                    onValueChange = { input ->
                        if (input.length <= 1) {
                            otp3 = input
                            if (input.isNotEmpty()) focusRequester4.requestFocus()
                        }
                    },
                    onBackspace = {
                        if (otp3.isEmpty()) focusRequester2.requestFocus()
                    }
                )
                OtpBox(
                    value = otp4,
                    focusRequester = focusRequester4,
                    onValueChange = { input ->
                        if (input.length <= 1) {
                            otp4 = input
                        }
                    },
                    onBackspace = {
                        if (otp4.isEmpty()) focusRequester3.requestFocus()
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { 
                    val otp = otp1 + otp2 + otp3 + otp4
                    if (otp.length == 4) {
                        viewModel.verifyOtp(otp)
                    } else {
                        android.widget.Toast.makeText(context, "Please enter all 4 digits", android.widget.Toast.LENGTH_SHORT).show()
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
                        text = "Verify Code",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_timer),
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Resend code in ",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
                Text(
                    text = "28s",
                    color = TealPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun OtpBox(
    value: String,
    focusRequester: FocusRequester,
    onValueChange: (String) -> Unit,
    onBackspace: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .size(64.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .focusRequester(focusRequester)
            .onKeyEvent {
                if (it.key == Key.Backspace && it.type == KeyEventType.KeyDown) {
                    onBackspace()
                    false
                } else {
                    false
                }
            },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color.White,
            focusedContainerColor = Color.White,
            unfocusedBorderColor = InputBorder,
            focusedBorderColor = Color(0xFF00BFA5),
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(
            textAlign = TextAlign.Center,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    )
}

@Preview
@Composable
fun VerifyOtpPreview() {
    val authRepository = AuthRepository(RetrofitClient.authService)
    val authViewModel = AuthViewModel(authRepository)
    VerifyOtpScreen(authViewModel, {}, {})
}
