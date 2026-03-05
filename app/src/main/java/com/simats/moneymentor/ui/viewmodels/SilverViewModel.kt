package com.simats.moneymentor.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.moneymentor.data.model.SilverRateResponse
import com.simats.moneymentor.data.network.SilverRateService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SilverRateState {
    object Idle : SilverRateState()
    object Loading : SilverRateState()
    data class Success(val response: SilverRateResponse) : SilverRateState()
    data class Error(val message: String) : SilverRateState()
}

class SilverViewModel(private val silverRateService: SilverRateService) : ViewModel() {

    private val _rateState = MutableStateFlow<SilverRateState>(SilverRateState.Idle)
    val rateState: StateFlow<SilverRateState> = _rateState.asStateFlow()

    init {
        fetchRates()
    }

    fun fetchRates(force: Boolean = false) {
        if (_rateState.value is SilverRateState.Loading || (!force && _rateState.value is SilverRateState.Success)) {
            println("SilverViewModel: Skip fetch (force=$force), state is ${_rateState.value::class.simpleName}")
            return
        }
        viewModelScope.launch {
            println("SilverViewModel: Starting fetchRates...")
            _rateState.value = SilverRateState.Loading
            try {
                val response = silverRateService.getSilverRates()
                println("SilverViewModel: Fetch success! Price: ${response.silverPrice}")
                _rateState.value = SilverRateState.Success(response)
            } catch (e: Exception) {
                println("SilverViewModel: Fetch error! ${e.message}")
                e.printStackTrace()
                _rateState.value = SilverRateState.Error(e.message ?: "Unknown connection error")
            }
        }
    }
}
