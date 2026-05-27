package com.medstock.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UnitType { BOX, PIECES, BOTTLE, STRIP }

@Entity(tableName = "medicines")
data class Medicine(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val batchNo: String = "",
    val manufacturer: String = "",
    val unitType: UnitType = UnitType.PIECES,
    val quantity: Int = 0,
    val piecesPerUnit: Int = 1,        // e.g. 10 tablets per strip
    val purchasePrice: Double = 0.0,
    val sellingPrice: Double = 0.0,    // MRP
    val gstPercent: Double = 12.0,
    val hsnCode: String = "",
    val expiryDate: Long = 0L,         // epoch millis
    val addedDate: Long = System.currentTimeMillis(),
    val lowStockThreshold: Int = 10
)
