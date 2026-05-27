package com.medstock.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PaymentStatus { PAID, PARTIAL, PENDING }

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNo: String,
    val customerId: Long? = null,
    val customerName: String = "Walk-in Customer",
    val date: Long = System.currentTimeMillis(),
    val subTotal: Double,
    val gstAmount: Double,
    val discount: Double = 0.0,
    val total: Double,
    val paidAmount: Double,
    val pendingAmount: Double,
    val paymentStatus: PaymentStatus = PaymentStatus.PAID,
    val paymentMode: String = "CASH",   // CASH / UPI / CARD
    val isInterState: Boolean = false   // true => IGST, false => CGST+SGST
)
