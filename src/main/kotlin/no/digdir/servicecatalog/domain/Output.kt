package no.digdir.servicecatalog.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Output(
    val identifier: String?,
    val title: LocalizedStrings?,
    val description: LocalizedStrings?,
    val language: List<String>?,
    @get:JsonProperty("isPartOf")
    @param:JsonProperty("isPartOf")
    val isPartOf: List<String>?,
    val type: List<String>?,
)
