package com.medstock.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.medstock.app.data.entity.Profile
import com.medstock.app.data.entity.Sale
import com.medstock.app.data.entity.SaleItem
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {

    /**
     * Renders a simple A4-ish invoice PDF and returns the saved File.
     * Uses Android's built-in PdfDocument (no external license issues).
     */
    fun generateInvoice(
        context: Context,
        sale: Sale,
        items: List<SaleItem>,
        profile: Profile?
    ): File {
        val pageWidth = 595          // A4 width @ 72dpi
        val pageHeight = 842
        val margin = 32f

        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas

        val title = Paint().apply { textSize = 22f; isAntiAlias = true; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
        val h2 = Paint().apply { textSize = 14f; isAntiAlias = true; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
        val body = Paint().apply { textSize = 11f; isAntiAlias = true }
        val bodyBold = Paint().apply { textSize = 11f; isAntiAlias = true; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
        val small = Paint().apply { textSize = 9f; isAntiAlias = true }

        var y = margin + 24f
        val storeName = profile?.storeName?.takeIf { it.isNotBlank() } ?: "MedStock Pharmacy"
        canvas.drawText(storeName, margin, y, title)
        y += 18f
        if (!profile?.address.isNullOrBlank()) { canvas.drawText(profile!!.address, margin, y, body); y += 14f }
        val infoLine = buildString {
            if (!profile?.phone.isNullOrBlank()) append("Phone: ${profile!!.phone}  ")
            if (!profile?.email.isNullOrBlank()) append("Email: ${profile!!.email}")
        }
        if (infoLine.isNotBlank()) { canvas.drawText(infoLine, margin, y, body); y += 14f }
        val taxLine = buildString {
            if (!profile?.gstNo.isNullOrBlank()) append("GSTIN: ${profile!!.gstNo}   ")
            if (!profile?.drugLicenseNo.isNullOrBlank()) append("DL No: ${profile!!.drugLicenseNo}")
        }
        if (taxLine.isNotBlank()) { canvas.drawText(taxLine, margin, y, body); y += 16f }

        canvas.drawLine(margin, y, pageWidth - margin, y, body); y += 18f
        canvas.drawText("TAX INVOICE", (pageWidth / 2f) - 40f, y, h2); y += 18f

        // Invoice meta
        canvas.drawText("Invoice No: ${sale.invoiceNo}", margin, y, body)
        canvas.drawText("Date: ${DateUtil.format(sale.date)}", pageWidth - margin - 160f, y, body)
        y += 14f
        canvas.drawText("Customer: ${sale.customerName}", margin, y, body)
        canvas.drawText("Payment: ${sale.paymentMode}", pageWidth - margin - 160f, y, body)
        y += 18f

        // Table header
        canvas.drawLine(margin, y, pageWidth - margin, y, body); y += 14f
        canvas.drawText("#", margin, y, bodyBold)
        canvas.drawText("Item", margin + 24f, y, bodyBold)
        canvas.drawText("HSN", margin + 220f, y, bodyBold)
        canvas.drawText("Qty", margin + 270f, y, bodyBold)
        canvas.drawText("Rate", margin + 310f, y, bodyBold)
        canvas.drawText("GST%", margin + 360f, y, bodyBold)
        canvas.drawText("Amount", pageWidth - margin - 60f, y, bodyBold)
        y += 6f
        canvas.drawLine(margin, y, pageWidth - margin, y, body); y += 14f

        items.forEachIndexed { index, it ->
            canvas.drawText("${index + 1}", margin, y, body)
            canvas.drawText(it.medicineName.take(28), margin + 24f, y, body)
            canvas.drawText(it.hsnCode.ifBlank { "-" }, margin + 220f, y, small)
            canvas.drawText("${it.quantity}", margin + 270f, y, body)
            canvas.drawText(formatMoney(it.unitPrice), margin + 310f, y, body)
            canvas.drawText("${it.gstPercent.toInt()}%", margin + 360f, y, body)
            canvas.drawText(formatMoney(it.total), pageWidth - margin - 60f, y, body)
            y += 14f
        }
        y += 6f
        canvas.drawLine(margin, y, pageWidth - margin, y, body); y += 18f

        val totalsX = pageWidth - margin - 180f
        val valX = pageWidth - margin - 60f
        canvas.drawText("Subtotal", totalsX, y, body)
        canvas.drawText(formatMoney(sale.subTotal), valX, y, body); y += 14f
        if (sale.isInterState) {
            canvas.drawText("IGST", totalsX, y, body)
            canvas.drawText(formatMoney(sale.gstAmount), valX, y, body); y += 14f
        } else {
            val half = sale.gstAmount / 2.0
            canvas.drawText("CGST", totalsX, y, body)
            canvas.drawText(formatMoney(half), valX, y, body); y += 14f
            canvas.drawText("SGST", totalsX, y, body)
            canvas.drawText(formatMoney(sale.gstAmount - half), valX, y, body); y += 14f
        }
        if (sale.discount > 0) {
            canvas.drawText("Discount", totalsX, y, body)
            canvas.drawText("- ${formatMoney(sale.discount)}", valX, y, body); y += 14f
        }
        canvas.drawLine(totalsX - 10f, y - 2f, pageWidth - margin, y - 2f, body)
        canvas.drawText("Grand Total", totalsX, y + 12f, bodyBold)
        canvas.drawText(formatMoney(sale.total), valX, y + 12f, bodyBold)
        y += 28f

        canvas.drawText("Paid", totalsX, y, body)
        canvas.drawText(formatMoney(sale.paidAmount), valX, y, body); y += 14f
        if (sale.pendingAmount > 0) {
            canvas.drawText("Balance Due", totalsX, y, bodyBold)
            canvas.drawText(formatMoney(sale.pendingAmount), valX, y, bodyBold); y += 14f
        }

        y = (pageHeight - margin - 30f)
        canvas.drawLine(margin, y, pageWidth - margin, y, body); y += 14f
        canvas.drawText("Thank you for your business!", margin, y, small)
        canvas.drawText("Authorized Signatory", pageWidth - margin - 110f, y, small)

        doc.finishPage(page)

        val dir = File(context.filesDir, "invoices").apply { if (!exists()) mkdirs() }
        val outFile = File(dir, "Invoice-${sale.invoiceNo}.pdf")
        FileOutputStream(outFile).use { doc.writeTo(it) }
        doc.close()
        return outFile
    }

    fun shareInvoice(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Invoice").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    private fun formatMoney(v: Double): String = "Rs. " + String.format("%.2f", v)
}
