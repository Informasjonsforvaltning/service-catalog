package no.digdir.servicecatalog.service

import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.digdir.servicecatalog.exception.CustomBadRequestException
import no.digdir.servicecatalog.exception.CustomNotFoundException
import no.digdir.servicecatalog.domain.ServiceValues
import no.digdir.servicecatalog.dto.JsonPatchOperation
import no.digdir.servicecatalog.dto.ServiceDTO
import no.digdir.servicecatalog.entity.ServiceEntity
import no.digdir.servicecatalog.entity.ServiceType
import no.digdir.servicecatalog.repository.ServiceRepository
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class ServiceService(private val serviceRepository: ServiceRepository) {
    private val logger = LoggerFactory.getLogger(ServiceService::class.java)

    private fun ServiceEntity.toDTO(): ServiceDTO? =
        if (serviceType == ServiceType.SERVICE.name) {
            val values = jacksonObjectMapper().convertValue<ServiceValues>(data)

            ServiceDTO(
                id = id,
                catalogId = catalogId,
                published = published,
                title = values.title,
                description = values.description,
                produces = values.produces,
                contactPoints = values.contactPoints,
                homepage = values.homepage,
                status = values.status
            )
        } else {
            null
        }

    fun findServicesByCatalogId(catalogId: String) =
        serviceRepository.findByCatalogIdAndServiceType(catalogId, ServiceType.SERVICE.name)
            .mapNotNull { it.toDTO() }

    private fun findById(id: String, catalogId: String): ServiceEntity? =
        serviceRepository
            .findByIdOrNull(id)
            ?.takeIf { it.catalogId == catalogId && it.serviceType == ServiceType.SERVICE.name }

    fun findServiceById(id: String, catalogId: String): ServiceDTO =
        findById(id, catalogId)
            ?.toDTO()
            ?: throw CustomNotFoundException()

    fun patchService(id: String, catalogId: String, operations: List<JsonPatchOperation>): ServiceDTO? =
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
                ?.toDTO()
                ?: throw CustomNotFoundException()
        } catch (ex: Exception) {
            logger.error("Failed to update service with id $id in catalog $catalogId", ex)
            throw ex
        }

    fun deleteService(id: String, catalogId: String) =
        try {
            findById(id, catalogId)
                ?.run { serviceRepository.delete(this) }
                ?: throw CustomNotFoundException()
        } catch (ex: Exception) {
            logger.error("Failed to delete service with id $id in catalog $catalogId", ex)
            throw ex
        }

    fun createService(catalogId: String, values: ServiceValues): ServiceDTO =
        try {
            serviceRepository.save(
                ServiceEntity(
                    id = UUID.randomUUID().toString(),
                    catalogId = catalogId,
                    published = false,
                    serviceType = ServiceType.SERVICE.name,
                    data = jacksonObjectMapper().convertValue<Map<String, Any>>(values)
                )
            ).toDTO()!!
        } catch (ex: Exception) {
            logger.error("Failed to create service for $catalogId", ex)
            throw ex
        }

    fun publishService(id: String, catalogId: String): ServiceDTO?  =
        try {
            findById(id, catalogId)
                ?. also { if (it.published) throw CustomBadRequestException() }
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
                ?.toDTO()
                ?: throw CustomNotFoundException()
        } catch (ex: Exception) {
            logger.error("Failed to publish service with id $id in catalog $catalogId", ex)
            throw ex
        }

    fun unpublishService(id: String, catalogId: String): ServiceDTO?  =
        try {
            findById(id, catalogId)
                ?. also { if (!it.published) throw CustomBadRequestException() }
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
                ?.toDTO()
                ?: throw CustomNotFoundException()
        } catch (ex: Exception) {
            logger.error("Failed to unpublish service with id $id in catalog $catalogId", ex)
            throw ex
        }

    fun publishedServicesInCatalog(catalogId: String): List<ServiceDTO> =
        serviceRepository.findByCatalogIdAndPublished(catalogId, true)
            .mapNotNull { it.toDTO() }

    fun getPublishedServiceInCatalog(id: String, catalogId: String): ServiceDTO =
        findServiceById(id, catalogId)
            .takeIf { it.published }
            ?: throw CustomNotFoundException()
}
