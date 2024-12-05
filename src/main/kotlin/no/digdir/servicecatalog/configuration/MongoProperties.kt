package no.digdir.servicecatalog.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("application.mongodb")
data class MongoProperties(
    val username: String,
    val password: String,
    val host: String,
    val auth: String,
    val rs: String,
    val db: String
)
