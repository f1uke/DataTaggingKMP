package com.finnomena.datatagging

import com.finnomena.datatagging.model.AnalyticsEvent
import com.finnomena.datatagging.model.DataTaggingConfig
import com.finnomena.datatagging.platform.DataTaggingStorage
import com.finnomena.datatagging.platform.currentTimeMillis
import com.finnomena.datatagging.platform.generateTimeBasedUUID
import com.finnomena.datatagging.platform.getPlatformName
import com.finnomena.datatagging.platform.getUUIDTimestamp
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Main manager for sending analytics events to GTM
 * This class handles event tracking, session management, and parameter construction
 */
class DataTaggingManager(
    private val config: DataTaggingConfig,
    private val storage: DataTaggingStorage,
    private val httpClient: HttpClient
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()
    private var exId: String? = null

    private val json = Json { encodeDefaults = true }

    /**
     * Log an analytics event
     */
    fun logEvent(event: AnalyticsEvent) {
        scope.launch {
            sendEvent(
                eventAction = event.name,
                eventLocation = event.location,
                eventType = event.type,
                path = event.path,
                params = event.params
            )
        }
    }

    /**
     * Log a screen view event
     */
    fun logScreenView(path: String, params: Map<String, String>? = null) {
        scope.launch {
            sendEvent(
                eventAction = "page_view",
                eventLocation = "",
                eventType = "page",
                path = path,
                params = params
            )
        }
    }

    /**
     * Set the experiment ID for A/B testing
     */
    fun setExId(exId: String) {
        scope.launch {
            mutex.withLock {
                this@DataTaggingManager.exId = exId
                refreshSession()
            }
        }
    }

    /**
     * Get the current experiment ID
     */
    fun getExId(): String? = exId

    private suspend fun sendEvent(
        eventAction: String,
        eventLocation: String,
        eventType: String,
        path: String,
        params: Map<String, String>?
    ) {
        mutex.withLock {
            val sessionUUID = refreshSessionIfNeeded()
            val clientId = getOrCreateClientId()

            val userId = storage.getUserId() ?: ""
            val brazeId = storage.getBrazeId() ?: ""

            // Build additional params with user agent and exId
            val actualParams = params?.toMutableMap() ?: mutableMapOf()
            actualParams["user_agent"] = config.userAgent
            exId?.let { actualParams["ex_id"] = it }

            val paramsJson = json.encodeToString(actualParams)

            // Build query parameters
            val queryParams = mapOf(
                "v" to "1",
                "t" to "event",
                "tid" to config.trackingId,
                "cid" to clientId,
                "ea" to eventAction,
                "l" to eventLocation,
                "u.fss" to sessionUUID,
                "u.f" to clientId,
                "u.e" to brazeId,
                "u.i" to userId,
                "p" to paramsJson,
                "ph" to path,
                "et" to eventType,
                "d" to getPlatformName()
            )

            try {
                httpClient.get(config.baseUrl) {
                    queryParams.forEach { (key, value) ->
                        parameter(key, value)
                    }
                }
            } catch (e: Exception) {
                // Fire and forget - ignore errors
            }
        }
    }

    private fun refreshSessionIfNeeded(): String {
        val currentUUID = storage.getSessionUUID()

        if (currentUUID == null) {
            return refreshSession()
        }

        val timestamp = getUUIDTimestamp(currentUUID)
        if (timestamp == null) {
            return refreshSession()
        }

        val currentTime = currentTimeMillis()
        val minutesElapsed = (currentTime - timestamp) / (60 * 1000)

        if (minutesElapsed > config.sessionTimeoutMinutes) {
            exId = null
            return refreshSession()
        }

        return currentUUID
    }

    private fun refreshSession(): String {
        val newUUID = generateTimeBasedUUID()
        storage.setSessionUUID(newUUID)
        return newUUID
    }

    private fun getOrCreateClientId(): String {
        val existingId = storage.getClientId()
        if (existingId != null && existingId.length == 15) {
            return existingId
        }

        // Generate new client ID: 7 random chars + 10-char timestamp
        val randomPart = generateRandomString(7)
        val timestampPart = formatTimestamp(currentTimeMillis())
        val clientId = randomPart + timestampPart

        storage.setClientId(clientId)
        return clientId
    }

    private fun generateRandomString(length: Int): String {
        val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }

    private fun formatTimestamp(millis: Long): String {
        // Format: yyMMddHHmm (10 characters)
        // This is a simplified version - platform implementations may provide better formatting
        val seconds = millis / 1000
        val minutes = (seconds / 60) % 60
        val hours = (seconds / 3600) % 24
        val days = ((seconds / 86400) % 31) + 1
        val months = ((seconds / 2629746) % 12) + 1
        val years = (seconds / 31556952) + 1970

        return buildString {
            append((years % 100).toString().padStart(2, '0'))
            append(months.toString().padStart(2, '0'))
            append(days.toString().padStart(2, '0'))
            append(hours.toString().padStart(2, '0'))
            append(minutes.toString().padStart(2, '0'))
        }
    }
}
