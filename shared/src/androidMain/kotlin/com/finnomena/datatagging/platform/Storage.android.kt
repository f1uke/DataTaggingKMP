package com.finnomena.datatagging.platform

import android.content.Context
import android.content.SharedPreferences

/**
 * Android implementation of DataTaggingStorage using SharedPreferences
 *
 * Note: For production use with sensitive data, consider using EncryptedSharedPreferences
 *
 * To use custom storage from Android:
 * ```kotlin
 * class AndroidDataTaggingStorage(context: Context) : DataTaggingStorage {
 *     private val prefs = context.getSharedPreferences("data_tagging", Context.MODE_PRIVATE)
 *     // ... implement methods
 * }
 * ```
 */
class DefaultAndroidDataTaggingStorage(context: Context) : DataTaggingStorage {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "com.finnomena.datatagging"
        private const val KEY_USER_ID = "userId"
        private const val KEY_SESSION_UUID = "sessionUUID"
        private const val KEY_BRAZE_ID = "brazeId"
        private const val KEY_CLIENT_ID = "clientId"
    }

    override fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }

    override fun getSessionUUID(): String? {
        return prefs.getString(KEY_SESSION_UUID, null)
    }

    override fun setSessionUUID(uuid: String) {
        prefs.edit().putString(KEY_SESSION_UUID, uuid).apply()
    }

    override fun getBrazeId(): String? {
        return prefs.getString(KEY_BRAZE_ID, null)
    }

    override fun getClientId(): String? {
        return prefs.getString(KEY_CLIENT_ID, null)
    }

    override fun setClientId(clientId: String) {
        prefs.edit().putString(KEY_CLIENT_ID, clientId).apply()
    }
}

/**
 * Factory function to create a default Android storage
 */
fun createDefaultAndroidStorage(context: Context): DataTaggingStorage =
    DefaultAndroidDataTaggingStorage(context)
