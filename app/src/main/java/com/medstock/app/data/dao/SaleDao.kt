package com.medstock.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.medstock.app.data.entity.Payment
import com.medstock.app.data.entity.Sale
import com.medstock.app.data.entity.SaleItem
import kotlinx.coroutines.flow.Flow

data class SaleWithItems(
    @androidx.room.Embedded val sale: Sale,
    @androidx.room.Relation(parentColumn = "id", entityColumn = "saleId")
    val items: List<SaleItem>
)

@Dao
interface SaleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: Sale): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<SaleItem>)

    @Update
    suspend fun updateSale(sale: Sale)

    @Delete
    suspend fun deleteSale(sale: Sale)

    @Query("SELECT * FROM sales ORDER BY date DESC")
    fun getAllSales(): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getSaleById(id: Long): Sale?

    @Transaction
    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getSaleWithItems(id: Long): SaleWithItems?

    @Query("SELECT * FROM sales WHERE pendingAmount > 0 ORDER BY date DESC")
    fun getPendingSales(): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE customerId = :customerId ORDER BY date DESC")
    fun getSalesByCustomer(customerId: Long): Flow<List<Sale>>

    @Query("SELECT IFNULL(SUM(total), 0) FROM sales WHERE date BETWEEN :start AND :end")
    fun getTotalSalesBetween(start: Long, end: Long): Flow<Double>

    @Query("SELECT IFNULL(SUM(pendingAmount), 0) FROM sales WHERE pendingAmount > 0")
    fun getTotalPending(): Flow<Double>

    @Query("SELECT COUNT(*) FROM sales WHERE date BETWEEN :start AND :end")
    fun getSalesCountBetween(start: Long, end: Long): Flow<Int>

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getItemsForSale(saleId: Long): List<SaleItem>

    @Query("SELECT IFNULL(MAX(id), 0) FROM sales")
    suspend fun getLastSaleId(): Long

    // Payments
    @Insert
    suspend fun insertPayment(payment: Payment): Long

    @Query("SELECT * FROM payments WHERE saleId = :saleId ORDER BY date DESC")
    fun getPaymentsForSale(saleId: Long): Flow<List<Payment>>
}
