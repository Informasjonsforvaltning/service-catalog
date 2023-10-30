package no.digdir.service_catalog.service

import no.digdir.service_catalog.model.PublicService
import no.digdir.service_catalog.mongodb.PublicServiceRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class PublicServiceService(private val publicServiceRepository: PublicServiceRepository) {
    fun findPublicServicesByCatalogId(catalogId: String) =
        publicServiceRepository.getByCatalogId(catalogId)

    fun findPublicServiceById(id: String, catalogId: String) =
        publicServiceRepository
            .findByIdOrNull(id)
            ?.takeIf { it.catalogId == catalogId }
}
