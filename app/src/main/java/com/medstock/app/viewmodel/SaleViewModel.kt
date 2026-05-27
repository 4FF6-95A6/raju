package com.medstock.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medstock.app.data.entity.Customer
import com.medstock.app.data.entity.Medicine
import com.medstock.app.data.entity.PaymentStatus
import com.medstock.app.data.entity.Sale
import com.medstock.app.data.entity.SaleItem
import com.medstock.app.data.repository.AppRepository
import com.medstock.app.util.GstCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CartLine(
    val medicine: Medicine,
    val quantity: Int,
    val unitPrice: Double,    // base price (excl. GST)
    val gstPercent: Double
) {
    val taxable: Double get() = unitPrice * quantity
    val gstAmount: Double get() = GstCalculator.round2(taxable * gstPercent / 100.0)
    val lineTotal: Double get() = GstCalculator.round2(taxable + gstAmount)
}

data class SaleUiState(
    val customer: Customer? = null,
    val cart: List<CartLine> = emptyList(),
    val paidAmount: Double = 0.0,
    val discount: Double = 0.0,
    val isInterState: Boolean = false,
    val paymentMode: String = "CASH",
    val savedSaleId: Long? = null
) {
    val subTotal: Double get() = cart.sumOf { it.taxable }
    val totalGst: Double get() = cart.sumOf { it.gstAmount }
    val grandTotal: Double get() = GstCalculator.round2(subTotal + totalGst - discount)
    val pending: Double get() = GstCalculator.round2((grandTotal - paidAmount).coerceAtLeast(0.0))
    val status: PaymentStatus get() = when {
        paidAmount >= grandTotal && grandTotal > 0 -> PaymentStatus.PAID
        paidAmount <= 0.0 -> PaymentStatus.PENDING
        else -> PaymentStatus.PARTIAL
    }
}

class SaleViewModel(private val repo: AppRepository) : ViewModel() {

    private val _state = MutableStateFlow(SaleUiState())
    val state: StateFlow<SaleUiState> = _state.asStateFlow()

    val allMedicines: StateFlow<List<Medicine>> = repo.allMedicines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allCustomers: StateFlow<List<Customer>> = repo.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setCustomer(c: Customer?) { _state.update { it.copy(customer = c) } }
    fun setPaid(amount: Double) { _state.update { it.copy(paidAmount = amount) } }
    fun setDiscount(amount: Double) { _state.update { it.copy(discount = amount) } }
    fun setInterState(value: Boolean) { _state.update { it.copy(isInterState = value) } }
    fun setPaymentMode(mode: String) { _state.update { it.copy(paymentMode = mode) } }

    fun addItem(medicine: Medicine, quantity: Int) {
        viewModelScope.launch {
            val customerId = _state.value.customer?.id
            val special = if (customerId != null) repo.specialPrice(customerId, medicine.id) else null
            // sellingPrice in DB is treated as base (excl. GST)
            val price = special ?: medicine.sellingPrice
            val line = CartLine(medicine, quantity, price, medicine.gstPercent)
            _state.update { st ->
                val existing = st.cart.indexOfFirst { it.medicine.id == medicine.id }
                if (existing >= 0) {
                    val updated = st.cart.toMutableList()
                    val cur = updated[existing]
                    updated[existing] = cur.copy(quantity = cur.quantity + quantity)
                    st.copy(cart = updated)
                } else st.copy(cart = st.cart + line)
            }
        }
    }

    fun updateQuantity(index: Int, qty: Int) {
        if (qty <= 0) { removeItem(index); return }
        _state.update { st ->
            val list = st.cart.toMutableList()
            if (index in list.indices) list[index] = list[index].copy(quantity = qty)
            st.copy(cart = list)
        }
    }

    fun updateUnitPrice(index: Int, price: Double) {
        _state.update { st ->
            val list = st.cart.toMutableList()
            if (index in list.indices) list[index] = list[index].copy(unitPrice = price)
            st.copy(cart = list)
        }
    }

    fun removeItem(index: Int) {
        _state.update { st ->
            val list = st.cart.toMutableList()
            if (index in list.indices) list.removeAt(index)
            st.copy(cart = list)
        }
    }

    fun reset() { _state.value = SaleUiState() }

    fun saveSale(onSaved: (Long) -> Unit) {
        viewModelScope.launch {
            val st = _state.value
            if (st.cart.isEmpty()) return@launch
            val profile = repo.getProfile()
            val invoiceNo = repo.nextInvoiceNumber(profile?.invoicePrefix?.ifBlank { "INV" } ?: "INV")
            val sale = Sale(
                invoiceNo = invoiceNo,
                customerId = st.customer?.id,
                customerName = st.customer?.name ?: "Walk-in Customer",
                subTotal = GstCalculator.round2(st.subTotal),
                gstAmount = GstCalculator.round2(st.totalGst),
                discount = st.discount,
                total = st.grandTotal,
                paidAmount = st.paidAmount,
                pendingAmount = st.pending,
                paymentStatus = st.status,
                paymentMode = st.paymentMode,
                isInterState = st.isInterState
            )
            val items = st.cart.map { line ->
                SaleItem(
                    saleId = 0L,
                    medicineId = line.medicine.id,
                    medicineName = line.medicine.name,
                    batchNo = line.medicine.batchNo,
                    hsnCode = line.medicine.hsnCode,
                    unitType = line.medicine.unitType.name,
                    quantity = line.quantity,
                    unitPrice = line.unitPrice,
                    gstPercent = line.gstPercent,
                    gstAmount = line.gstAmount,
                    total = line.lineTotal
                )
            }
            val id = repo.saveSale(sale, items)
            _state.update { it.copy(savedSaleId = id) }
            onSaved(id)
        }
    }
}
