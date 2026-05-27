package com.medstock.app.ui.screens

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.medstock.app.data.entity.Profile
import com.medstock.app.ui.components.AppTextField
import com.medstock.app.ui.theme.Primary
import com.medstock.app.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    vm: ProfileViewModel,
    onBack: () -> Unit
) {
    val profile by vm.profile.collectAsStateWithLifecycle()

    var ownerName by remember { mutableStateOf("") }
    var storeName by remember { mutableStateOf("") }
    var gst by remember { mutableStateOf("") }
    var dl by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var stateCode by remember { mutableStateOf("") }
    var prefix by remember { mutableStateOf("INV") }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(profile) {
        if (!loaded && profile != null) {
            profile?.let {
                ownerName = it.ownerName
                storeName = it.storeName
                gst = it.gstNo
                dl = it.drugLicenseNo
                address = it.address
                phone = it.phone
                email = it.email
                state = it.state
                stateCode = it.stateCode
                prefix = it.invoicePrefix.ifBlank { "INV" }
            }
            loaded = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile / Store") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
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
            Text("These details appear on every invoice.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            AppTextField("Owner / Pharmacist Name", ownerName, { ownerName = it })
            AppTextField("Store Name", storeName, { storeName = it })
            AppTextField("Address", address, { address = it }, singleLine = false)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppTextField("State", state, { state = it }, modifier = Modifier.weight(2f))
                AppTextField("Code", stateCode, { stateCode = it.filter(Char::isDigit) },
                    modifier = Modifier.weight(1f), keyboardType = KeyboardType.Number)
            }

            AppTextField("Phone", phone, { phone = it.filter(Char::isDigit) }, keyboardType = KeyboardType.Phone)
            AppTextField("Email", email, { email = it }, keyboardType = KeyboardType.Email)

            AppTextField("GSTIN", gst, { gst = it.uppercase() })
            AppTextField("Drug License No.", dl, { dl = it.uppercase() })
            AppTextField("Invoice Prefix (e.g. INV)", prefix, { prefix = it.uppercase() })

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    vm.save(
                        Profile(
                            id = 1,
                            ownerName = ownerName.trim(),
                            storeName = storeName.trim(),
                            gstNo = gst.trim(),
                            drugLicenseNo = dl.trim(),
                            address = address.trim(),
                            phone = phone.trim(),
                            email = email.trim(),
                            state = state.trim(),
                            stateCode = stateCode.trim(),
                            invoicePrefix = prefix.trim().ifBlank { "INV" }
                        )
                    ) { onBack() }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) { Text("Save Profile") }
        }
    }
}
