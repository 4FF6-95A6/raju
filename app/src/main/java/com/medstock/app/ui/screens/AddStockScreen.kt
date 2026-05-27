package com.medstock.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.medstock.app.data.entity.Medicine
import com.medstock.app.data.entity.UnitType
import com.medstock.app.ui.components.AppTextField
import com.medstock.app.ui.components.DatePickerField
import com.medstock.app.ui.components.DropdownPicker
import com.medstock.app.ui.theme.Danger
import com.medstock.app.ui.theme.Primary
import com.medstock.app.util.GstCalculator
import com.medstock.app.viewmodel.StockViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStockScreen(
    vm: StockViewModel,
    medicineId: Long?,    // null = add new
    onDone: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var batch by remember { mutableStateOf("") }
    var manufacturer by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf(UnitType.PIECES) }
    var quantity by remember { mutableStateOf("0") }
    var piecesPerUnit by remember { mutableStateOf("1") }
    var purchasePrice by remember { mutableStateOf("") }
    var sellingPrice by remember { mutableStateOf("") }
    var gst by remember { mutableStateOf(12.0) }
    var hsn by remember { mutableStateOf("") }
    var expiry by remember { mutableLongStateOf(0L) }
    var lowThreshold by remember { mutableStateOf("10") }
    var existing by remember { mutableStateOf<Medicine?>(null) }

    LaunchedEffect(medicineId) {
        if (medicineId != null && medicineId > 0) {
            vm.get(medicineId)?.let { m ->
                existing = m
                name = m.name
                batch = m.batchNo
                manufacturer = m.manufacturer
                unit = m.unitType
                quantity = m.quantity.toString()
                piecesPerUnit = m.piecesPerUnit.toString()
                purchasePrice = m.purchasePrice.toString()
                sellingPrice = m.sellingPrice.toString()
                gst = m.gstPercent
                hsn = m.hsnCode
                expiry = m.expiryDate
                lowThreshold = m.lowStockThreshold.toString()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existing == null) "Add Stock" else "Edit Stock") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    if (existing != null) {
                        IconButton(onClick = {
                            scope.launch { vm.delete(existing!!); onDone() }
                        }) {
                            Icon(Icons.Default.Delete, null, tint = Danger)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
            AppTextField("Medicine Name *", name, { name = it })
            AppTextField("Batch Number", batch, { batch = it })
            AppTextField("Manufacturer", manufacturer, { manufacturer = it })

            DropdownPicker(
                label = "Unit Type",
                value = unit,
                options = UnitType.entries,
                optionLabel = { it.name },
                onSelect = { unit = it }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppTextField("Quantity", quantity, { quantity = it.filter(Char::isDigit) },
                    Modifier.weight(1f), keyboardType = KeyboardType.Number)
                AppTextField(
                    if (unit == UnitType.BOX) "Pcs / Box" else "Pcs / Unit",
                    piecesPerUnit, { piecesPerUnit = it.filter(Char::isDigit) },
                    Modifier.weight(1f), keyboardType = KeyboardType.Number
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppTextField("Purchase Price", purchasePrice, { purchasePrice = it.filter { c -> c.isDigit() || c == '.' } },
                    Modifier.weight(1f), keyboardType = KeyboardType.Decimal)
                AppTextField("Selling (MRP)", sellingPrice, { sellingPrice = it.filter { c -> c.isDigit() || c == '.' } },
                    Modifier.weight(1f), keyboardType = KeyboardType.Decimal)
            }

            DropdownPicker(
                label = "GST %",
                value = gst,
                options = GstCalculator.gstRates,
                optionLabel = { "${it.toInt()}%" },
                onSelect = { gst = it }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppTextField("HSN Code", hsn, { hsn = it }, Modifier.weight(1f))
                AppTextField("Low Alert Qty", lowThreshold,
                    { lowThreshold = it.filter(Char::isDigit) },
                    Modifier.weight(1f), keyboardType = KeyboardType.Number)
            }

            DatePickerField("Expiry Date", expiry) { expiry = it }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    if (name.isBlank()) return@Button
                    val m = (existing ?: Medicine(name = name)).copy(
                        name = name.trim(),
                        batchNo = batch.trim(),
                        manufacturer = manufacturer.trim(),
                        unitType = unit,
                        quantity = quantity.toIntOrNull() ?: 0,
                        piecesPerUnit = piecesPerUnit.toIntOrNull() ?: 1,
                        purchasePrice = purchasePrice.toDoubleOrNull() ?: 0.0,
                        sellingPrice = sellingPrice.toDoubleOrNull() ?: 0.0,
                        gstPercent = gst,
                        hsnCode = hsn.trim(),
                        expiryDate = expiry,
                        lowStockThreshold = lowThreshold.toIntOrNull() ?: 10
                    )
                    vm.save(m) { onDone() }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text(if (existing == null) "Save Stock" else "Update Stock")
            }
        }
    }
}
