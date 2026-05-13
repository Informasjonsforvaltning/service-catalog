package no.digdir.servicecatalog.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Cost(
    val value: Double? = null,
    val description: LocalizedStrings? = null,
    val documentation: List<String>? = null,
    val currency: String? = null,
)
