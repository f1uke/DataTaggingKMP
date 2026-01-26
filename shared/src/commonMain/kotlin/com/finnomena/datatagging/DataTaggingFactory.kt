package com.finnomena.datatagging

import com.finnomena.datatagging.model.DataTaggingConfig
import com.finnomena.datatagging.platform.DataTaggingStorage
import com.finnomena.datatagging.platform.createHttpClient

/**
 * Factory for creating DataTaggingManager instances
 */
object DataTaggingFactory {

    /**
     * Create a DataTaggingManager with the specified configuration and storage
     */
    fun create(
        config: DataTaggingConfig,
        storage: DataTaggingStorage
    ): DataTaggingManager {
        return DataTaggingManager(
            config = config,
            storage = storage,
            httpClient = createHttpClient()
        )
    }

    /**
     * Create a DataTaggingManager for development environment
     */
    fun createDevelopment(storage: DataTaggingStorage, userAgent: String): DataTaggingManager {
        return create(DataTaggingConfig.development(userAgent), storage)
    }

    /**
     * Create a DataTaggingManager for UAT environment
     */
    fun createUAT(storage: DataTaggingStorage, userAgent: String): DataTaggingManager {
        return create(DataTaggingConfig.uat(userAgent), storage)
    }

    /**
     * Create a DataTaggingManager for production environment
     */
    fun createProduction(storage: DataTaggingStorage, userAgent: String): DataTaggingManager {
        return create(DataTaggingConfig.production(userAgent), storage)
    }
}
