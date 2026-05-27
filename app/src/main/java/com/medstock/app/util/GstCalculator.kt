package com.medstock.app.util

object GstCalculator {

    data class Breakdown(
        val taxableAmount: Double,
        val gstAmount: Double,
        val cgst: Double,
        val sgst: Double,
        val igst: Double,
        val total: Double
    )

    /** Treats unitPrice as base (excl. GST). */
    fun calculate(
        unitPrice: Double,
        quantity: Int,
        gstPercent: Double,
        isInterState: Boolean = false
    ): Breakdown {
        val taxable = unitPrice * quantity
        val gst = round2(taxable * gstPercent / 100.0)
        val cgst = if (isInterState) 0.0 else round2(gst / 2)
        val sgst = if (isInterState) 0.0 else round2(gst - cgst)
        val igst = if (isInterState) gst else 0.0
        val total = round2(taxable + gst)
        return Breakdown(round2(taxable), gst, cgst, sgst, igst, total)
    }

    /** Treats price as inclusive of GST and extracts base + tax. */
    fun fromInclusive(
        inclusivePrice: Double,
        quantity: Int,
        gstPercent: Double,
        isInterState: Boolean = false
    ): Breakdown {
        val total = round2(inclusivePrice * quantity)
        val taxable = round2(total * 100.0 / (100.0 + gstPercent))
        val gst = round2(total - taxable)
        val cgst = if (isInterState) 0.0 else round2(gst / 2)
        val sgst = if (isInterState) 0.0 else round2(gst - cgst)
        val igst = if (isInterState) gst else 0.0
        return Breakdown(taxable, gst, cgst, sgst, igst, total)
    }

    fun round2(v: Double): Double = (Math.round(v * 100.0)) / 100.0

    val gstRates: List<Double> = listOf(0.0, 5.0, 12.0, 18.0, 28.0)
}
