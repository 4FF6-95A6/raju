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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.medstock.app.ui.components.StatusChip
import com.medstock.app.ui.theme.Danger
import com.medstock.app.ui.theme.DangerSoft
import com.medstock.app.ui.theme.Success
import com.medstock.app.ui.theme.SuccessSoft
import com.medstock.app.ui.theme.Warning
import com.medstock.app.ui.theme.WarningSoft
import com.medstock.app.util.DateUtil
import com.medstock.app.viewmodel.ExpiryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpiryScreen(
    vm: ExpiryViewModel,
    onBack: () -> Unit
) {
    val items by vm.expiring.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expiry Alerts") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState("All clear", "No medicines expiring within 6 months.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items, key = { it.id }) { med ->
                    val daysLeft = DateUtil.daysBetween(System.currentTimeMillis(), med.expiryDate)
                    val (color, soft, label) = when {
                        daysLeft < 0 -> Triple(Danger, DangerSoft, "EXPIRED")
                        daysLeft <= 30 -> Triple(Danger, DangerSoft, "${daysLeft}D LEFT")
                        daysLeft <= 90 -> Triple(Warning, WarningSoft, "${daysLeft}D LEFT")
                        else -> Triple(Success, SuccessSoft, "${daysLeft}D LEFT")
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(44.dp).background(soft, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) { Icon(Icons.Outlined.AccessTime, null, tint = color) }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(med.name, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "Batch ${med.batchNo.ifBlank { "-" }} • Qty ${med.quantity} • Exp ${DateUtil.format(med.expiryDate)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            StatusChip(label, color, soft)
                        }
                    }
                }
            }
        }
    }
}
