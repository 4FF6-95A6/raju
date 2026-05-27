package com.medstock.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medstock.app.data.entity.Customer
import com.medstock.app.data.entity.CustomerPrice
import com.medstock.app.data.repository.AppRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CustomerViewModel(private val repo: AppRepository) : ViewModel() {

    val query = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val customers: StateFlow<List<Customer>> = query
        .flatMapLatest { q -> if (q.isBlank()) repo.allCustomers else repo.searchCustomers(q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(q: String) { query.value = q }

    fun save(customer: Customer, onDone: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repo.upsertCustomer(customer)
            onDone(id)
        }
    }

    fun delete(customer: Customer) {
        viewModelScope.launch { repo.deleteCustomer(customer) }
    }

    suspend fun get(id: Long): Customer? = repo.getCustomer(id)

    fun pricesFor(id: Long): kotlinx.coroutines.flow.Flow<List<CustomerPrice>> =
        repo.pricesForCustomer(id)

    fun setSpecialPrice(customerId: Long, medicineId: Long, price: Double) {
        viewModelScope.launch {
            repo.setCustomerPrice(CustomerPrice(customerId = customerId, medicineId = medicineId, specialPrice = price))
        }
    }

    fun removeSpecialPrice(customerId: Long, medicineId: Long) {
        viewModelScope.launch { repo.removeCustomerPrice(customerId, medicineId) }
    }
}
