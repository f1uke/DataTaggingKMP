package com.finnomena.datatagging.model

import kotlinx.serialization.Serializable

/**
 * Represents an analytics event to be tracked
 */
@Serializable
data class AnalyticsEvent(
    val name: String,
    val location: String,
    val type: String = "click",
    val path: String,
    val params: Map<String, String>? = null
)
