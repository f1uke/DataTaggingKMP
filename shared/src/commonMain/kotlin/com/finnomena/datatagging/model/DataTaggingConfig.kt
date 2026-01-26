package com.finnomena.datatagging.model

/**
 * Configuration for the DataTagging module
 */
data class DataTaggingConfig(
    val baseUrl: String,
    val userAgent: String,
    val trackingId: String = "UA-FINNO",
    val sessionTimeoutMinutes: Int = 30
) {
    companion object {
        // Pre-configured environments
        fun development(userAgent: String) = DataTaggingConfig(
            baseUrl = "https://gtm-int.finnomena.com/mpua",
            userAgent = userAgent
        )

        fun uat(userAgent: String) = DataTaggingConfig(
            baseUrl = "https://gtm-uat.finnomena.com/mpua",
            userAgent = userAgent
        )

        fun production(userAgent: String) = DataTaggingConfig(
            baseUrl = "https://gtm.finnomena.com/mpua",
            userAgent = userAgent
        )
    }
}
