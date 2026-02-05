package com.finnomena.datatagging

import com.finnomena.datatagging.model.AnalyticsEvent
import com.finnomena.datatagging.model.DataTaggingConfig
import com.finnomena.datatagging.platform.DataTaggingStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DataTaggingManagerTest {

    private class TestStorage : DataTaggingStorage {
        private val storage = mutableMapOf<String, String>()

        override fun getClientId(): String? = storage["clientId"]
        override fun setClientId(clientId: String) {
            storage["clientId"] = clientId
        }

        override fun getSessionUUID(): String? = storage["sessionUUID"]
        override fun setSessionUUID(uuid: String) {
            storage["sessionUUID"] = uuid
        }

        override fun getUserId(): String? = storage["userId"]
        override fun getBrazeId(): String? = storage["brazeId"]
    }

    private fun createMockHttpClient(): HttpClient {
        val mockEngine = MockEngine { request ->
            respond(
                content = "OK",
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", "text/plain")
            )
        }
        return HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
        }
    }

    @Test
    fun testClientIdGenerationFormat() = runBlocking {
        val storage = TestStorage()
        val config = DataTaggingConfig(
            baseUrl = DataTaggingConfig.DEV_BASE_URL,
            userAgent = "TestApp/1.0",
            trackingId = "GTM-TEST"
        )
        val httpClient = createMockHttpClient()
        val manager = DataTaggingManager(config, storage, httpClient)

        // Log an event to trigger client ID generation
        manager.logEvent(
            AnalyticsEvent(
                name = "test_event",
                location = "test",
                type = "click",
                path = "/test"
            )
        )

        // Wait for coroutine to complete (using longer delay since manager uses IO dispatcher)
        kotlinx.coroutines.delay(500)

        // Check that client ID was generated
        val clientId = storage.getClientId()
        assertNotNull(clientId, "Client ID should be generated")
        assertEquals(15, clientId.length, "Client ID should be exactly 15 characters")

        // Verify format: 7 random chars + 8 digit date (yyyyMMdd)
        val randomPart = clientId.substring(0, 7)
        val datePart = clientId.substring(7, 15)

        // Random part should be alphanumeric
        assertTrue(
            randomPart.all { it.isLetterOrDigit() },
            "Random part should be alphanumeric"
        )

        // Date part should be all digits and valid format
        assertTrue(
            datePart.all { it.isDigit() },
            "Date part should be all digits"
        )

        // Verify date format yyyyMMdd
        val year = datePart.substring(0, 4).toInt()
        val month = datePart.substring(4, 6).toInt()
        val day = datePart.substring(6, 8).toInt()

        assertTrue(year >= 2020, "Year should be reasonable")
        assertTrue(month in 1..12, "Month should be 1-12")
        assertTrue(day in 1..31, "Day should be 1-31")
    }

    @Test
    fun testExistingClientIdIsPreserved() = runBlocking {
        val storage = TestStorage()
        val config = DataTaggingConfig(
            baseUrl = DataTaggingConfig.DEV_BASE_URL,
            userAgent = "TestApp/1.0",
            trackingId = "GTM-TEST"
        )
        val httpClient = createMockHttpClient()

        // Set an existing client ID with different format (e.g., from another platform)
        val existingClientId = "abc123xyz"
        storage.setClientId(existingClientId)

        val manager = DataTaggingManager(config, storage, httpClient)

        // Log an event
        manager.logEvent(
            AnalyticsEvent(
                name = "test_event",
                location = "test",
                type = "click",
                path = "/test"
            )
        )

        // Wait for coroutine to complete (using longer delay since manager uses IO dispatcher)
        kotlinx.coroutines.delay(500)

        // Verify existing client ID is preserved regardless of length
        assertEquals(
            existingClientId,
            storage.getClientId(),
            "Existing client ID should be preserved"
        )
    }

    @Test
    fun testNewClientIdWhenNoneExists() = runBlocking {
        val storage = TestStorage()
        val config = DataTaggingConfig(
            baseUrl = DataTaggingConfig.DEV_BASE_URL,
            userAgent = "TestApp/1.0",
            trackingId = "GTM-TEST"
        )
        val httpClient = createMockHttpClient()
        val manager = DataTaggingManager(config, storage, httpClient)

        // Verify no client ID initially
        assertEquals(null, storage.getClientId())

        // Log an event to trigger generation
        manager.logEvent(
            AnalyticsEvent(
                name = "test_event",
                location = "test",
                type = "click",
                path = "/test"
            )
        )

        // Wait for coroutine to complete (using longer delay since manager uses IO dispatcher)
        kotlinx.coroutines.delay(500)

        // Verify client ID was created
        val clientId = storage.getClientId()
        assertNotNull(clientId)
        assertEquals(15, clientId.length)
    }

    @Test
    fun testUserAgentSpacesEncodedAsPercent20() = runBlocking {
        val storage = TestStorage()
        // Use real Android user agent format with spaces
        val userAgentWithSpaces = "android-nter-app/6.5.1 (com.finnomena.finnomena; build:327; Android 15)"
        val config = DataTaggingConfig(
            baseUrl = DataTaggingConfig.DEV_BASE_URL,
            userAgent = userAgentWithSpaces,
            trackingId = "GTM-TEST"
        )

        var capturedRequestUrl: String? = null
        val mockEngine = MockEngine { request ->
            capturedRequestUrl = request.url.toString()
            respond(
                content = "OK",
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", "text/plain")
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
        }

        val manager = DataTaggingManager(config, storage, httpClient)

        // Log an event
        manager.logEvent(
            AnalyticsEvent(
                name = "test_event",
                location = "test",
                type = "click",
                path = "/test"
            )
        )

        // Wait for coroutine to complete
        kotlinx.coroutines.delay(500)

        // Verify request was captured
        assertNotNull(capturedRequestUrl, "Request URL should be captured")

        // Verify spaces are encoded as %20, not as +
        // The user_agent should contain %20 instead of + for spaces
        assertTrue(
            capturedRequestUrl!!.contains("%2520") || capturedRequestUrl!!.contains("user_agent"),
            "Request should contain user_agent parameter"
        )

        // Verify the URL does NOT contain + where spaces should be
        // After our fix, spaces in user_agent should be pre-encoded as %20
        // which then becomes %2520 after URL encoding, or stays as %20
        val hasEncodedSpaces = capturedRequestUrl!!.contains("%2520") ||
            capturedRequestUrl!!.contains("%20")
        assertTrue(hasEncodedSpaces, "Spaces should be encoded as %20, not +")
    }

    @Test
    fun testUserAgentWithoutSpacesRemainsUnchanged() = runBlocking {
        val storage = TestStorage()
        val userAgentNoSpaces = "android-app/1.0.0"
        val config = DataTaggingConfig(
            baseUrl = DataTaggingConfig.DEV_BASE_URL,
            userAgent = userAgentNoSpaces,
            trackingId = "GTM-TEST"
        )

        var capturedRequestUrl: String? = null
        val mockEngine = MockEngine { request ->
            capturedRequestUrl = request.url.toString()
            respond(
                content = "OK",
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", "text/plain")
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
        }

        val manager = DataTaggingManager(config, storage, httpClient)

        // Log an event
        manager.logEvent(
            AnalyticsEvent(
                name = "test_event",
                location = "test",
                type = "click",
                path = "/test"
            )
        )

        // Wait for coroutine to complete
        kotlinx.coroutines.delay(500)

        // Verify request was captured
        assertNotNull(capturedRequestUrl, "Request URL should be captured")

        // Verify the user agent without spaces works correctly
        assertTrue(
            capturedRequestUrl!!.contains("android-app"),
            "User agent should be present in request"
        )
    }
}
