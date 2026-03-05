package com.simats.moneymentor.ui.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.moneymentor.data.NotificationRepository
import com.simats.moneymentor.data.model.NotificationData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.content.Context
import com.simats.moneymentor.data.NotificationScheduler

sealed class NotificationUiState {
    object Idle : NotificationUiState()
    object Loading : NotificationUiState()
    data class Success(val message: String) : NotificationUiState()
    data class Error(val message: String) : NotificationUiState()
}

class NotificationViewModel(
    private val repository: NotificationRepository,
    private val prefs: SharedPreferences,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<NotificationUiState>(NotificationUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationData>>(emptyList())
    val notifications = _notifications.asStateFlow()

    private val _dailyEnabled = MutableStateFlow(prefs.getBoolean("daily_enabled", true))
    val dailyEnabled = _dailyEnabled.asStateFlow()

    private val _dailyTime = MutableStateFlow(prefs.getString("daily_time", "09:00 AM") ?: "09:00 AM")
    val dailyTime = _dailyTime.asStateFlow()

    private val _monthlyEnabled = MutableStateFlow(prefs.getBoolean("monthly_enabled", true))
    val monthlyEnabled = _monthlyEnabled.asStateFlow()

    private val _monthlyDay = MutableStateFlow(prefs.getString("monthly_day", "1") ?: "1")
    val monthlyDay = _monthlyDay.asStateFlow()

    private val _monthlyTime = MutableStateFlow(prefs.getString("monthly_time", "10:00 AM") ?: "10:00 AM")
    val monthlyTime = _monthlyTime.asStateFlow()

    fun resetState() { _uiState.value = NotificationUiState.Idle }

    fun fetchNotifications(userId: Int) {
        viewModelScope.launch {
            val list = repository.getNotifications(userId)
            if (list != null) _notifications.value = list
        }
    }

    fun deleteNotification(id: Int, userId: Int) {
        viewModelScope.launch {
            val success = repository.deleteNotification(id)
            if (success) fetchNotifications(userId)
        }
    }

    fun enableDaily(userId: Int, time: String) {
        _uiState.value = NotificationUiState.Loading
        viewModelScope.launch {
            val result = repository.enableDaily(userId, time)
            result.onSuccess { msg ->
                _dailyEnabled.value = true
                _dailyTime.value = time
                prefs.edit().apply {
                    putBoolean("daily_enabled", true)
                    putString("daily_time", time)
                    apply()
                }
                _uiState.value = NotificationUiState.Success(msg)
                NotificationScheduler.scheduleReminders(context)
            }.onFailure { e ->
                _uiState.value = NotificationUiState.Error(e.message ?: "Error")
            }
        }
    }

    fun enableMonthly(userId: Int, day: Int, time: String) {
        _uiState.value = NotificationUiState.Loading
        viewModelScope.launch {
            val result = repository.enableMonthly(userId, day, time)
            result.onSuccess { msg ->
                _monthlyEnabled.value = true
                _monthlyDay.value = day.toString()
                _monthlyTime.value = time
                prefs.edit().apply {
                    putBoolean("monthly_enabled", true)
                    putString("monthly_day", day.toString())
                    putString("monthly_time", time)
                    apply()
                }
                _uiState.value = NotificationUiState.Success(msg)
                NotificationScheduler.scheduleReminders(context)
            }.onFailure { e ->
                _uiState.value = NotificationUiState.Error(e.message ?: "Error")
            }
        }
    }

    fun disableNotification(userId: Int, type: String) {
        _uiState.value = NotificationUiState.Loading
        viewModelScope.launch {
            val result = repository.disableNotification(userId, type)
            result.onSuccess { msg ->
                if (type == "daily") {
                    _dailyEnabled.value = false
                    prefs.edit().putBoolean("daily_enabled", false).apply()
                } else {
                    _monthlyEnabled.value = false
                    prefs.edit().putBoolean("monthly_enabled", false).apply()
                }
                _uiState.value = NotificationUiState.Success(msg)
                NotificationScheduler.scheduleReminders(context)
            }.onFailure { e ->
                _uiState.value = NotificationUiState.Error(e.message ?: "Error")
            }
        }
    }
}
