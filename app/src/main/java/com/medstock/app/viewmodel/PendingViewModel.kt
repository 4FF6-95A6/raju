package com.medstock.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medstock.app.data.entity.Sale
import com.medstock.app.data.repository.AppRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PendingViewModel(private val repo: AppRepository) : ViewModel() {

    val pending: StateFlow<List<Sale>> = repo.pendingSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val totalPending: StateFlow<Double> = repo.totalPending()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    fun recordPayment(saleId: Long, amount: Double, mode: String, note: String = "", onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repo.recordPayment(saleId, amount, mode, note)
            onDone()
        }
    }
}
