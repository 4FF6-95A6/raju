package com.medstock.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.medstock.app.data.dao.CustomerDao
import com.medstock.app.data.dao.MedicineDao
import com.medstock.app.data.dao.ProfileDao
import com.medstock.app.data.dao.SaleDao
import com.medstock.app.data.entity.Customer
import com.medstock.app.data.entity.CustomerPrice
import com.medstock.app.data.entity.Medicine
import com.medstock.app.data.entity.Payment
import com.medstock.app.data.entity.Profile
import com.medstock.app.data.entity.Sale
import com.medstock.app.data.entity.SaleItem

@Database(
    entities = [
        Medicine::class,
        Customer::class,
        CustomerPrice::class,
        Sale::class,
        SaleItem::class,
        Payment::class,
        Profile::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun medicineDao(): MedicineDao
    abstract fun customerDao(): CustomerDao
    abstract fun saleDao(): SaleDao
    abstract fun profileDao(): ProfileDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "medstock.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = db
                db
            }
        }
    }
}
