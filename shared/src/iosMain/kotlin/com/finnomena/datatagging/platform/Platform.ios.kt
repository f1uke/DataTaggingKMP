package com.finnomena.datatagging.platform

import platform.Foundation.NSBundle
import platform.Foundation.NSDate
import platform.Foundation.NSProcessInfo
import platform.Foundation.NSUUID
import platform.Foundation.timeIntervalSince1970

actual fun getPlatformName(): String = "ios"

actual fun generateTimeBasedUUID(): String {
    // Generate a pseudo time-based UUID by encoding timestamp into UUID
    // Format: timestamp_hex (12 chars) + random (rest of UUID)
    val timestamp = currentTimeMillis()
    val uuid = NSUUID().UUIDString

    // Encode timestamp into the UUID string for later extraction
    val timestampHex = timestamp.toString(16).padStart(12, '0')

    // Create a custom format: first 8 chars are timestamp hex, rest is from random UUID
    // Format: XXXXXXXX-XXXX-1XXX-XXXX-XXXXXXXXXXXX (version 1 marker)
    return "${timestampHex.substring(0, 8)}-${timestampHex.substring(8, 12)}-1${uuid.substring(15)}"
}

actual fun getUUIDTimestamp(uuid: String): Long? {
    return try {
        val parts = uuid.split("-")
        if (parts.size != 5) return null

        val timeLow = parts[0]
        val timeMid = parts[1]
        val timeHiAndVersion = parts[2]

        // Check if it's our pseudo time-based UUID (version nibble is '1')
        if (timeHiAndVersion.startsWith("1")) {
            // Extract embedded timestamp
            val timestampHex = timeLow + timeMid
            return timestampHex.toLongOrNull(16)
        }

        // Try to parse as real UUID v1
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

actual fun currentTimeMillis(): Long {
    return (NSDate().timeIntervalSince1970 * 1000).toLong()
}
