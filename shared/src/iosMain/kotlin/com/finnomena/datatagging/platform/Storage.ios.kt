package com.finnomena.datatagging.platform

import platform.Foundation.NSUserDefaults

/**
 * iOS implementation of DataTaggingStorage using NSUserDefaults
 *
 * Note: For production use, the iOS app should provide its own implementation
 * that wraps the existing UserManager, UserDefaultManager, and CookieManager.
 * This default implementation uses NSUserDefaults for simplicity.
 *
 * To use custom storage from Swift:
 * ```swift
 * class iOSDataTaggingStorage: DataTaggingStorage {
 *     func getUserId() -> String? {
 *         return UserManager.getUserID()
 *     }
 *     // ... implement other methods
 * }
 * ```
 */
class DefaultIOSDataTaggingStorage : DataTaggingStorage {
    private val defaults = NSUserDefaults.standardUserDefaults

    companion object {
        private const val KEY_USER_ID = "com.finnomena.datatagging.userId"
        private const val KEY_SESSION_UUID = "com.finnomena.datatagging.sessionUUID"
        private const val KEY_BRAZE_ID = "com.finnomena.datatagging.brazeId"
        private const val KEY_CLIENT_ID = "com.finnomena.datatagging.clientId"
    }

    override fun getUserId(): String? {
        return defaults.stringForKey(KEY_USER_ID)
    }

    override fun getSessionUUID(): String? {
        return defaults.stringForKey(KEY_SESSION_UUID)
    }

    override fun setSessionUUID(uuid: String) {
        defaults.setObject(uuid, KEY_SESSION_UUID)
    }

    override fun getBrazeId(): String? {
        return defaults.stringForKey(KEY_BRAZE_ID)
    }

    override fun getClientId(): String? {
        return defaults.stringForKey(KEY_CLIENT_ID)
    }

    override fun setClientId(clientId: String) {
        defaults.setObject(clientId, KEY_CLIENT_ID)
    }
}

/**
 * Factory function to create a default iOS storage
 */
fun createDefaultIOSStorage(): DataTaggingStorage = DefaultIOSDataTaggingStorage()
