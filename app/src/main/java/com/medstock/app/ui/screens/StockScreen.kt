package com.medstock.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.medstock.app.ui.components.EmptyState
import com.medstock.app.ui.components.SearchBar
import com.medstock.app.ui.components.StatusChip
import com.medstock.app.ui.theme.Danger
import com.medstock.app.ui.theme.DangerSoft
import com.medstock.app.ui.theme.InfoSoft
import com.medstock.app.ui.theme.Primary
import com.medstock.app.ui.theme.Success
import com.medstock.app.ui.theme.SuccessSoft
import com.medstock.app.ui.theme.Warning
import com.medstock.app.ui.theme.WarningSoft
import com.medstock.app.util.DateUtil
import com.medstock.app.util.formatRupees
import com.medstock.app.viewmodel.StockViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockScreen(
    vm: StockViewModel,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit
) {
    val items by vm.medicines.collectAsStateWithLifecycle()
    val query by vm.query.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stock Inventory") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAdd,
                containerColor = Primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, null); Spacer(Modifier.width(6.dp)); Text("Add Stock")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Spacer(Modifier.height(8.dp))
            SearchBar(value = query, onChange = vm::setQuery, placeholder = "Search medicine or batch")
            Spacer(Modifier.height(8.dp))
            if (items.isEmpty()) {
                EmptyState("No stock yet", "Tap the + button to add your first medicine")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items, key = { it.id }) { med ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            onClick = { onEdit(med.id) }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(InfoSoft, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) { Icon(Icons.Outlined.Medication, null, tint = Primary) }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(med.name, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        buildString {
                                            if (med.batchNo.isNotBlank()) append("Batch ${med.batchNo} • ")
                                            append("Exp ${DateUtil.format(med.expiryDate)}")
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "${med.unitType.name} • Qty: ${med.quantity} • ${med.sellingPrice.formatRupees()} • GST ${med.gstPercent.toInt()}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                val daysLeft = DateUtil.daysBetween(System.currentTimeMillis(), med.expiryDate)
                                when {
                                    med.expiryDate <= 0L -> {}
                                    daysLeft < 0 -> StatusChip("EXPIRED", Danger, DangerSoft)
                                    daysLeft <= 30 -> StatusChip("≤30D", Danger, DangerSoft)
                                    daysLeft <= 90 -> StatusChip("≤90D", Warning, WarningSoft)
                                    else -> StatusChip("OK", Success, SuccessSoft)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
