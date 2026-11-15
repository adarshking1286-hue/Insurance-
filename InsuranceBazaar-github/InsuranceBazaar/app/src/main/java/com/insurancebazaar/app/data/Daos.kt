package com.insurancebazaar.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers WHERE name LIKE :q OR phone LIKE :q ORDER BY name COLLATE NOCASE")
    fun search(q: String): Flow<List<Customer>>

    @Query("SELECT * FROM customers ORDER BY name COLLATE NOCASE")
    fun getAll(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getById(id: Long): Customer?

    @Upsert
    suspend fun upsert(customer: Customer): Long

    @Delete
    suspend fun delete(customer: Customer)

    /** Deletes every customer; their policies go too (foreign key cascade). */
    @Query("DELETE FROM customers")
    suspend fun deleteAll()
}

@Dao
interface PolicyDao {
    @Query(
        """
        SELECT p.*, c.name AS customerName
        FROM policies p
        INNER JOIN customers c ON c.id = p.customerId
        ORDER BY p.expiryDate ASC
        """
    )
    fun getAll(): Flow<List<PolicyWithCustomer>>

    @Query("SELECT * FROM policies WHERE id = :id")
    suspend fun getById(id: Long): Policy?

    @Upsert
    suspend fun upsert(policy: Policy): Long

    @Delete
    suspend fun delete(policy: Policy)

    @Query(
        """
        SELECT
            (SELECT COUNT(*) FROM customers) AS totalCustomers,
            COUNT(*) AS totalPolicies,
            COALESCE(SUM(CASE WHEN expiryDate >= :now THEN 1 ELSE 0 END), 0) AS activePolicies,
            COALESCE(SUM(CASE WHEN expiryDate < :now THEN 1 ELSE 0 END), 0) AS expiredPolicies,
            COALESCE(SUM(CASE WHEN expiryDate >= :now AND expiryDate < :soon THEN 1 ELSE 0 END), 0) AS expiringSoon,
            COALESCE(SUM(CASE WHEN expiryDate >= :now THEN premium ELSE 0 END), 0) AS activePremium,
            COALESCE(SUM(CASE WHEN startDate >= :monthStart AND startDate < :monthEnd THEN premium ELSE 0 END), 0) AS monthPremium
        FROM policies
        """
    )
    fun stats(now: Long, soon: Long, monthStart: Long, monthEnd: Long): Flow<DashboardStats>

    @Query(
        """
        SELECT strftime('%Y-%m', startDate / 1000, 'unixepoch') AS month,
               SUM(premium) AS total
        FROM policies
        GROUP BY month
        ORDER BY month DESC
        LIMIT 6
        """
    )
    fun monthlyPremiums(): Flow<List<MonthlyPremium>>
}
