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
        // Pre-configured environment base URLs
        const val DEV_BASE_URL = "https://gtm-int.finnomena.com/mpua"
        const val UAT_BASE_URL = "https://gtm-uat.finnomena.com/mpua"
        const val PROD_BASE_URL = "https://gtm.finnomena.com/mpua"
    }
}
