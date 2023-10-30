package no.digdir.service_catalog.service

import no.digdir.service_catalog.mongodb.PublicServiceRepository
import org.springframework.stereotype.Service

@Service
class PublicServiceService(private val publicServiceRepository: PublicServiceRepository) {
    fun findPublicServicesByCatalogId(catalogId: String) =
        publicServiceRepository.getByCatalogId(catalogId)
}
