package com.medstock.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medstock.app.data.entity.Medicine
import com.medstock.app.data.entity.Profile
import com.medstock.app.data.entity.Sale
import com.medstock.app.data.repository.AppRepository
import com.medstock.app.util.DateUtil
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(private val repo: AppRepository) : ViewModel() {

    private val now = System.currentTimeMillis()

    val todaysSales: StateFlow<Double> = repo
        .totalSalesBetween(DateUtil.startOfDay(now), DateUtil.endOfDay(now))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val todaysSaleCount: StateFlow<Int> = repo
        .salesCountBetween(DateUtil.startOfDay(now), DateUtil.endOfDay(now))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val totalPending: StateFlow<Double> = repo
        .totalPending()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val expiringSoon: StateFlow<List<Medicine>> = repo
        .expiringBefore(DateUtil.daysFromNow(90))
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val lowStock: StateFlow<List<Medicine>> = repo
        .lowStock
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val recentSales: StateFlow<List<Sale>> = repo
        .allSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val profile: StateFlow<Profile?> = repo
        .profile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
