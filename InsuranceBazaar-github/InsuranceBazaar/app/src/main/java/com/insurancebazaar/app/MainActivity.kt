package com.insurancebazaar.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.insurancebazaar.app.ui.*
import com.insurancebazaar.app.ui.theme.InsuranceBazaarTheme

object Routes {
    const val WELCOME = "welcome"
    const val AUTH = "auth"
    const val DASHBOARD = "dashboard"
    const val CUSTOMERS = "customers"
    const val POLICIES = "policies"
    const val PROFILE = "profile"
    const val CUSTOMER_FORM = "customer_form/{id}"
    const val POLICY_FORM = "policy_form/{id}"
    fun customerForm(id: Long) = "customer_form/$id"
    fun policyForm(id: Long) = "policy_form/$id"
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InsuranceBazaarTheme { AppRoot() }
        }
    }
}

@Composable
fun AppRoot(vm: MainViewModel = viewModel()) {
    val nav = rememberNavController()
    val start = remember { if (vm.isLoggedIn) Routes.DASHBOARD else Routes.WELCOME }
    val route = nav.currentBackStackEntryAsState().value?.destination?.route

    val tabs = listOf(
        Tab(Routes.DASHBOARD, "Dashboard", Icons.Default.Home),
        Tab(Routes.CUSTOMERS, "Customers", Icons.Default.Person),
        Tab(Routes.POLICIES, "Policies", Icons.AutoMirrored.Filled.List),
        Tab(Routes.PROFILE, "Profile", Icons.Default.Person)
    )

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (tabs.any { it.route == route }) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = route == tab.route,
                            onClick = {
                                nav.navigate(tab.route) {
                                    popUpTo(Routes.DASHBOARD) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(nav, startDestination = start, modifier = Modifier.padding(padding)) {
            composable(Routes.WELCOME) {
                WelcomeScreen(onContinue = { nav.navigate(Routes.AUTH) })
            }
            composable(Routes.AUTH) {
                AuthScreen(vm) {
                    nav.navigate(Routes.DASHBOARD) {
                        popUpTo(nav.graph.id) { inclusive = true }
                    }
                }
            }
            composable(Routes.DASHBOARD) { DashboardScreen(vm) }
            composable(Routes.CUSTOMERS) {
                CustomerListScreen(
                    vm,
                    onAdd = { nav.navigate(Routes.customerForm(0)) },
                    onEdit = { nav.navigate(Routes.customerForm(it)) }
                )
            }
            composable(Routes.POLICIES) {
                PolicyListScreen(
                    vm,
                    onAdd = { nav.navigate(Routes.policyForm(0)) },
                    onEdit = { nav.navigate(Routes.policyForm(it)) }
                )
            }
            composable(Routes.PROFILE) {
                ProfileScreen(vm) {
                    vm.logout()
                    nav.navigate(Routes.WELCOME) {
                        popUpTo(nav.graph.id) { inclusive = true }
                    }
                }
            }
            composable(Routes.CUSTOMER_FORM, arguments = listOf(navArgument("id") { type = NavType.LongType })) {
                CustomerFormScreen(it.arguments?.getLong("id") ?: 0L, vm) { nav.popBackStack() }
            }
            composable(Routes.POLICY_FORM, arguments = listOf(navArgument("id") { type = NavType.LongType })) {
                PolicyFormScreen(it.arguments?.getLong("id") ?: 0L, vm) { nav.popBackStack() }
            }
        }
    }
}
