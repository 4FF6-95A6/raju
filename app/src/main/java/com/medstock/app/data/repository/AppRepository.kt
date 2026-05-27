package com.medstock.app.data.repository

import com.medstock.app.data.dao.CustomerDao
import com.medstock.app.data.dao.MedicineDao
import com.medstock.app.data.dao.ProfileDao
import com.medstock.app.data.dao.SaleDao
import com.medstock.app.data.dao.SaleWithItems
import com.medstock.app.data.entity.Customer
import com.medstock.app.data.entity.CustomerPrice
import com.medstock.app.data.entity.Medicine
import com.medstock.app.data.entity.Payment
import com.medstock.app.data.entity.PaymentStatus
import com.medstock.app.data.entity.Profile
import com.medstock.app.data.entity.Sale
import com.medstock.app.data.entity.SaleItem
import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val medicineDao: MedicineDao,
    private val customerDao: CustomerDao,
    private val saleDao: SaleDao,
    private val profileDao: ProfileDao
) {
    // Medicine
    val allMedicines: Flow<List<Medicine>> = medicineDao.getAll()
    fun searchMedicines(q: String): Flow<List<Medicine>> = medicineDao.search(q)
    fun expiringBefore(t: Long): Flow<List<Medicine>> = medicineDao.getExpiringBefore(t)
    val lowStock: Flow<List<Medicine>> = medicineDao.getLowStock()
    suspend fun upsertMedicine(m: Medicine): Long =
        if (m.id == 0L) medicineDao.insert(m) else { medicineDao.update(m); m.id }
    suspend fun deleteMedicine(m: Medicine) = medicineDao.delete(m)
    suspend fun getMedicine(id: Long): Medicine? = medicineDao.getById(id)

    // Customer
    val allCustomers: Flow<List<Customer>> = customerDao.getAll()
    fun searchCustomers(q: String): Flow<List<Customer>> = customerDao.search(q)
    suspend fun upsertCustomer(c: Customer): Long =
        if (c.id == 0L) customerDao.insert(c) else { customerDao.update(c); c.id }
    suspend fun deleteCustomer(c: Customer) = customerDao.delete(c)
    suspend fun getCustomer(id: Long): Customer? = customerDao.getById(id)
    suspend fun setCustomerPrice(p: CustomerPrice) = customerDao.insertPrice(p)
    suspend fun removeCustomerPrice(customerId: Long, medicineId: Long) =
        customerDao.deletePrice(customerId, medicineId)
    fun pricesForCustomer(id: Long): Flow<List<CustomerPrice>> = customerDao.getPricesForCustomer(id)
    suspend fun specialPrice(customerId: Long, medicineId: Long): Double? =
        customerDao.getSpecialPrice(customerId, medicineId)

    // Sale
    val allSales: Flow<List<Sale>> = saleDao.getAllSales()
    val pendingSales: Flow<List<Sale>> = saleDao.getPendingSales()
    fun totalPending(): Flow<Double> = saleDao.getTotalPending()
    fun totalSalesBetween(s: Long, e: Long): Flow<Double> = saleDao.getTotalSalesBetween(s, e)
    fun salesCountBetween(s: Long, e: Long): Flow<Int> = saleDao.getSalesCountBetween(s, e)
    fun salesByCustomer(id: Long): Flow<List<Sale>> = saleDao.getSalesByCustomer(id)
    suspend fun getSaleWithItems(id: Long): SaleWithItems? = saleDao.getSaleWithItems(id)
    suspend fun getSaleItems(id: Long): List<SaleItem> = saleDao.getItemsForSale(id)

    suspend fun saveSale(sale: Sale, items: List<SaleItem>): Long {
        val saleId = saleDao.insertSale(sale)
        val withSaleId = items.map { it.copy(saleId = saleId) }
        saleDao.insertItems(withSaleId)
        // reduce stock
        items.forEach { medicineDao.reduceStock(it.medicineId, it.quantity) }
        return saleId
    }

    suspend fun nextInvoiceNumber(prefix: String): String {
        val last = saleDao.getLastSaleId()
        val next = last + 1
        return "$prefix-${next.toString().padStart(5, '0')}"
    }

    suspend fun recordPayment(saleId: Long, amount: Double, mode: String, note: String) {
        saleDao.insertPayment(Payment(saleId = saleId, amount = amount, mode = mode, note = note))
        val sale = saleDao.getSaleById(saleId) ?: return
        val newPending = (sale.pendingAmount - amount).coerceAtLeast(0.0)
        val newPaid = sale.paidAmount + amount
        val status = when {
            newPending <= 0.0 -> PaymentStatus.PAID
            newPaid > 0 -> PaymentStatus.PARTIAL
            else -> PaymentStatus.PENDING
        }
        saleDao.updateSale(sale.copy(paidAmount = newPaid, pendingAmount = newPending, paymentStatus = status))
    }

    fun paymentsForSale(saleId: Long): Flow<List<Payment>> = saleDao.getPaymentsForSale(saleId)

    // Profile
    val profile: Flow<Profile?> = profileDao.observe()
    suspend fun saveProfile(p: Profile) = profileDao.upsert(p.copy(id = 1))
    suspend fun getProfile(): Profile? = profileDao.get()
}
