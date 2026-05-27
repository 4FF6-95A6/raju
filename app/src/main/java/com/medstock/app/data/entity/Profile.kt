package com.medstock.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class Profile(
    @PrimaryKey val id: Int = 1,    // always 1 - single row
    val ownerName: String = "",
    val storeName: String = "",
    val gstNo: String = "",
    val drugLicenseNo: String = "",
    val address: String = "",
    val phone: String = "",
    val email: String = "",
    val state: String = "",
    val stateCode: String = "",
    val invoicePrefix: String = "INV"
)
