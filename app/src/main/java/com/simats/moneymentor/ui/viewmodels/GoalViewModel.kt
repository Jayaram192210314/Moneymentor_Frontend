package com.simats.moneymentor.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.moneymentor.data.Goal
import com.simats.moneymentor.data.GoalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GoalViewModel : ViewModel() {
    private val repository = GoalRepository

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    var goals = repository.goals

    fun clearMessage() {
        _uiMessage.value = null
    }

    fun initRepository(context: android.content.Context) {
        repository.init(context)
    }

    fun fetchGoals() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.fetchGoals(repository.currentUserId)
            _isLoading.value = false
            result.onFailure { e ->
                _uiMessage.value = "Error: ${e.message}"
            }
        }
    }

    fun fetchSingleGoal(goalId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.fetchGoalById(goalId)
            _isLoading.value = false
            result.onFailure { e ->
                _uiMessage.value = "Error: ${e.message}"
            }
        }
    }

    fun addGoal(name: String, target: Double, deadline: String, currentAmount: Double = 0.0) {
        viewModelScope.launch {
            _isLoading.value = true
            val request = com.simats.moneymentor.data.model.AddGoalRequest(
                goalName = name,
                targetAmount = target,
                alreadySavedAmount = currentAmount,
                deadline = deadline,
                userId = repository.currentUserId
            )
            val result = repository.createGoal(request)
            _isLoading.value = false
            
            result.onSuccess {
                _uiMessage.value = "Goal created successfully!"
                fetchGoals() // Refresh the list from server
            }.onFailure { e ->
                _uiMessage.value = "Error: ${e.message}"
            }
        }
    }

    fun updateGoal(goalId: Int, name: String?, target: Double?, deadline: String?, currentAmount: Double?) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.updateGoal(goalId, name, target, deadline, currentAmount)
            _isLoading.value = false

            result.onSuccess {
                _uiMessage.value = "Goal updated successfully!"
                fetchGoals() // Sync list
            }.onFailure { e ->
                _uiMessage.value = "Error: ${e.message}"
            }
        }
    }

    fun addMoney(goalId: Int, amount: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.updateExtra(goalId, amount)
            _isLoading.value = false
            
            result.onSuccess {
                _uiMessage.value = "Money added successfully!"
                fetchSingleGoal(goalId) // Refresh data for this goal
                fetchGoals() // Refresh data from server
            }.onFailure { e ->
                _uiMessage.value = "Error: ${e.message}"
            }
        }
    }

    fun withdrawMoney(goalId: Int, amount: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.withdrawMoney(goalId, amount)
            _isLoading.value = false
            
            result.onSuccess {
                _uiMessage.value = "Money withdrawn successfully!"
                fetchSingleGoal(goalId) // Refresh data for this goal
                fetchGoals() // Refresh data from server
            }.onFailure { e ->
                _uiMessage.value = "Error: ${e.message}"
            }
        }
    }

    fun deleteGoal(goalId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.deleteGoal(goalId)
            _isLoading.value = false

            result.onSuccess {
                _uiMessage.value = "Goal deleted successfully!"
                fetchGoals() // Refresh list from server
            }.onFailure { e ->
                _uiMessage.value = "Error: ${e.message}"
            }
        }
    }
}
