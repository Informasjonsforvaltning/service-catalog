package no.digdir.servicecatalog.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Output(
    val identifier: String = UUID.randomUUID().toString(),
    val title: LocalizedStrings?,
    val description: LocalizedStrings?,
    val language: List<String>?
)
