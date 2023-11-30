package no.digdir.servicecatalog.service

import no.digdir.servicecatalog.exception.CustomBadRequestException
import no.digdir.servicecatalog.exception.CustomNotFoundException
import no.digdir.servicecatalog.model.JsonPatchOperation
import no.digdir.servicecatalog.model.Service
import no.digdir.servicecatalog.model.ServiceToBeCreated
import no.digdir.servicecatalog.mongodb.ServiceRepository
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import java.util.*

@org.springframework.stereotype.Service
class ServiceService(private val serviceRepository: ServiceRepository) {
    private val logger = LoggerFactory.getLogger(ServiceService::class.java)

    fun findServicesByCatalogId(catalogId: String) =
        serviceRepository.getByCatalogId(catalogId)

    fun findServiceById(id: String, catalogId: String) =
        serviceRepository
            .findByIdOrNull(id)
            ?.takeIf { it.catalogId == catalogId }

    fun patchService(id: String, catalogId: String, operations: List<JsonPatchOperation>): Service? =
        try {
            findServiceById(id, catalogId)
                ?.let { patchOriginal(it, operations)}
                ?.let { serviceRepository.save(it) }
        } catch (ex: Exception) {
            logger.error("Failed to update service with id $id in catalog $catalogId", ex)
            throw ex
        }

    fun deleteService(id: String, catalogId: String) =
        try {
            findServiceById(id, catalogId)
                ?.run { serviceRepository.delete(this) }
                ?: throw CustomNotFoundException()
        } catch (ex: Exception) {
            logger.error("Failed to delete service with id $id in catalog $catalogId", ex)
            throw ex
        }

    fun createService(catalogId: String, serviceToBeCreated: ServiceToBeCreated): Service =
        try {
            Service(
                id = UUID.randomUUID().toString(),
                catalogId = catalogId,
                title = serviceToBeCreated.title,
                description = serviceToBeCreated.description,
                produces = serviceToBeCreated.produces
            ).let { serviceRepository.insert(it) }
        } catch (ex: Exception) {
            logger.error("Failed to create service for $catalogId", ex)
            throw ex
        }

    fun publishService(id: String, catalogId: String): Service?  =
        try {
            findServiceById(id, catalogId)
                ?. also { if (it.published) throw CustomBadRequestException() }
                ?.let { serviceRepository.save(it.copy(published = true)) }
                ?: throw CustomNotFoundException()
        } catch (ex: Exception) {
            logger.error("Failed to publish service with id $id in catalog $catalogId", ex)
            throw ex
        }

    fun unpublishService(id: String, catalogId: String): Service?  =
        try {
            findServiceById(id, catalogId)
                ?. also { if (!it.published) throw CustomBadRequestException() }
                ?.let { serviceRepository.save(it.copy(published = false)) }
                ?: throw CustomNotFoundException()
        } catch (ex: Exception) {
            logger.error("Failed to unpublish service with id $id in catalog $catalogId", ex)
            throw ex
        }

    fun publishedServicesInCatalog(catalogId: String): List<Service> =
        serviceRepository.getByCatalogIdAndPublished(catalogId, true)

    fun getPublishedServiceInCatalog(id: String, catalogId: String): Service =
        findServiceById(id, catalogId)
            ?.takeIf { it.published }
            ?: throw CustomNotFoundException()
}
