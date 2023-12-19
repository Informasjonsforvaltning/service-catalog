package no.digdir.servicecatalog.service

import no.digdir.servicecatalog.exception.CustomBadRequestException
import no.digdir.servicecatalog.exception.CustomNotFoundException
import no.digdir.servicecatalog.model.JsonPatchOperation
import no.digdir.servicecatalog.model.PublicService
import no.digdir.servicecatalog.model.PublicServiceToBeCreated
import no.digdir.servicecatalog.model.ServiceCount
import no.digdir.servicecatalog.mongodb.PublicServiceRepository
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class PublicServiceService(private val publicServiceRepository: PublicServiceRepository, private val mongoOperations: MongoOperations) {
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
                produces = publicServiceToBeCreated.produces,
                contactPoints = publicServiceToBeCreated.contactPoints,
                homepage = publicServiceToBeCreated.homepage,
                status = publicServiceToBeCreated.status
            ).let { publicServiceRepository.insert(it) }
        } catch (ex: Exception) {
            logger.error("Failed to create public service for $catalogId", ex)
            throw ex
        }

    fun publishPublicService(id: String, catalogId: String): PublicService?  =
        try {
            findPublicServiceById(id, catalogId)
                ?. also { if (it.published) throw CustomBadRequestException() }
                ?.let { publicServiceRepository.save(it.copy(published = true)) }
                ?: throw CustomNotFoundException()
        } catch (ex: Exception) {
            logger.error("Failed to publish public service with id $id in catalog $catalogId", ex)
            throw ex
        }

    fun unpublishPublicService(id: String, catalogId: String): PublicService?  =
        try {
            findPublicServiceById(id, catalogId)
                ?. also { if (!it.published) throw CustomBadRequestException() }
                ?.let { publicServiceRepository.save(it.copy(published = false)) }
                ?: throw CustomNotFoundException()
        } catch (ex: Exception) {
            logger.error("Failed to unpublish public service with id $id in catalog $catalogId", ex)
            throw ex
        }

    fun publishedServicesInCatalog(catalogId: String): List<PublicService> =
        publicServiceRepository.getByCatalogIdAndPublished(catalogId, true)

    fun getPublishedPublicServiceInCatalog(id: String, catalogId: String): PublicService =
        findPublicServiceById(id, catalogId)
            ?.takeIf { it.published }
            ?: throw CustomNotFoundException()

    private fun getAllCatalogIds(): List<String> {
        return mongoOperations
            .query(no.digdir.servicecatalog.model.Service::class.java)
            .distinct("catalogId")
            .`as`(String::class.java)
            .all()
    }

    fun getServiceCountForAllCatalogs(): List<ServiceCount> =
        getAllCatalogIds()
            .map { getServiceCountForCatalog(it) }

    private fun getServiceCountForCatalog(catalogId: String): ServiceCount =
        ServiceCount(
            catalogId = catalogId,
            count = findPublicServicesByCatalogId(catalogId)
                .distinctBy { it.id }
                .size,
        )

    fun getServiceCountForListOfCatalogs(catalogIds: Set<String>): List<ServiceCount> =
        catalogIds
            .map { getServiceCountForCatalog(it) }
            .filter { it.count > 0 }
}
