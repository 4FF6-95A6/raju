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
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CurrencyRupee
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.medstock.app.ui.components.SectionTitle
import com.medstock.app.ui.components.StatCard
import com.medstock.app.ui.components.StatusChip
import com.medstock.app.ui.theme.Danger
import com.medstock.app.ui.theme.DangerSoft
import com.medstock.app.ui.theme.Info
import com.medstock.app.ui.theme.InfoSoft
import com.medstock.app.ui.theme.Primary
import com.medstock.app.ui.theme.PrimaryDark
import com.medstock.app.ui.theme.PrimaryLight
import com.medstock.app.ui.theme.Success
import com.medstock.app.ui.theme.SuccessSoft
import com.medstock.app.ui.theme.Warning
import com.medstock.app.ui.theme.WarningSoft
import com.medstock.app.util.DateUtil
import com.medstock.app.util.formatRupees
import com.medstock.app.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    vm: HomeViewModel,
    onNewSale: () -> Unit,
    onAddStock: () -> Unit,
    onAddCustomer: () -> Unit,
    onExpiry: () -> Unit,
    onPending: () -> Unit,
    onInvoice: (Long) -> Unit
) {
    val todaysSales by vm.todaysSales.collectAsStateWithLifecycle()
    val saleCount by vm.todaysSaleCount.collectAsStateWithLifecycle()
    val pending by vm.totalPending.collectAsStateWithLifecycle()
    val expiring by vm.expiringSoon.collectAsStateWithLifecycle()
    val lowStock by vm.lowStock.collectAsStateWithLifecycle()
    val recent by vm.recentSales.collectAsStateWithLifecycle()
    val profile by vm.profile.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Greeting header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(Primary, PrimaryDark)))
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Column {
                    Text(
                        "Hello, ${profile?.ownerName?.takeIf { it.isNotBlank() } ?: "there"}",
                        style = MaterialTheme.typography.titleMedium,
                        color = PrimaryLight
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        profile?.storeName?.takeIf { it.isNotBlank() } ?: "MedStock Pharmacy",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        DateUtil.format(System.currentTimeMillis()),
                        style = MaterialTheme.typography.bodySmall,
                        color = PrimaryLight
                    )
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }

        item {
            Row(modifier = Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    title = "Today's Sales",
                    value = todaysSales.formatRupees(),
                    icon = Icons.Outlined.PointOfSale,
                    accent = Success,
                    softAccent = SuccessSoft,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Bills Today",
                    value = saleCount.toString(),
                    icon = Icons.Outlined.Receipt,
                    accent = Info,
                    softAccent = InfoSoft,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item { Spacer(Modifier.height(12.dp)) }

        item {
            Row(modifier = Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    title = "Pending Payments",
                    value = pending.formatRupees(),
                    icon = Icons.Outlined.CurrencyRupee,
                    accent = Danger,
                    softAccent = DangerSoft,
                    modifier = Modifier.weight(1f),
                    onClick = onPending
                )
                StatCard(
                    title = "Expiring Soon",
                    value = expiring.size.toString(),
                    icon = Icons.Outlined.AccessTime,
                    accent = Warning,
                    softAccent = WarningSoft,
                    modifier = Modifier.weight(1f),
                    onClick = onExpiry
                )
            }
        }

        item { Spacer(Modifier.height(20.dp)) }

        // Quick actions
        item { SectionTitle("Quick Actions") }

        item {
            Row(modifier = Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickAction("New Sale", Icons.Outlined.Add, Primary, Modifier.weight(1f), onNewSale)
                QuickAction("Add Stock", Icons.Outlined.Inventory2, Info, Modifier.weight(1f), onAddStock)
                QuickAction("Customer", Icons.Outlined.PersonAdd, Warning, Modifier.weight(1f), onAddCustomer)
            }
        }

        item { Spacer(Modifier.height(20.dp)) }

        // Alerts
        if (lowStock.isNotEmpty()) {
            item { SectionTitle("Low Stock") }
            items(lowStock.take(3)) { med ->
                Card(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp).fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.WarningAmber, null, tint = Warning)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(med.name, style = MaterialTheme.typography.titleSmall)
                            Text("Only ${med.quantity} left", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        StatusChip("LOW", Warning, WarningSoft)
                    }
                }
            }
            item { Spacer(Modifier.height(12.dp)) }
        }

        if (recent.isNotEmpty()) {
            item { SectionTitle("Recent Sales") }
            items(recent.take(5)) { sale ->
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    onClick = { onInvoice(sale.id) }
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(SuccessSoft, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Receipt, null, tint = Success)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(sale.invoiceNo, style = MaterialTheme.typography.titleSmall)
                            Text(
                                "${sale.customerName} • ${DateUtil.format(sale.date)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(sale.total.formatRupees(), style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickAction(
    label: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(color.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color)
            }
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}
