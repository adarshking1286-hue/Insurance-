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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.insurancebazaar.app.data.*

private val PolicyTypes = listOf("Motor - Comprehensive", "Motor - Third Party", "Health", "Life", "Other")

@Composable
private fun statusColor(status: PolicyStatus): Color = when (status) {
    PolicyStatus.ACTIVE -> Color(0xFF2E7D32)
    PolicyStatus.EXPIRING -> Color(0xFFEF6C00)
    PolicyStatus.EXPIRED -> MaterialTheme.colorScheme.error
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PolicyListScreen(vm: MainViewModel, onAdd: () -> Unit, onEdit: (Long) -> Unit) {
    val policies by vm.policies.collectAsState()
    val query by vm.policyQuery.collectAsState()
    val filter by vm.policyFilter.collectAsState()
    var toDelete by remember { mutableStateOf<Policy?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Policies") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) { Icon(Icons.Default.Add, "Add policy") }
        }
    ) { pad ->
        Column(Modifier.padding(pad)) {
            OutlinedTextField(
                value = query,
                onValueChange = { vm.policyQuery.value = it },
                label = { Text("Search vehicle, insurer or customer") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Active", "Expired").forEachIndexed { index, label ->
                    FilterChip(
                        selected = filter == index,
                        onClick = { vm.policyFilter.value = index },
                        label = { Text(label) }
                    )
                }
            }
            if (policies.isEmpty()) {
                EmptyState(
                    if (query.isBlank() && filter == 0) "No policies yet. Add a customer first, then tap + to add a policy."
                    else "No policies match your search."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(policies, key = { it.policy.id }) { item ->
                        val p = item.policy
                        val status = p.status()
                        Card(Modifier.fillMaxWidth()) {
                            Row(
                                Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(item.customerName, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        buildString {
                                            append(p.policyType)
                                            if (p.vehicleNumber.isNotBlank()) append(" • ${p.vehicleNumber}")
                                        }
                                    )
                                    Text("Insurer: ${p.insurer}", style = MaterialTheme.typography.bodySmall)
                                    Text("Premium: ${Fmt.money(p.premium)}", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        "Expires ${Fmt.date(p.expiryDate)} • ${status.label}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = statusColor(status)
                                    )
                                }
                                IconButton(onClick = { onEdit(p.id) }) { Icon(Icons.Default.Edit, "Edit") }
                                IconButton(onClick = { toDelete = p }) { Icon(Icons.Default.Delete, "Delete") }
                            }
                        }
                    }
                }
            }
        }
    }

    toDelete?.let { p ->
        ConfirmDialog(
            title = "Delete policy?",
            text = "This policy will be permanently deleted.",
            confirmLabel = "Delete",
            onConfirm = {
                vm.deletePolicy(p)
                toDelete = null
            },
            onDismiss = { toDelete = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PolicyFormScreen(id: Long, vm: MainViewModel, onBack: () -> Unit) {
    val customers by vm.allCustomers.collectAsState()
    var customerId by remember { mutableLongStateOf(0L) }
    var vehicle by remember { mutableStateOf("") }
    var insurer by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(PolicyTypes.first()) }
    var start by remember { mutableLongStateOf(startOfToday()) }
    var expiry by remember { mutableLongStateOf(startOfToday() + 365 * DAY_MS) }
    var premium by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(id) {
        if (id != 0L) {
            vm.getPolicy(id)?.let {
                customerId = it.customerId
                vehicle = it.vehicleNumber
                insurer = it.insurer
                type = it.policyType
                start = it.startDate
                expiry = it.expiryDate
                premium = if (it.premium % 1.0 == 0.0) it.premium.toLong().toString() else it.premium.toString()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (id == 0L) "Add policy" else "Edit policy") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                }
            )
        }
    ) { pad ->
        if (customers.isEmpty()) {
            EmptyState("Add a customer first, then you can assign a policy to them.", Modifier.padding(pad))
        } else {
            Column(
                Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DropdownField(
                    label = "Customer *",
                    selectedText = customers.firstOrNull { it.id == customerId }?.name ?: "",
                    options = customers.map { it.name },
                    onSelect = { customerId = customers[it].id }
                )
                DropdownField(
                    label = "Policy type",
                    selectedText = type,
                    options = PolicyTypes,
                    onSelect = { type = PolicyTypes[it] }
                )
                OutlinedTextField(
                    value = vehicle, onValueChange = { vehicle = it.uppercase() },
                    label = { Text(if (type.startsWith("Motor")) "Vehicle number *" else "Vehicle number (optional)") },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = insurer, onValueChange = { insurer = it }, label = { Text("Insurer name *") },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                DateField("Start date", start, { start = it })
                DateField("Expiry date", expiry, { expiry = it })
                OutlinedTextField(
                    value = premium, onValueChange = { premium = it }, label = { Text("Premium (₹) *") },
                    singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Button(
                    onClick = {
                        val amount = premium.toDoubleOrNull()
                        error = when {
                            customerId == 0L -> "Choose a customer."
                            type.startsWith("Motor") && vehicle.isBlank() -> "Vehicle number is required for motor policies."
                            insurer.isBlank() -> "Insurer name is required."
                            expiry < start -> "Expiry date must be after the start date."
                            amount == null || amount <= 0.0 -> "Enter a valid premium amount."
                            else -> null
                        }
                        if (error == null && amount != null) {
                            vm.savePolicy(
                                Policy(id, customerId, vehicle.trim(), insurer.trim(), type, start, expiry, amount),
                                onBack
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Save") }
            }
        }
    }
}
