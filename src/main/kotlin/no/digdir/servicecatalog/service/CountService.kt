package no.digdir.servicecatalog.service

import no.digdir.servicecatalog.model.ServiceCount
import no.digdir.servicecatalog.mongodb.PublicServiceRepository
import no.digdir.servicecatalog.mongodb.ServiceRepository
import org.springframework.stereotype.Service

@Service
class CountService(
    private val serviceRepository: ServiceRepository,
    private val publicServiceRepository: PublicServiceRepository,
) {
    private fun getAllDistinctCatalogIds(): List<String> {
        val serviceCatalogIds = serviceRepository.findAll().map { it.catalogId }
        val publicServiceCatalogIds = publicServiceRepository.findAll().map { it.catalogId }

        return (serviceCatalogIds + publicServiceCatalogIds).distinct()
    }

    fun getServiceCountForListOfCatalogs(catalogIds: Set<String>): List<ServiceCount> =
        catalogIds.map { getServiceCountForCatalog(it) }

    fun getServiceCountForAllCatalogs(): List<ServiceCount> =
        getAllDistinctCatalogIds().map { getServiceCountForCatalog(it) }

    private fun getServiceCountForCatalog(catalogId: String): ServiceCount =
        ServiceCount(
            catalogId = catalogId,
            serviceCount = serviceRepository.getByCatalogId(catalogId)
                .distinctBy { it.id }
                .size,
            publicServiceCount = publicServiceRepository.getByCatalogId(catalogId)
                .distinctBy { it.id }
                .size,
        )
}
