package com.insurancebazaar.app.data

import android.content.Context
import java.security.MessageDigest
import java.security.SecureRandom

/** Local-only account. Stored on the device; there is no server. */
class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("insurance_bazaar_session", Context.MODE_PRIVATE)

    fun hasAccount(): Boolean = prefs.contains(KEY_USERNAME)
    fun isLoggedIn(): Boolean = hasAccount() && prefs.getBoolean(KEY_LOGGED_IN, false)
    fun name(): String = prefs.getString(KEY_NAME, "") ?: ""
    fun username(): String = prefs.getString(KEY_USERNAME, "") ?: ""

    fun register(name: String, username: String, password: String) {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }.toHex()
        prefs.edit()
            .putString(KEY_NAME, name.trim())
            .putString(KEY_USERNAME, username.trim())
            .putString(KEY_SALT, salt)
            .putString(KEY_HASH, hash(salt, password))
            .putBoolean(KEY_LOGGED_IN, true)
            .apply()
    }

    fun login(username: String, password: String): Boolean {
        val salt = prefs.getString(KEY_SALT, null) ?: return false
        val ok = username.trim() == username() && hash(salt, password) == prefs.getString(KEY_HASH, null)
        if (ok) prefs.edit().putBoolean(KEY_LOGGED_IN, true).apply()
        return ok
    }

    fun logout() = prefs.edit().putBoolean(KEY_LOGGED_IN, false).apply()

    fun updateName(name: String) = prefs.edit().putString(KEY_NAME, name.trim()).apply()

    private fun hash(salt: String, password: String): String =
        MessageDigest.getInstance("SHA-256").digest((salt + password).toByteArray()).toHex()

    private fun ByteArray.toHex() = joinToString("") { "%02x".format(it) }

    private companion object {
        const val KEY_NAME = "name"
        const val KEY_USERNAME = "username"
        const val KEY_SALT = "salt"
        const val KEY_HASH = "hash"
        const val KEY_LOGGED_IN = "logged_in"
    }
}
