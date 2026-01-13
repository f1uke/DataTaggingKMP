package com.finnomena.datatagging.platform

import io.ktor.client.HttpClient

/**
 * Factory for creating platform-specific HTTP clients
 */
expect fun createHttpClient(): HttpClient
