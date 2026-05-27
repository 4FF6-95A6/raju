package com.medstock.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.medstock.app.data.repository.AppRepository

class ViewModelFactory(private val repo: AppRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(repo) as T
            modelClass.isAssignableFrom(StockViewModel::class.java) -> StockViewModel(repo) as T
            modelClass.isAssignableFrom(CustomerViewModel::class.java) -> CustomerViewModel(repo) as T
            modelClass.isAssignableFrom(SaleViewModel::class.java) -> SaleViewModel(repo) as T
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> ProfileViewModel(repo) as T
            modelClass.isAssignableFrom(InvoiceViewModel::class.java) -> InvoiceViewModel(repo) as T
            modelClass.isAssignableFrom(PendingViewModel::class.java) -> PendingViewModel(repo) as T
            modelClass.isAssignableFrom(ExpiryViewModel::class.java) -> ExpiryViewModel(repo) as T
            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
