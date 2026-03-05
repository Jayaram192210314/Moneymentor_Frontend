package com.simats.moneymentor.ui.viewmodels

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val time: String = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
)

class ChatViewModel : ViewModel() {
    private val _messages = mutableStateListOf<ChatMessage>()
    val messages: List<ChatMessage> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = "AIzaSyCHri2Fr3idKk1EHcZ5f0Fg_YMXA-qRZt0",
        systemInstruction = content {
            text("""
                You are FinBot, an AI assistant that is strictly limited to financial topics only.

                ALLOWED:
                - Personal finance (budgeting, savings, loans, EMIs, credit score)
                - Investments (stocks, mutual funds, SIP, bonds, gold, real estate basics)
                - Financial markets (NSE/BSE basics, indices, market trends)
                - Economics (inflation, GDP, interest rates, taxation concepts)
                - Banking & insurance (FD, RD, life insurance, health insurance)
                - Financial planning (retirement planning, compounding, wealth building)
                - Financial calculations (EMI, SIP, CAGR, ROI)

                RESTRICTED:
                You MUST refuse and decline ANY request that is NOT directly related to finance.
                This includes:
                - Programming/coding
                - Gaming, movies, entertainment
                - Politics, religion, sensitive topics
                - Medical/health advice
                - Personal opinions, emotional support
                - Non-financial general knowledge
                - Math problems unrelated to finance
                - Legal cases (unless it is financial compliance/Regulations)

                RESPONSE RULES:
                1. If the user asks something outside finance, immediately respond:
                   “I can only answer finance-related questions. Please ask something related to money, investment, banking, or financial learning.”

                2. Do NOT try to answer restricted questions. Do NOT give partial answers.

                3. Keep answers simple, short, structured, and correct.

                4. If the user asks ambiguous questions, ask them to relate it to finance.

                5. Never leave your financial domain under any circumstances.
            """.trimIndent())
        }
    )

    private val chat = generativeModel.startChat()

    init {
        // Initial welcome message
        if (_messages.isEmpty()) {
            _messages.add(ChatMessage("Hello! I'm FinBot, your financial assistant. How can I help you with your money goals today?", false))
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMessage = ChatMessage(text, true)
        _messages.add(userMessage)

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = chat.sendMessage(text)
                val responseText = response.text
                if (responseText != null) {
                    _messages.add(ChatMessage(responseText, false))
                } else {
                    _messages.add(ChatMessage("I encountered an issue generating a response. Please try again.", false))
                }
            } catch (e: Exception) {
                val errorMessage = e.localizedMessage ?: "Failed to get response"
                if (errorMessage.contains("429") || errorMessage.contains("quota", ignoreCase = true)) {
                    _messages.add(ChatMessage("Today's Quota for the FinBot is completed. Please try again tomorrow.", false))
                } else {
                    _messages.add(ChatMessage("Error: $errorMessage", false))
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
}
