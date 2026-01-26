package com.finnomena.datatagging.platform

/**
 * Platform-specific information provider
 */
expect fun getPlatformName(): String

/**
 * Generate a time-based UUID (similar to UUID v1)
 * Returns a UUID string that contains timestamp information
 */
expect fun generateTimeBasedUUID(): String

/**
 * Extract timestamp from a time-based UUID
 * Returns timestamp in milliseconds since epoch, or null if invalid
 */
expect fun getUUIDTimestamp(uuid: String): Long?

/**
 * Get current time in milliseconds since epoch
 */
expect fun currentTimeMillis(): Long
