package no.digdir.service_catalog.service

import no.digdir.service_catalog.model.JsonPatchOperation
import no.digdir.service_catalog.model.PublicService
import no.digdir.service_catalog.mongodb.PublicServiceRepository
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class PublicServiceService(private val publicServiceRepository: PublicServiceRepository) {
    private val logger = LoggerFactory.getLogger(PublicServiceService::class.java)

    fun findPublicServicesByCatalogId(catalogId: String) =
        publicServiceRepository.getByCatalogId(catalogId)

    fun findPublicServiceById(id: String, catalogId: String) =
        publicServiceRepository
            .findByIdOrNull(id)
            ?.takeIf { it.catalogId == catalogId }

    fun patchPublicService(id: String, catalogId: String, operations: List<JsonPatchOperation>): PublicService? =
        try {
            findPublicServiceById(id, catalogId)
                ?.let { patchOriginal(it, operations)}
                ?.let { publicServiceRepository.save(it) }
        } catch (ex: Exception) {
            logger.error("Failed to update public service with id $id in catalog $catalogId", ex)
            throw ex
        }

    fun deletePublicService(id: String, catalogId: String) =
        try {
            findPublicServiceById(id, catalogId)
                ?.run { publicServiceRepository.delete(this) }
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        } catch (ex: Exception) {
            logger.error("Failed to delete public service with id $id in catalog $catalogId", ex)
            throw ex
        }
}
