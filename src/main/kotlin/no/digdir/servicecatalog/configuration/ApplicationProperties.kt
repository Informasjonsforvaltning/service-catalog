package no.digdir.servicecatalog.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("application")
data class ApplicationProperties(
    val serviceCatalogUri: String
)