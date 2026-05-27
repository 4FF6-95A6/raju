package com.medstock.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medstock.app.data.entity.Medicine
import com.medstock.app.data.repository.AppRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StockViewModel(private val repo: AppRepository) : ViewModel() {

    val query = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val medicines: StateFlow<List<Medicine>> = query
        .flatMapLatest { q ->
            if (q.isBlank()) repo.allMedicines else repo.searchMedicines(q)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(q: String) { query.value = q }

    fun save(medicine: Medicine, onDone: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repo.upsertMedicine(medicine)
            onDone(id)
        }
    }

    fun delete(medicine: Medicine) {
        viewModelScope.launch { repo.deleteMedicine(medicine) }
    }

    suspend fun get(id: Long): Medicine? = repo.getMedicine(id)
}
