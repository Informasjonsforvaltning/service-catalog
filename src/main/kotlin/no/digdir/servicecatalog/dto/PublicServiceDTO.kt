package no.digdir.servicecatalog.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import no.digdir.servicecatalog.domain.ContactPoint
import no.digdir.servicecatalog.domain.LocalizedStrings
import no.digdir.servicecatalog.domain.Output

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
