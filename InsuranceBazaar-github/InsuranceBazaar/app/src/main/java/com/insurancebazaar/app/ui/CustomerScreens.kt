package com.insurancebazaar.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.insurancebazaar.app.data.Customer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListScreen(vm: MainViewModel, onAdd: () -> Unit, onEdit: (Long) -> Unit) {
    val customers by vm.customers.collectAsState()
    val query by vm.customerQuery.collectAsState()
    var toDelete by remember { mutableStateOf<Customer?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Customers") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) { Icon(Icons.Default.Add, "Add customer") }
        }
    ) { pad ->
        Column(Modifier.padding(pad)) {
            OutlinedTextField(
                value = query,
                onValueChange = { vm.customerQuery.value = it },
                label = { Text("Search by name or phone") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )
            if (customers.isEmpty()) {
                EmptyState(
                    if (query.isBlank()) "No customers yet. Tap + to add your first customer."
                    else "No customers match your search."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(customers, key = { it.id }) { c ->
                        Card(Modifier.fillMaxWidth()) {
                            Row(
                                Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(c.name, style = MaterialTheme.typography.titleMedium)
                                    Text(c.phone)
                                    if (c.email.isNotBlank()) Text(c.email)
                                    if (c.address.isNotBlank()) Text(c.address, style = MaterialTheme.typography.bodySmall)
                                }
                                IconButton(onClick = { onEdit(c.id) }) { Icon(Icons.Default.Edit, "Edit") }
                                IconButton(onClick = { toDelete = c }) { Icon(Icons.Default.Delete, "Delete") }
                            }
                        }
                    }
                }
            }
        }
    }

    toDelete?.let { c ->
        ConfirmDialog(
            title = "Delete customer?",
            text = "${c.name} and all of their policies will be permanently deleted.",
            confirmLabel = "Delete",
            onConfirm = {
                vm.deleteCustomer(c)
                toDelete = null
            },
            onDismiss = { toDelete = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerFormScreen(id: Long, vm: MainViewModel, onBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(id) {
        if (id != 0L) {
            vm.getCustomer(id)?.let {
                name = it.name
                phone = it.phone
                email = it.email
                address = it.address
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (id == 0L) "Add customer" else "Edit customer") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                }
            )
        }
    ) { pad ->
        Column(
            Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name, onValueChange = { name = it }, label = { Text("Full name *") },
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = phone, onValueChange = { phone = it }, label = { Text("Phone *") },
                singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = email, onValueChange = { email = it }, label = { Text("Email") },
                singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = address, onValueChange = { address = it }, label = { Text("Address") },
                minLines = 2, modifier = Modifier.fillMaxWidth()
            )
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(
                onClick = {
                    error = when {
                        name.isBlank() -> "Name is required."
                        phone.filter { it.isDigit() }.length < 7 -> "Enter a valid phone number."
                        else -> null
                    }
                    if (error == null) {
                        vm.saveCustomer(
                            Customer(id, name.trim(), phone.trim(), email.trim(), address.trim()),
                            onBack
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save") }
        }
    }
}
