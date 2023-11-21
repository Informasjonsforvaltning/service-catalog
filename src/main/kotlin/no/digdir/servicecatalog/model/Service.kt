package no.digdir.servicecatalog.model

import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "services")
data class Service (
    val id: String,
    val title: String
)
