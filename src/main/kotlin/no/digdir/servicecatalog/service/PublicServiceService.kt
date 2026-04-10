package no.digdir.servicecatalog.service

import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.digdir.servicecatalog.adapter.HarvestAdminClient
import no.digdir.servicecatalog.exception.CustomBadRequestException
import no.digdir.servicecatalog.exception.CustomNotFoundException
import no.digdir.servicecatalog.domain.PublicServiceValues
import no.digdir.servicecatalog.dto.JsonPatchOperation
import no.digdir.servicecatalog.dto.PublicServiceDTO
import no.digdir.servicecatalog.entity.ServiceEntity
import no.digdir.servicecatalog.entity.ServiceType
import no.digdir.servicecatalog.repository.ServiceRepository
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class PublicServiceService(
    private val serviceRepository: ServiceRepository,
    private val harvestAdminClient: HarvestAdminClient
) {
    private val logger = LoggerFactory.getLogger(PublicServiceService::class.java)
    private val mapper = jacksonObjectMapper()

    private fun ServiceEntity.toDTO(): PublicServiceDTO? =
        if (serviceType == ServiceType.PUBLIC_SERVICE.name) {
            val values = mapper.convertValue<PublicServiceValues>(data)
            PublicServiceDTO(
                id = id,
                catalogId = catalogId,
                published = published,
                title = values.title,
                description = values.description,
                dctType = values.dctType,
                produces = values.produces,
                contactPoints = values.contactPoints,
                homepage = values.homepage,
                status = values.status,
                spatial = values.spatial,
                subject = values.subject
            )
        } else null

    fun findPublicServicesByCatalogId(catalogId: String): List<PublicServiceDTO> =
        serviceRepository.findByCatalogIdAndServiceType(catalogId, ServiceType.PUBLIC_SERVICE.name)
            .mapNotNull { it.toDTO() }

    private fun findById(id: String, catalogId: String): ServiceEntity? =
        serviceRepository
            .findByIdOrNull(id)
            ?.takeIf { it.catalogId == catalogId && it.serviceType == ServiceType.PUBLIC_SERVICE.name }

    fun findPublicServiceById(id: String, catalogId: String): PublicServiceDTO? =
        findById(id, catalogId)?.toDTO()

    fun patchPublicService(id: String, catalogId: String, operations: List<JsonPatchOperation>): PublicServiceDTO? =
        try {
            val dbo = findById(id, catalogId)
            dbo?.data
                ?.let { patchOriginal(it, operations) }
                ?.let { patchedData ->
                    serviceRepository.save(
                        ServiceEntity(
                            id = dbo.id,
                            catalogId = dbo.catalogId,
                            published = dbo.published,
                            serviceType = dbo.serviceType,
                            data = patchedData
                        )
                    )
                }
                ?.also { if (it.published) harvestAdminClient.triggerHarvest(catalogId) }
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
                    data = mapper.convertValue<Map<String, Any?>>(values)
                )
            ).toDTO()!!
        } catch (ex: Exception) {
            logger.error("Failed to create public service for $catalogId", ex)
            throw ex
        }

    fun publishPublicService(id: String, catalogId: String): PublicServiceDTO? =
        try {
            findById(id, catalogId)
                ?.also { if (it.published) throw CustomBadRequestException() }
                ?.let { dbo ->
                    serviceRepository.save(
                        ServiceEntity(
                            id = dbo.id,
                            catalogId = dbo.catalogId,
                            published = true,
                            serviceType = dbo.serviceType,
                            data = dbo.data
                        )
                    )
                }
                ?.also { if (catalogHasAnyPublishedServices(catalogId)) harvestAdminClient.createNewDataSource(catalogId) }
                ?.also { harvestAdminClient.triggerHarvest(catalogId) }
                ?.toDTO()
                ?: throw CustomNotFoundException()
        } catch (ex: Exception) {
            logger.error("Failed to publish public service with id $id in catalog $catalogId", ex)
            throw ex
        }

    fun unpublishPublicService(id: String, catalogId: String): PublicServiceDTO? =
        try {
            findById(id, catalogId)
                ?.also { if (!it.published) throw CustomBadRequestException() }
                ?.let { dbo ->
                    serviceRepository.save(
                        ServiceEntity(
                            id = dbo.id,
                            catalogId = dbo.catalogId,
                            published = false,
                            serviceType = dbo.serviceType,
                            data = dbo.data
                        )
                    )
                }
                ?.also { harvestAdminClient.triggerHarvest(catalogId) }
                ?.toDTO()
                ?: throw CustomNotFoundException()
        } catch (ex: Exception) {
            logger.error("Failed to unpublish public service with id $id in catalog $catalogId", ex)
            throw ex
        }

    fun publishedServicesInCatalog(catalogId: String): List<PublicServiceDTO> =
        serviceRepository.findByCatalogIdAndPublished(catalogId, true)
            .mapNotNull { it.toDTO() }

    fun catalogHasAnyPublishedServices(catalogId: String): Boolean =
        serviceRepository.findByCatalogIdAndPublished(catalogId, true).isNotEmpty()

    fun getPublishedPublicServiceInCatalog(id: String, catalogId: String): PublicServiceDTO =
        findPublicServiceById(id, catalogId)
            ?.takeIf { it.published }
            ?: throw CustomNotFoundException()
}
