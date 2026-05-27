package com.medstock.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.medstock.app.data.entity.Customer
import com.medstock.app.data.entity.Medicine
import com.medstock.app.ui.components.AppTextField
import com.medstock.app.ui.components.DropdownPicker
import com.medstock.app.ui.theme.Danger
import com.medstock.app.ui.theme.InfoSoft
import com.medstock.app.ui.theme.Primary
import com.medstock.app.ui.theme.PrimaryLight
import com.medstock.app.util.formatRupees
import com.medstock.app.viewmodel.SaleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSaleScreen(
    vm: SaleViewModel,
    onCancel: () -> Unit,
    onInvoice: (Long) -> Unit
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val medicines by vm.allMedicines.collectAsStateWithLifecycle()
    val customers by vm.allCustomers.collectAsStateWithLifecycle()

    var showAddItem by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        snapshotFlow { state.savedSaleId }
            .collect { id -> if (id != null) onInvoice(id) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Sale") },
                navigationIcon = {
                    IconButton(onClick = onCancel) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Customer
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                DropdownPicker(
                    label = "Customer",
                    value = state.customer,
                    options = listOf<Customer?>(null) + customers,
                    optionLabel = { it?.name ?: "Walk-in Customer" },
                    onSelect = { vm.setCustomer(it) }
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = state.isInterState, onCheckedChange = { vm.setInterState(it) })
                    Text("Inter-state sale (IGST)", style = MaterialTheme.typography.bodyMedium)
                }
            }

            // Items
            Card(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Items", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        Button(
                            onClick = { showAddItem = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Icon(Icons.Default.Add, null); Spacer(Modifier.width(4.dp)); Text("Add")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    if (state.cart.isEmpty()) {
                        Text(
                            "No items yet. Tap Add to put medicines in this bill.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(8.dp)
                        )
                    } else {
                        LazyColumn(modifier = Modifier.heightLimited(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            itemsIndexed(state.cart) { index, line ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(InfoSoft, RoundedCornerShape(10.dp))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(line.medicine.name, style = MaterialTheme.typography.titleSmall)
                                        Text(
                                            "Qty ${line.quantity} × ${line.unitPrice.formatRupees()} • GST ${line.gstPercent.toInt()}%",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            "Line: ${line.lineTotal.formatRupees()}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    IconButton(onClick = { vm.removeItem(index) }) {
                                        Icon(Icons.Default.Close, null, tint = Danger)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Totals
            Card(
                modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryLight)
            ) {
                Column(Modifier.padding(14.dp)) {
                    TotalRow("Subtotal", state.subTotal.formatRupees())
                    if (state.isInterState) TotalRow("IGST", state.totalGst.formatRupees())
                    else {
                        TotalRow("CGST", (state.totalGst / 2).formatRupees())
                        TotalRow("SGST", (state.totalGst - state.totalGst / 2).formatRupees())
                    }
                    Row {
                        OutlinedTextField(
                            value = if (state.discount == 0.0) "" else state.discount.toString(),
                            onValueChange = { vm.setDiscount(it.toDoubleOrNull() ?: 0.0) },
                            placeholder = { Text("Discount") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    TotalRow("Grand Total", state.grandTotal.formatRupees(), bold = true)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Payment
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                DropdownPicker(
                    label = "Payment Mode",
                    value = state.paymentMode,
                    options = listOf("CASH", "UPI", "CARD", "CREDIT"),
                    optionLabel = { it },
                    onSelect = { vm.setPaymentMode(it) }
                )
                Spacer(Modifier.height(8.dp))
                AppTextField(
                    label = "Paid Amount",
                    value = if (state.paidAmount == 0.0) "" else state.paidAmount.toString(),
                    onChange = { vm.setPaid(it.toDoubleOrNull() ?: 0.0) },
                    keyboardType = KeyboardType.Decimal
                )
                Spacer(Modifier.height(4.dp))
                if (state.pending > 0) {
                    Text(
                        "Balance Due: ${state.pending.formatRupees()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Danger
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { vm.reset(); onCancel() },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Cancel") }

                Button(
                    onClick = { vm.saveSale {} },
                    enabled = state.cart.isNotEmpty(),
                    modifier = Modifier.weight(2f).height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Icon(Icons.Outlined.Receipt, null); Spacer(Modifier.width(6.dp))
                    Text("Save & Generate Invoice")
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    if (showAddItem) {
        AddItemDialog(
            medicines = medicines,
            onAdd = { med, qty -> vm.addItem(med, qty); showAddItem = false },
            onDismiss = { showAddItem = false }
        )
    }
}

@Composable
private fun TotalRow(label: String, value: String, bold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = if (bold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text(value, style = if (bold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddItemDialog(
    medicines: List<Medicine>,
    onAdd: (Medicine, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selected by remember { mutableStateOf<Medicine?>(null) }
    var qty by remember { mutableStateOf("1") }
    var search by remember { mutableStateOf("") }
    val filtered = remember(search, medicines) {
        if (search.isBlank()) medicines else medicines.filter {
            it.name.contains(search, ignoreCase = true) || it.batchNo.contains(search, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val q = qty.toIntOrNull() ?: 0
                    if (selected != null && q > 0) onAdd(selected!!, q)
                },
                enabled = selected != null && (qty.toIntOrNull() ?: 0) > 0
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text("Add Item") },
        text = {
            Column {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text("Search medicine") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.heightLimited()) {
                    itemsIndexed(filtered) { _, med ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (selected?.id == med.id) PrimaryLight else MaterialTheme.colorScheme.surface,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp)
                                .clickable { selected = med },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(med.name, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "${med.unitType.name} • Stock ${med.quantity} • ${med.sellingPrice.formatRupees()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = qty,
                    onValueChange = { qty = it.filter(Char::isDigit) },
                    label = { Text("Quantity") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    )
}

private fun Modifier.heightLimited(): Modifier = this.height(220.dp)
