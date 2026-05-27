package com.medstock.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.medstock.app.ui.theme.Danger
import com.medstock.app.ui.theme.Primary
import com.medstock.app.ui.theme.PrimaryLight
import com.medstock.app.ui.theme.Success
import com.medstock.app.ui.theme.SuccessSoft
import com.medstock.app.util.DateUtil
import com.medstock.app.util.PdfGenerator
import com.medstock.app.util.formatRupees
import com.medstock.app.viewmodel.InvoiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(
    vm: InvoiceViewModel,
    saleId: Long,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(saleId) { vm.load(saleId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoice") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (state.loading || state.sale == null) {
            Column(modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        val sale = state.sale!!
        val items = state.items
        val profile = state.profile

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Success card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SuccessSoft)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, null, tint = Success)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Sale Saved", style = MaterialTheme.typography.titleMedium, color = Success)
                        Text("Invoice ${sale.invoiceNo}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(profile?.storeName?.takeIf { it.isNotBlank() } ?: "MedStock Pharmacy",
                        style = MaterialTheme.typography.titleLarge)
                    if (!profile?.address.isNullOrBlank()) Text(profile!!.address, style = MaterialTheme.typography.bodySmall)
                    if (!profile?.gstNo.isNullOrBlank()) Text("GSTIN: ${profile!!.gstNo}", style = MaterialTheme.typography.bodySmall)
                    if (!profile?.drugLicenseNo.isNullOrBlank()) Text("DL No: ${profile!!.drugLicenseNo}", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column { Text("Invoice", style = MaterialTheme.typography.bodySmall); Text(sale.invoiceNo, fontWeight = FontWeight.SemiBold) }
                        Column { Text("Date", style = MaterialTheme.typography.bodySmall); Text(DateUtil.format(sale.date), fontWeight = FontWeight.SemiBold) }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text("Customer: ${sale.customerName}", style = MaterialTheme.typography.bodyMedium)
                }
            }

            // Items
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Items", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    items.forEach { it ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(it.medicineName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Text(
                                    "Qty ${it.quantity} × ${it.unitPrice.formatRupees()} • GST ${it.gstPercent.toInt()}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(it.total.formatRupees(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Totals
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryLight)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Total("Subtotal", sale.subTotal.formatRupees())
                    if (sale.isInterState) Total("IGST", sale.gstAmount.formatRupees())
                    else {
                        val half = sale.gstAmount / 2.0
                        Total("CGST", half.formatRupees())
                        Total("SGST", (sale.gstAmount - half).formatRupees())
                    }
                    if (sale.discount > 0) Total("Discount", "- ${sale.discount.formatRupees()}")
                    Spacer(Modifier.height(4.dp))
                    Total("Grand Total", sale.total.formatRupees(), bold = true)
                    Spacer(Modifier.height(6.dp))
                    Total("Paid (${sale.paymentMode})", sale.paidAmount.formatRupees())
                    if (sale.pendingAmount > 0)
                        Total("Balance Due", sale.pendingAmount.formatRupees(), color = Danger)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDone,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Done") }
                Button(
                    onClick = {
                        try {
                            val file = PdfGenerator.generateInvoice(context, sale, items, profile)
                            PdfGenerator.shareInvoice(context, file)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.weight(2f).height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Icon(Icons.Default.Share, null); Spacer(Modifier.width(6.dp)); Text("Share PDF Invoice")
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun Total(label: String, value: String, bold: Boolean = false, color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label,
            style = if (bold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = color)
        Text(value,
            style = if (bold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = color)
    }
}
