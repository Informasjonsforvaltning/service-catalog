package no.digdir.servicecatalog.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class LocalizedStrings(
        val nb: String?,
        val nn: String?,
        val en: String?
)

fun LocalizedStrings.hasData() =
        when {
                !en.isNullOrBlank() -> true
                !nb.isNullOrBlank() -> true
                !nn.isNullOrBlank() -> true
                else -> false
        }
