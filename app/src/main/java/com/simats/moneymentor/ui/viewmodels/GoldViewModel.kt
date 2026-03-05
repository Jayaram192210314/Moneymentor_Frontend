package com.simats.moneymentor.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.moneymentor.data.model.GoldRateResponse
import com.simats.moneymentor.data.network.GoldRateService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class GoldRateState {
    object Idle : GoldRateState()
    object Loading : GoldRateState()
    data class Success(val response: GoldRateResponse) : GoldRateState()
    data class Error(val message: String) : GoldRateState()
}

class GoldViewModel(private val goldRateService: GoldRateService) : ViewModel() {

    private val _rateState = MutableStateFlow<GoldRateState>(GoldRateState.Idle)
    val rateState: StateFlow<GoldRateState> = _rateState.asStateFlow()

    init {
        fetchRates()
    }

    fun fetchRates() {
        viewModelScope.launch {
            _rateState.value = GoldRateState.Loading
            try {
                val response = goldRateService.getGoldRates()
                _rateState.value = GoldRateState.Success(response)
            } catch (e: Exception) {
                _rateState.value = GoldRateState.Error(e.message ?: "Failed to fetch rates")
            }
        }
    }
}
