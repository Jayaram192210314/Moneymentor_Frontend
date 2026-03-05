package com.simats.moneymentor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.moneymentor.ui.theme.*

@Composable
fun InputLabel(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = TextPrimary,
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
    )
}

@Composable
fun RequirementItem(isValid: Boolean, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Radio button style indicator
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .then(
                    Modifier.padding(2.dp) // inner padding/border effect
                )
                .background(if (isValid) SuccessGreen else Color.Transparent)
                .then(
                     if (!isValid) Modifier.border(1.dp, TextSecondary, RoundedCornerShape(8.dp)) else Modifier
                )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = if (isValid) SuccessGreen else TextSecondary
        )
    }
}

@Composable
fun inputColors() = OutlinedTextFieldDefaults.colors(
    unfocusedContainerColor = InputFieldBg,
    focusedContainerColor = InputFieldBg,
    unfocusedBorderColor = InputBorder,
    focusedBorderColor = AuthPrimary,
    focusedTextColor = Color.Black,
    unfocusedTextColor = Color.Black
)
