package com.medstock.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medstock.app.data.entity.Profile
import com.medstock.app.data.entity.Sale
import com.medstock.app.data.entity.SaleItem
import com.medstock.app.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class InvoiceUiState(
    val sale: Sale? = null,
    val items: List<SaleItem> = emptyList(),
    val profile: Profile? = null,
    val loading: Boolean = true
)

class InvoiceViewModel(private val repo: AppRepository) : ViewModel() {

    private val _state = MutableStateFlow(InvoiceUiState())
    val state: StateFlow<InvoiceUiState> = _state.asStateFlow()

    fun load(saleId: Long) {
        viewModelScope.launch {
            val swi = repo.getSaleWithItems(saleId)
            val profile = repo.getProfile()
            _state.value = InvoiceUiState(
                sale = swi?.sale,
                items = swi?.items ?: emptyList(),
                profile = profile,
                loading = false
            )
        }
    }
}
