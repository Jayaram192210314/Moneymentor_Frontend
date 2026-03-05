package com.simats.moneymentor.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.moneymentor.data.InsightRepository
import com.simats.moneymentor.data.model.DailyTipResponse
import com.simats.moneymentor.data.model.DailyTermResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class InsightState<out T> {
    object Idle : InsightState<Nothing>()
    object Loading : InsightState<Nothing>()
    data class Success<T>(val data: T) : InsightState<T>()
    data class Error(val message: String) : InsightState<Nothing>()
}

class InsightViewModel(private val repository: InsightRepository) : ViewModel() {

    private val _tipState = MutableStateFlow<InsightState<DailyTipResponse>>(InsightState.Idle)
    val tipState: StateFlow<InsightState<DailyTipResponse>> = _tipState.asStateFlow()

    private val _termState = MutableStateFlow<InsightState<DailyTermResponse>>(InsightState.Idle)
    val termState: StateFlow<InsightState<DailyTermResponse>> = _termState.asStateFlow()

    fun fetchDailyInsights() {
        fetchDailyTip()
        fetchDailyTerm()
    }

    fun fetchDailyTip() {
        viewModelScope.launch {
            _tipState.value = InsightState.Loading
            repository.getDailyTip().onSuccess {
                _tipState.value = InsightState.Success(it)
            }.onFailure {
                _tipState.value = InsightState.Error(it.message ?: "Failed to fetch tip")
            }
        }
    }

    fun fetchDailyTerm() {
        viewModelScope.launch {
            _termState.value = InsightState.Loading
            repository.getDailyTerm().onSuccess {
                _termState.value = InsightState.Success(it)
            }.onFailure {
                _termState.value = InsightState.Error(it.message ?: "Failed to fetch term")
            }
        }
    }
}
