package no.digdir.servicecatalog.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class ServiceCount(
    val catalogId: String,
    val serviceCount: Int,
    val publicServiceCount: Int
)
