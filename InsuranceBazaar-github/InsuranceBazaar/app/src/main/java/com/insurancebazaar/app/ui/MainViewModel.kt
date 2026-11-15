package com.insurancebazaar.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.insurancebazaar.app.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.get(app)
    private val customerDao = db.customerDao()
    private val policyDao = db.policyDao()
    private val session = SessionManager(app)

    // ---------- Auth / profile ----------
    private val _userName = MutableStateFlow(session.name())
    val userName = _userName.asStateFlow()
    val username: String get() = session.username()
    val isLoggedIn: Boolean get() = session.isLoggedIn()

    fun hasAccount() = session.hasAccount()

    fun login(username: String, password: String): Boolean {
        val ok = session.login(username, password)
        if (ok) _userName.value = session.name()
        return ok
    }

    fun register(name: String, username: String, password: String) {
        session.register(name, username, password)
        _userName.value = session.name()
    }

    fun logout() = session.logout()

    fun updateName(name: String) {
        session.updateName(name)
        _userName.value = session.name()
    }

    // ---------- Customers ----------
    val customerQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val customers: StateFlow<List<Customer>> = customerQuery
        .flatMapLatest { customerDao.search("%${it.trim()}%") }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCustomers: StateFlow<List<Customer>> = customerDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun getCustomer(id: Long) = customerDao.getById(id)

    fun saveCustomer(customer: Customer, onDone: () -> Unit) {
        viewModelScope.launch {
            customerDao.upsert(customer)
            onDone()
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch { customerDao.delete(customer) }
    }

    // ---------- Policies ----------
    val policyQuery = MutableStateFlow("")

    /** 0 = all, 1 = active, 2 = expired */
    val policyFilter = MutableStateFlow(0)

    val policies: StateFlow<List<PolicyWithCustomer>> =
        combine(policyDao.getAll(), policyQuery, policyFilter) { list, query, filter ->
            val q = query.trim()
            val now = startOfToday()
            list.filter { item ->
                val matchesText = q.isEmpty() ||
                    item.policy.vehicleNumber.contains(q, ignoreCase = true) ||
                    item.policy.insurer.contains(q, ignoreCase = true) ||
                    item.customerName.contains(q, ignoreCase = true)
                val expired = item.policy.expiryDate < now
                val matchesFilter = when (filter) {
                    1 -> !expired
                    2 -> expired
                    else -> true
                }
                matchesText && matchesFilter
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun getPolicy(id: Long) = policyDao.getById(id)

    fun savePolicy(policy: Policy, onDone: () -> Unit) {
        viewModelScope.launch {
            policyDao.upsert(policy)
            onDone()
        }
    }

    fun deletePolicy(policy: Policy) {
        viewModelScope.launch { policyDao.delete(policy) }
    }

    // ---------- Database setup helpers ----------
    /** Fills an empty database with demo customers and policies (dates are relative to today). */
    fun loadSampleData() {
        viewModelScope.launch {
            val today = startOfToday()
            val rahul = customerDao.upsert(Customer(name = "Rahul Sharma", phone = "9876543210", email = "rahul.sharma@example.com", address = "Pune, Maharashtra"))
            val priya = customerDao.upsert(Customer(name = "Priya Verma", phone = "9811122233", email = "priya.verma@example.com", address = "Delhi"))
            val amit = customerDao.upsert(Customer(name = "Amit Patel", phone = "9922334455", address = "Ahmedabad, Gujarat"))
            val sneha = customerDao.upsert(Customer(name = "Sneha Iyer", phone = "9845012345", email = "sneha.iyer@example.com", address = "Bengaluru, Karnataka"))

            policyDao.upsert(Policy(customerId = rahul, vehicleNumber = "MH12AB1234", insurer = "HDFC Ergo", policyType = "Motor - Comprehensive", startDate = today - 165 * DAY_MS, expiryDate = today + 200 * DAY_MS, premium = 12450.0))
            policyDao.upsert(Policy(customerId = rahul, vehicleNumber = "", insurer = "Star Health", policyType = "Health", startDate = today - 90 * DAY_MS, expiryDate = today + 275 * DAY_MS, premium = 18200.0))
            policyDao.upsert(Policy(customerId = priya, vehicleNumber = "DL8CAF5031", insurer = "ICICI Lombard", policyType = "Motor - Comprehensive", startDate = today - 353 * DAY_MS, expiryDate = today + 12 * DAY_MS, premium = 9800.0))
            policyDao.upsert(Policy(customerId = amit, vehicleNumber = "GJ01KP7788", insurer = "Bajaj Allianz", policyType = "Motor - Third Party", startDate = today - 385 * DAY_MS, expiryDate = today - 20 * DAY_MS, premium = 2900.0))
            policyDao.upsert(Policy(customerId = amit, vehicleNumber = "", insurer = "LIC", policyType = "Life", startDate = today - 400 * DAY_MS, expiryDate = today + 330 * DAY_MS, premium = 25000.0))
            policyDao.upsert(Policy(customerId = sneha, vehicleNumber = "KA05MN2201", insurer = "New India Assurance", policyType = "Motor - Comprehensive", startDate = today, expiryDate = today + 365 * DAY_MS, premium = 11300.0))
        }
    }

    /** Removes all customers and policies. The login account is kept. */
    fun clearAllData() {
        viewModelScope.launch { customerDao.deleteAll() }
    }

    // ---------- Dashboard ----------
    val stats: StateFlow<DashboardStats> = run {
        val now = startOfToday()
        val (monthStart, monthEnd) = currentMonthRange()
        policyDao.stats(now, now + 30 * DAY_MS, monthStart, monthEnd)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats(0, 0, 0, 0, 0, 0.0, 0.0))

    val monthlyPremiums: StateFlow<List<MonthlyPremium>> = policyDao.monthlyPremiums()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
