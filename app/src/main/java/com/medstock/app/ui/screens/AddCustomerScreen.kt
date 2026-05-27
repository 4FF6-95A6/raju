package com.medstock.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.medstock.app.data.entity.Customer
import com.medstock.app.ui.components.AppTextField
import com.medstock.app.ui.theme.Danger
import com.medstock.app.ui.theme.Primary
import com.medstock.app.viewmodel.CustomerViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomerScreen(
    vm: CustomerViewModel,
    customerId: Long?,
    onDone: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var gst by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var existing by remember { mutableStateOf<Customer?>(null) }

    LaunchedEffect(customerId) {
        if (customerId != null && customerId > 0) {
            vm.get(customerId)?.let { c ->
                existing = c
                name = c.name; phone = c.phone; address = c.address
                gst = c.gstNo; notes = c.notes
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existing == null) "Add Customer" else "Edit Customer") },
                navigationIcon = {
                    IconButton(onClick = onDone) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                actions = {
                    if (existing != null) {
                        IconButton(onClick = {
                            scope.launch { vm.delete(existing!!); onDone() }
                        }) { Icon(Icons.Default.Delete, null, tint = Danger) }
                    }
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppTextField("Customer Name *", name, { name = it })
            AppTextField("Phone", phone, { phone = it.filter { c -> c.isDigit() } }, keyboardType = KeyboardType.Phone)
            AppTextField("Address", address, { address = it }, singleLine = false)
            AppTextField("GST Number (optional)", gst, { gst = it.uppercase() })
            AppTextField("Notes", notes, { notes = it }, singleLine = false)

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    if (name.isBlank()) return@Button
                    val c = (existing ?: Customer(name = name)).copy(
                        name = name.trim(),
                        phone = phone.trim(),
                        address = address.trim(),
                        gstNo = gst.trim(),
                        notes = notes.trim()
                    )
                    vm.save(c) { onDone() }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text(if (existing == null) "Save Customer" else "Update Customer")
            }
        }
    }
}
