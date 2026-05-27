package com.medstock.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.medstock.app.data.entity.Medicine
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicineDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medicine: Medicine): Long

    @Update
    suspend fun update(medicine: Medicine)

    @Delete
    suspend fun delete(medicine: Medicine)

    @Query("SELECT * FROM medicines WHERE id = :id")
    suspend fun getById(id: Long): Medicine?

    @Query("SELECT * FROM medicines ORDER BY name ASC")
    fun getAll(): Flow<List<Medicine>>

    @Query("SELECT * FROM medicines WHERE name LIKE '%' || :query || '%' OR batchNo LIKE '%' || :query || '%' ORDER BY name ASC")
    fun search(query: String): Flow<List<Medicine>>

    @Query("SELECT * FROM medicines WHERE expiryDate > 0 AND expiryDate <= :before ORDER BY expiryDate ASC")
    fun getExpiringBefore(before: Long): Flow<List<Medicine>>

    @Query("SELECT * FROM medicines WHERE quantity <= lowStockThreshold ORDER BY quantity ASC")
    fun getLowStock(): Flow<List<Medicine>>

    @Query("SELECT COUNT(*) FROM medicines")
    fun count(): Flow<Int>

    @Query("UPDATE medicines SET quantity = quantity - :qty WHERE id = :id")
    suspend fun reduceStock(id: Long, qty: Int)
}
