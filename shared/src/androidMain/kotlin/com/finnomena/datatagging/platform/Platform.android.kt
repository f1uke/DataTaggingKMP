package com.finnomena.datatagging.platform

import android.os.Build
import java.util.UUID

actual fun getPlatformName(): String = "android"

actual fun generateTimeBasedUUID(): String {
    // Generate a pseudo time-based UUID
    // For proper UUID v1 on Android, consider using a dedicated library
    val timestamp = System.currentTimeMillis()
    val uuid = UUID.randomUUID()

    // Encode timestamp into the UUID string for later extraction
    // Format: first 8 chars are timestamp hex, rest is random
    val timestampHex = timestamp.toString(16).padStart(12, '0')
    val uuidString = uuid.toString()

    // Create a custom format that embeds timestamp
    return "${timestampHex.substring(0, 8)}-${timestampHex.substring(8, 12)}-1${uuidString.substring(15)}"
}

actual fun getUUIDTimestamp(uuid: String): Long? {
    return try {
        val parts = uuid.split("-")
        if (parts.size != 5) return null

        val timeLow = parts[0]
        val timeMid = parts[1]
        val timeHiAndVersion = parts[2]

        // Check if it's a v1-like UUID (version nibble is '1')
        if (!timeHiAndVersion.startsWith("1")) {
            // Try to extract embedded timestamp
            val timestampHex = timeLow + timeMid
            return timestampHex.toLongOrNull(16)
        }

        // Parse as UUID v1
        val timeHi = timeHiAndVersion.drop(1)
        val hex = timeHi + timeMid + timeLow
        val interval = hex.toLongOrNull(16) ?: return null

        val seconds = interval / 10_000_000
        val epochOffset = 12219292800L
        val unixSeconds = seconds - epochOffset

        unixSeconds * 1000
    } catch (e: Exception) {
        null
    }
}

actual fun currentTimeMillis(): Long = System.currentTimeMillis()
