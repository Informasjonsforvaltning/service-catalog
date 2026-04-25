package no.digdir.servicecatalog.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Evidence(
    val identifier: String? = UUID.randomUUID().toString(),
    val title: LocalizedStrings?,
    val description: LocalizedStrings?,
    val language: List<String>?,
    val relatedDocumentation: List<String>?,
    val dataset: List<String>?
)
