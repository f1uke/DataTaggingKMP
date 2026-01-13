package com.finnomena.datatagging.platform

/**
 * Storage interface for persisting analytics-related data
 * Each platform should implement this using appropriate secure storage
 */
interface DataTaggingStorage {
    /**
     * Get the user's account ID (if logged in)
     */
    fun getUserId(): String?

    /**
     * Get the current session UUID
     */
    fun getSessionUUID(): String?

    /**
     * Store a new session UUID
     */
    fun setSessionUUID(uuid: String)

    /**
     * Get the Braze marketing platform ID
     */
    fun getBrazeId(): String?

    /**
     * Get the client ID (finnakie cookie equivalent)
     * This is a persistent identifier for the device/installation
     */
    fun getClientId(): String?

    /**
     * Store the client ID
     */
    fun setClientId(clientId: String)
}
