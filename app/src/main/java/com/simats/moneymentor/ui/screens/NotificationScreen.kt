package com.simats.moneymentor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.moneymentor.ui.viewmodels.NotificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationBottomSheet(
    viewModel: NotificationViewModel?,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val userId = com.simats.moneymentor.data.GoalRepository.currentUserId
    val notifications by viewModel?.notifications?.collectAsState() ?: remember { mutableStateOf(emptyList()) }

    LaunchedEffect(userId) {
        viewModel?.fetchNotifications(userId)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxHeight(0.7f)) {
            Text(text = "Notifications", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            if (notifications.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No notifications yet", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(notifications) { notif ->
                        NotificationItem(notif) {
                            viewModel?.deleteNotification(notif.id, userId)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notif: com.simats.moneymentor.data.model.NotificationData, onDelete: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFE3F2FD)), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Notifications, null, tint = Color(0xFF1976D2))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = notif.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = notif.message, fontSize = 14.sp, color = Color.Gray)
                Text(text = notif.time, fontSize = 12.sp, color = Color.LightGray)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.Delete, null, tint = Color.Red, modifier = Modifier.size(20.dp))
            }
        }
    }
}
