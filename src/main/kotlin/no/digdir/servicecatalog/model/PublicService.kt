package no.digdir.servicecatalog.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "publicServices")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@CompoundIndexes(value = [
    CompoundIndex(name = "catalog_id", def = "{'catalogId' : 1}"),
    CompoundIndex(name = "catalog_id_published", def = "{'catalogId' : 1, 'published': 1}")
])
data class PublicService (
    val id: String,
    val catalogId: String,
    val title: LocalizedStrings?,
    val description: LocalizedStrings?,
    val published: Boolean = false,
    val produces: List<Output>?,
    val contactPoints: List<ContactPoint>?,
    val homepage: String?,
    val status: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PublicServiceToBeCreated(
    val title: LocalizedStrings?,
    val description: LocalizedStrings?,
    val produces: List<Output>?,
    val contactPoints: List<ContactPoint>?,
    val homepage: String?,
    val status: String?
)
