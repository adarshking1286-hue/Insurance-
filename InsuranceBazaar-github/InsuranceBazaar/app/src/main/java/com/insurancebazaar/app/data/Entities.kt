package com.insurancebazaar.app.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val email: String = "",
    val address: String = ""
)

@Entity(
    tableName = "policies",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("customerId")]
)
data class Policy(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val vehicleNumber: String,
    val insurer: String,
    val policyType: String,
    val startDate: Long,   // UTC midnight, millis
    val expiryDate: Long,  // UTC midnight, millis
    val premium: Double
)

data class PolicyWithCustomer(
    @Embedded val policy: Policy,
    val customerName: String
)

data class DashboardStats(
    val totalCustomers: Int,
    val totalPolicies: Int,
    val activePolicies: Int,
    val expiredPolicies: Int,
    val expiringSoon: Int,
    val activePremium: Double,
    val monthPremium: Double
)

data class MonthlyPremium(
    val month: String, // yyyy-MM
    val total: Double
)
