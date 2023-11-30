package no.digdir.servicecatalog.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class ContactPoint(
    val category: LocalizedStrings?,
    val contactPage: List<String>?,
    val telephone: List<String>?,
    val email: List<String>?,
    val language: List<String>?
)
