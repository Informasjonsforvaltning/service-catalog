package no.digdir.service_catalog.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "publicServices")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@CompoundIndexes(value = [CompoundIndex(name = "catalog_id", def = "{'catalogId' : 1}")])
data class PublicService (
    val id: String,
    val catalogId: String,
    val title: LocalizedStrings?,
    val description: LocalizedStrings?
)
