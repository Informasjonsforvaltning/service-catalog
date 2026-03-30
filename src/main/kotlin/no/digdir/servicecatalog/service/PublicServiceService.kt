package no.digdir.servicecatalog.service

import no.digdir.servicecatalog.adapter.HarvestAdminClient
import no.digdir.servicecatalog.exception.CustomBadRequestException
import no.digdir.servicecatalog.exception.CustomNotFoundException
import no.digdir.servicecatalog.model.JsonPatchOperation
import no.digdir.servicecatalog.model.PublicService
import no.digdir.servicecatalog.model.PublicServiceToBeCreated
import no.digdir.servicecatalog.repository.PublicServiceRepository
import no.digdir.servicecatalog.repository.ServiceRepository
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class PublicServiceService(
    private val publicServiceRepository: PublicServiceRepository,
    private val serviceRepository: ServiceRepository,
    private val harvestAdminClient: HarvestAdminClient
) {
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
                ?.also { if (it.published) harvestAdminClient.triggerHarvest(catalogId) }
        } catch (ex: Exception) {
            logger.error("Failed to update public service with id $id in catalog $catalogId", ex)
            throw ex
        }

    fun deletePublicService(id: String, catalogId: String) =
        try {
            findPublicServiceById(id, catalogId)
                ?.run { publicServiceRepository.delete(this) }
                ?: throw CustomNotFoundException()
        } catch (ex: Exception) {
            logger.error("Failed to delete public service with id $id in catalog $catalogId", ex)
            throw ex
        }

    fun createPublicService(catalogId: String, publicServiceToBeCreated: PublicServiceToBeCreated): PublicService =
        try {
            PublicService(
                id = UUID.randomUUID().toString(),
                catalogId = catalogId,
                title = publicServiceToBeCreated.title,
                description = publicServiceToBeCreated.description,
                dctType = publicServiceToBeCreated.dctType,
                produces = publicServiceToBeCreated.produces,
                contactPoints = publicServiceToBeCreated.contactPoints,
                homepage = publicServiceToBeCreated.homepage,
                status = publicServiceToBeCreated.status,
                spatial = publicServiceToBeCreated.spatial,
                subject = publicServiceToBeCreated.subject
            ).let { publicServiceRepository.save(it) }
        } catch (ex: Exception) {
            logger.error("Failed to create public service for $catalogId", ex)
            throw ex
        }

    fun publishPublicService(id: String, catalogId: String): PublicService?  =
        try {
            findPublicServiceById(id, catalogId)
                ?.also { if (it.published) throw CustomBadRequestException() }
                ?.let { publicServiceRepository.save(it.copy(published = true)) }
                ?.also { if (catalogHasAnyPublishedServices(catalogId)) harvestAdminClient.createNewDataSource(catalogId) }
                ?.also { harvestAdminClient.triggerHarvest(catalogId) }
                ?: throw CustomNotFoundException()
        } catch (ex: Exception) {
            logger.error("Failed to publish public service with id $id in catalog $catalogId", ex)
            throw ex
        }

    fun unpublishPublicService(id: String, catalogId: String): PublicService?  =
        try {
            findPublicServiceById(id, catalogId)
                ?.also { if (!it.published) throw CustomBadRequestException() }
                ?.let { publicServiceRepository.save(it.copy(published = false)) }
                ?.also { harvestAdminClient.triggerHarvest(catalogId) }
                ?: throw CustomNotFoundException()
        } catch (ex: Exception) {
            logger.error("Failed to unpublish public service with id $id in catalog $catalogId", ex)
            throw ex
        }

    fun publishedServicesInCatalog(catalogId: String): List<PublicService> =
        publicServiceRepository.getByCatalogIdAndPublished(catalogId, true)

    fun catalogHasAnyPublishedServices(catalogId: String): Boolean =
        publicServiceRepository.getByCatalogIdAndPublished(catalogId, true).isNotEmpty()
            || serviceRepository.getByCatalogIdAndPublished(catalogId, true).isNotEmpty()

    fun getPublishedPublicServiceInCatalog(id: String, catalogId: String): PublicService =
        findPublicServiceById(id, catalogId)
            ?.takeIf { it.published }
            ?: throw CustomNotFoundException()
}
