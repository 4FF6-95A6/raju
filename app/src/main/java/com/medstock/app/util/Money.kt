package com.medstock.app.util

fun Double.formatRupees(): String = "₹" + String.format("%,.2f", this)
fun Int.formatRupees(): String = "₹" + String.format("%,d", this)
