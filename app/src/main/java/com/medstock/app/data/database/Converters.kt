package com.medstock.app.data.database

import androidx.room.TypeConverter
import com.medstock.app.data.entity.PaymentStatus
import com.medstock.app.data.entity.UnitType

class Converters {
    @TypeConverter
    fun fromUnitType(value: UnitType): String = value.name

    @TypeConverter
    fun toUnitType(value: String): UnitType = runCatching { UnitType.valueOf(value) }.getOrDefault(UnitType.PIECES)

    @TypeConverter
    fun fromPaymentStatus(value: PaymentStatus): String = value.name

    @TypeConverter
    fun toPaymentStatus(value: String): PaymentStatus = runCatching { PaymentStatus.valueOf(value) }.getOrDefault(PaymentStatus.PAID)
}
