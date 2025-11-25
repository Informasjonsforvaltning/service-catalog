package no.digdir.servicecatalog.service

import no.digdir.servicecatalog.model.ServiceCount
import no.digdir.servicecatalog.model.ServiceType
import no.digdir.servicecatalog.repository.ServiceRepository
import org.springframework.stereotype.Service

@Service
class CountService(
    private val serviceRepository: ServiceRepository,
) {
    private fun getAllDistinctCatalogIds(): List<String> =
        serviceRepository.findAll()
            .map { it.catalogId }
            .distinct()

    fun getServiceCountForListOfCatalogs(catalogIds: Set<String>): List<ServiceCount> =
        catalogIds.map { getServiceCountForCatalog(it) }

    fun getServiceCountForAllCatalogs(): List<ServiceCount> =
        getAllDistinctCatalogIds().map { getServiceCountForCatalog(it) }

    private fun getServiceCountForCatalog(catalogId: String): ServiceCount {
        val allServices = serviceRepository.findByCatalogId(catalogId)
        return ServiceCount(
            catalogId = catalogId,
            serviceCount = allServices
                .filter { it.serviceType == ServiceType.SERVICE.name }
                .size,
            publicServiceCount = allServices
                .filter { it.serviceType == ServiceType.PUBLIC_SERVICE.name }
                .size,
        )
    }
}
