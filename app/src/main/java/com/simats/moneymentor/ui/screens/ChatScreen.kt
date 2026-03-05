package com.simats.moneymentor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.moneymentor.ui.theme.TextPrimary
import com.simats.moneymentor.ui.theme.TextSecondary
import com.simats.moneymentor.ui.viewmodels.ChatViewModel

@Composable
fun ChatScreen(viewModel: ChatViewModel = viewModel()) {
    val messages = viewModel.messages
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0FDF4)) // Very light green background
    ) {
        // 1. Header
        ChatHeader()

        // 2. Chat Area (Messages)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(messages) { message ->
                    if (message.isUser) {
                        UserMessageBubble(
                            text = message.text,
                            time = message.time
                        )
                    } else {
                        AIMessageBubble(
                            text = message.text,
                            time = message.time
                        )
                    }
                }
                
                if (isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color(0xFF00BFA5),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }
            }
        }

        // 3. Input Area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF0FDF4)) // Match bg
                .padding(bottom = 0.dp) // Minimized padding to zero
        ) {
            // Input Field
            ChatInputArea(onSendMessage = { text ->
                viewModel.sendMessage(text)
            })
        }
    }
}

@Composable
fun ChatHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF00BFA5), Color(0xFF80CBC4))
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
                    imageVector = Icons.Rounded.AccountBalanceWallet,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Money Mentor AI",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Online • Replies instantly",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun UserMessageBubble(text: String, time: String) {
    Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.End) {
             Box(
                modifier = Modifier
                    .weight(1f, fill = false) // Don't take full width if not needed
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 0.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                    .background(Color(0xFF00BFA5)) // Teal
                    .padding(16.dp)
            ) {
                Text(text = text, color = Color.White, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            // User Avatar
             Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00BFA5)),
                contentAlignment = Alignment.Center
            ) {
                 Icon(Icons.Rounded.Person, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
        Text(
            text = time,
            fontSize = 10.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp, end = 40.dp) // Align with bubble
        )
    }
}

@Composable
fun AIMessageBubble(text: String, time: String) {
    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top) {
            // AI Avatar
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0F7FA)), // Light Cyan
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.SmartToy, null, tint = Color(0xFF00BFA5), modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            
            Box(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .clip(RoundedCornerShape(topStart = 0.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Text(
                    text = text, 
                    color = TextPrimary, 
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )
            }
        }
        Text(
            text = time,
            fontSize = 10.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp, start = 40.dp)
        )
    }
}

@Composable
fun ChatInputArea(onSendMessage: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp), // Adjusted internal padding
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Text Field
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("Ask anything about finance...", color = TextSecondary) },
            modifier = Modifier
                .weight(1f)
                .background(Color.White, RoundedCornerShape(50))
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(50)), // Custom Border
            shape = RoundedCornerShape(50),
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent, // Using Box border instead
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Send Button
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFF00BFA5)) // Vibrant Teal button
                .clickable { 
                    if (text.isNotBlank()) {
                        onSendMessage(text)
                        text = "" 
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Send, null, tint = Color.White, modifier = Modifier.size(24.dp))
        }
    }
}

