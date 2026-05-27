package com.medstock.app.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.medstock.app.data.entity.Sale
import com.medstock.app.ui.components.AppTextField
import com.medstock.app.ui.components.DropdownPicker
import com.medstock.app.ui.components.EmptyState
import com.medstock.app.ui.theme.Danger
import com.medstock.app.ui.theme.DangerSoft
import com.medstock.app.ui.theme.Primary
import com.medstock.app.util.DateUtil
import com.medstock.app.util.formatRupees
import com.medstock.app.viewmodel.PendingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingPaymentsScreen(
    vm: PendingViewModel,
    onBack: () -> Unit
) {
    val items by vm.pending.collectAsStateWithLifecycle()
    val total by vm.totalPending.collectAsStateWithLifecycle()
    var paying by remember { mutableStateOf<Sale?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pending Payments") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Card(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DangerSoft)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Total Outstanding", style = MaterialTheme.typography.bodyMedium, color = Danger)
                    Text(total.formatRupees(), style = MaterialTheme.typography.headlineMedium, color = Danger, fontWeight = FontWeight.Bold)
                }
            }
            if (items.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState("No pending dues", "All bills are settled.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items, key = { it.id }) { sale ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            onClick = { paying = sale }
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(sale.customerName, style = MaterialTheme.typography.titleSmall)
                                    Text("${sale.invoiceNo} • ${DateUtil.format(sale.date)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Total ${sale.total.formatRupees()} • Paid ${sale.paidAmount.formatRupees()}",
                                        style = MaterialTheme.typography.bodySmall)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Due", style = MaterialTheme.typography.bodySmall, color = Danger)
                                    Text(sale.pendingAmount.formatRupees(),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Danger, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    paying?.let { sale ->
        var amt by remember(sale.id) { mutableStateOf(sale.pendingAmount.toString()) }
        var mode by remember(sale.id) { mutableStateOf("CASH") }
        var note by remember(sale.id) { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { paying = null },
            confirmButton = {
                Button(
                    onClick = {
                        val a = amt.toDoubleOrNull() ?: 0.0
                        if (a > 0) vm.recordPayment(sale.id, a, mode, note) { paying = null }
                    },
                    enabled = (amt.toDoubleOrNull() ?: 0.0) > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) { Text("Record") }
            },
            dismissButton = { TextButton(onClick = { paying = null }) { Text("Cancel") } },
            title = { Text("Record Payment") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${sale.customerName} • ${sale.invoiceNo}")
                    Text("Pending: ${sale.pendingAmount.formatRupees()}", color = Danger)
                    Spacer(Modifier.height(4.dp))
                    AppTextField("Amount", amt, { amt = it.filter { c -> c.isDigit() || c == '.' } },
                        keyboardType = KeyboardType.Decimal)
                    DropdownPicker(
                        label = "Mode",
                        value = mode,
                        options = listOf("CASH", "UPI", "CARD", "BANK"),
                        optionLabel = { it },
                        onSelect = { mode = it }
                    )
                    AppTextField("Note (optional)", note, { note = it })
                }
            }
        )
    }
}
