package no.digdir.servicecatalog.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class PublicServiceDTO (
    val id: String,
    val catalogId: String,
    val title: LocalizedStrings?,
    val description: LocalizedStrings?,
    val published: Boolean = false,
    val produces: List<Output>?,
    val contactPoints: List<ContactPoint>?,
    val homepage: String?,
    val status: String?,
    val spatial: List<String>?
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class PublicServiceValues (
    val title: LocalizedStrings?,
    val description: LocalizedStrings?,
    val produces: List<Output>?,
    val contactPoints: List<ContactPoint>?,
    val homepage: String?,
    val status: String?,
    val spatial: List<String>?
)
