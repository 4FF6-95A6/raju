package com.medstock.app

import android.app.Application
import com.medstock.app.data.database.AppDatabase
import com.medstock.app.data.repository.AppRepository

class MedStockApp : Application() {

    lateinit var repository: AppRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.get(this)
        repository = AppRepository(
            medicineDao = db.medicineDao(),
            customerDao = db.customerDao(),
            saleDao = db.saleDao(),
            profileDao = db.profileDao()
        )
    }
}
