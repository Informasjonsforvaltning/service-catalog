package no.digdir.servicecatalog.model

data class ServiceCount (
    val catalogId: String,
    val count: Int
)

data class CombinedServiceCounts (
    val catalogId: String,
    val serviceCount: Int,
    val publicServiceCount: Int
)
