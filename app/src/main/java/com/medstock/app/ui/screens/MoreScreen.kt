package com.medstock.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.medstock.app.ui.theme.Danger
import com.medstock.app.ui.theme.DangerSoft
import com.medstock.app.ui.theme.Info
import com.medstock.app.ui.theme.InfoSoft
import com.medstock.app.ui.theme.Primary
import com.medstock.app.ui.theme.PrimaryLight
import com.medstock.app.ui.theme.Warning
import com.medstock.app.ui.theme.WarningSoft

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onCustomers: () -> Unit,
    onPending: () -> Unit,
    onExpiry: () -> Unit,
    onProfile: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("More") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MoreItem("Customers", "Manage customers and special prices", Icons.Outlined.People, Primary, PrimaryLight, onCustomers)
            MoreItem("Pending Payments", "Track outstanding dues", Icons.Outlined.Receipt, Danger, DangerSoft, onPending)
            MoreItem("Expiry Alerts", "Stock expiring soon", Icons.Outlined.AccessTime, Warning, WarningSoft, onExpiry)
            MoreItem("My Profile / Store", "Store name, GSTIN, license", Icons.Outlined.AccountCircle, Info, InfoSoft, onProfile)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoreItem(title: String, subtitle: String, icon: ImageVector, accent: Color, soft: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onClick
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .padding(0.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier
                    .size(44.dp)
                    .padding(0.dp)) {
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = soft)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(icon, null, tint = accent)
                        }
                    }
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
