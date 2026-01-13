package com.finnomena.datatagging.model

/**
 * Configuration for the DataTagging module
 */
data class DataTaggingConfig(
    val baseUrl: String,
    val trackingId: String = "UA-FINNO",
    val sessionTimeoutMinutes: Int = 30
) {
    companion object {
        // Pre-configured environments
        fun development() = DataTaggingConfig(
            baseUrl = "https://gtm-int.finnomena.com/mpua"
        )

        fun uat() = DataTaggingConfig(
            baseUrl = "https://gtm-uat.finnomena.com/mpua"
        )

        fun production() = DataTaggingConfig(
            baseUrl = "https://gtm.finnomena.com/mpua"
        )
    }
}
