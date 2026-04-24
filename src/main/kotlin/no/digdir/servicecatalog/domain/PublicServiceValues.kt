package no.digdir.servicecatalog.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class PublicServiceValues(
    val title: LocalizedStrings?,
    val description: LocalizedStrings?,
    val dctType: Set<String>?,
    val produces: List<Output>?,
    val contactPoints: List<ContactPoint>?,
    val homepage: String?,
    val status: String?,
    val spatial: List<String>?,
    val subject: Set<String>?,
    val thematicArea: Set<String>?
)
