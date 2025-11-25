package no.digdir.servicecatalog.service

import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.digdir.servicecatalog.exception.CustomBadRequestException
import no.digdir.servicecatalog.exception.CustomNotFoundException
import no.digdir.servicecatalog.model.JsonPatchOperation
import no.digdir.servicecatalog.model.PublicServiceDTO
import no.digdir.servicecatalog.model.PublicServiceValues
import no.digdir.servicecatalog.model.ServiceEntity
import no.digdir.servicecatalog.model.ServiceType
import no.digdir.servicecatalog.repository.ServiceRepository
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class PublicServiceService(private val serviceRepository: ServiceRepository) {
    private val logger = LoggerFactory.getLogger(PublicServiceService::class.java)

    private fun ServiceEntity.toDTO(): PublicServiceDTO? =
        if (serviceType == ServiceType.PUBLIC_SERVICE.name) {
            val values = jacksonObjectMapper().convertValue<PublicServiceValues>(data)

            PublicServiceDTO(
                id = id,
                catalogId = catalogId,
                published = published,
                title = values.title,
                description = values.description,
                produces = values.produces,
                contactPoints = values.contactPoints,
                homepage = values.homepage,
                status = values.status,
                spatial = values.spatial
            )
        } else {
            null
        }

    fun findPublicServicesByCatalogId(catalogId: String): List<PublicServiceDTO> =
        serviceRepository.findByCatalogIdAndServiceType(catalogId, ServiceType.PUBLIC_SERVICE.name)
            .mapNotNull { it.toDTO() }

    private fun findById(id: String, catalogId: String): ServiceEntity? =
        serviceRepository
            .findByIdOrNull(id)
            ?.takeIf { it.catalogId == catalogId && it.serviceType == ServiceType.PUBLIC_SERVICE.name }

    fun findPublicServiceById(id: String, catalogId: String): PublicServiceDTO =
        findById(id, catalogId)
            ?.toDTO()
            ?: throw CustomNotFoundException()

    fun patchPublicService(id: String, catalogId: String, operations: List<JsonPatchOperation>): PublicServiceDTO =
        try {
            val dbo = findById(id, catalogId)
            dbo?.data
                ?.let { patchOriginal(it, operations) }
                ?.let { serviceRepository.save(dbo.copy(data = it)) }
                ?.toDTO()
                ?: throw CustomNotFoundException()
        } catch (ex: Exception) {
            logger.error("Failed to update public service with id $id in catalog $catalogId", ex)
            throw ex
        }

    fun deletePublicService(id: String, catalogId: String) =
        try {
            findById(id, catalogId)
                ?.run { serviceRepository.delete(this) }
                ?: throw CustomNotFoundException()
        } catch (ex: Exception) {
            logger.error("Failed to delete public service with id $id in catalog $catalogId", ex)
            throw ex
        }

    fun createPublicService(catalogId: String, values: PublicServiceValues): PublicServiceDTO =
        try {
            serviceRepository.save(
                ServiceEntity(
                    id = UUID.randomUUID().toString(),
                    catalogId = catalogId,
                    published = false,
                    serviceType = ServiceType.PUBLIC_SERVICE.name,
                    data = jacksonObjectMapper().convertValue<Map<String, Any>>(values)
                )
            ).toDTO()!!
        } catch (ex: Exception) {
            logger.error("Failed to create public service for $catalogId", ex)
            throw ex
        }

    fun publishPublicService(id: String, catalogId: String): PublicServiceDTO?  =
        try {
            findById(id, catalogId)
                ?. also { if (it.published) throw CustomBadRequestException() }
                ?.let { serviceRepository.save(it.copy(published = true)) }
                ?.toDTO()
                ?: throw CustomNotFoundException()
        } catch (ex: Exception) {
            logger.error("Failed to publish public service with id $id in catalog $catalogId", ex)
            throw ex
        }

    fun unpublishPublicService(id: String, catalogId: String): PublicServiceDTO?  =
        try {
            findById(id, catalogId)
                ?. also { if (!it.published) throw CustomBadRequestException() }
                ?.let { serviceRepository.save(it.copy(published = false)) }
                ?.toDTO()
                ?: throw CustomNotFoundException()
        } catch (ex: Exception) {
            logger.error("Failed to unpublish public service with id $id in catalog $catalogId", ex)
            throw ex
        }

    fun publishedServicesInCatalog(catalogId: String): List<PublicServiceDTO> =
        serviceRepository.findByCatalogIdAndPublished(catalogId, true)
            .mapNotNull { it.toDTO() }

    fun getPublishedPublicServiceInCatalog(id: String, catalogId: String): PublicServiceDTO =
        findPublicServiceById(id, catalogId)
            .takeIf { it.published }
            ?: throw CustomNotFoundException()
}
