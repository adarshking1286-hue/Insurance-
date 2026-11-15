package com.insurancebazaar.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.insurancebazaar.app.data.Fmt

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(vm: MainViewModel) {
    val stats by vm.stats.collectAsState()
    val months by vm.monthlyPremiums.collectAsState()
    val name by vm.userName.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Insurance Bazaar") }) }) { pad ->
        Column(
            Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Hello, $name", style = MaterialTheme.typography.titleLarge)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Customers", stats.totalCustomers.toString(), Modifier.weight(1f))
                StatCard("Policies", stats.totalPolicies.toString(), Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Active policies", stats.activePolicies.toString(), Modifier.weight(1f))
                StatCard("Expired policies", stats.expiredPolicies.toString(), Modifier.weight(1f))
            }
            StatCard("Expiring in the next 30 days", stats.expiringSoon.toString(), Modifier.fillMaxWidth())

            Text("Premium summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("This month", Fmt.money(stats.monthPremium), Modifier.weight(1f))
                StatCard("Active policies total", Fmt.money(stats.activePremium), Modifier.weight(1f))
            }

            if (months.isNotEmpty()) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("By month (policy start date)", style = MaterialTheme.typography.titleSmall)
                        months.forEach { m ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(Fmt.month(m.month))
                                Text(Fmt.money(m.total), fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }
}
