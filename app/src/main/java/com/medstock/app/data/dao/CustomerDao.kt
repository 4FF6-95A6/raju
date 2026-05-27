package com.medstock.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.medstock.app.data.entity.Customer
import com.medstock.app.data.entity.CustomerPrice
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customer: Customer): Long

    @Update
    suspend fun update(customer: Customer)

    @Delete
    suspend fun delete(customer: Customer)

    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAll(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getById(id: Long): Customer?

    @Query("SELECT * FROM customers WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' ORDER BY name ASC")
    fun search(query: String): Flow<List<Customer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrice(price: CustomerPrice): Long

    @Query("DELETE FROM customer_prices WHERE customerId = :customerId AND medicineId = :medicineId")
    suspend fun deletePrice(customerId: Long, medicineId: Long)

    @Query("SELECT * FROM customer_prices WHERE customerId = :customerId")
    fun getPricesForCustomer(customerId: Long): Flow<List<CustomerPrice>>

    @Query("SELECT specialPrice FROM customer_prices WHERE customerId = :customerId AND medicineId = :medicineId LIMIT 1")
    suspend fun getSpecialPrice(customerId: Long, medicineId: Long): Double?
}
