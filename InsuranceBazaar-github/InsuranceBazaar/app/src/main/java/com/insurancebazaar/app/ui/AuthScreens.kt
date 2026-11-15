package com.insurancebazaar.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.insurancebazaar.app.R

@Composable
fun WelcomeScreen(onContinue: () -> Unit) {
    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = "Insurance Bazaar logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth().heightIn(max = 260.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Manage customers, policies and renewals in one place.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))
            Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) { Text("Get started") }
        }
    }
}

@Composable
fun AuthScreen(vm: MainViewModel, onSuccess: () -> Unit) {
    val hasAccount = remember { vm.hasAccount() }
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = "Insurance Bazaar logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth().heightIn(max = 140.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                if (hasAccount) "Log in" else "Create your account",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            if (!hasAccount) {
                Text(
                    "The account is stored only on this phone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(24.dp))

            if (!hasAccount) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it }, label = { Text("Your name") },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
            }
            OutlinedTextField(
                value = username, onValueChange = { username = it }, label = { Text("Username") },
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password, onValueChange = { password = it }, label = { Text("Password") },
                singleLine = true, visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            if (!hasAccount) {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = confirm, onValueChange = { confirm = it }, label = { Text("Confirm password") },
                    singleLine = true, visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    if (hasAccount) {
                        if (vm.login(username, password)) onSuccess()
                        else error = "Wrong username or password."
                    } else {
                        error = when {
                            name.isBlank() -> "Please enter your name."
                            username.isBlank() -> "Please choose a username."
                            password.length < 4 -> "Password must be at least 4 characters."
                            password != confirm -> "Passwords do not match."
                            else -> null
                        }
                        if (error == null) {
                            vm.register(name, username, password)
                            onSuccess()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (hasAccount) "Log in" else "Create account") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(vm: MainViewModel, onLogout: () -> Unit) {
    val savedName by vm.userName.collectAsState()
    var name by remember(savedName) { mutableStateOf(savedName) }
    val stats by vm.stats.collectAsState()
    var confirmClear by remember { mutableStateOf(false) }

    Scaffold(topBar = { TopAppBar(title = { Text("Profile") }) }) { pad ->
        Column(
            Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(savedName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("@${vm.username}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = name, onValueChange = { name = it }, label = { Text("Display name") },
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { vm.updateName(name) },
                enabled = name.isNotBlank() && name.trim() != savedName
            ) { Text("Save name") }
            HorizontalDivider()
            Text("Data", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Button(
                onClick = { vm.loadSampleData() },
                enabled = stats.totalCustomers == 0,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Load sample data") }
            OutlinedButton(
                onClick = { confirmClear = true },
                enabled = stats.totalCustomers > 0,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Delete all customers and policies") }
            HorizontalDivider()
            Text("Insurance Bazaar v1.0", style = MaterialTheme.typography.bodySmall)
            Text(
                "All data is stored locally on this device.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) { Text("Log out") }
        }
    }

    if (confirmClear) {
        ConfirmDialog(
            title = "Delete all data?",
            text = "Every customer and policy will be permanently deleted. Your login is kept.",
            confirmLabel = "Delete all",
            onConfirm = {
                vm.clearAllData()
                confirmClear = false
            },
            onDismiss = { confirmClear = false }
        )
    }
}
