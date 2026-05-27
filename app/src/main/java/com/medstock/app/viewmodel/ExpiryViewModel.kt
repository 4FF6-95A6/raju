package com.medstock.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medstock.app.data.entity.Medicine
import com.medstock.app.data.repository.AppRepository
import com.medstock.app.util.DateUtil
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ExpiryViewModel(private val repo: AppRepository) : ViewModel() {

    /** Show items expiring within the next 180 days (and already-expired). */
    val expiring: StateFlow<List<Medicine>> = repo
        .expiringBefore(DateUtil.daysFromNow(180))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
