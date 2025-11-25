package no.digdir.servicecatalog.repository

import no.digdir.servicecatalog.model.ServiceEntity
import no.digdir.servicecatalog.model.ServiceType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ServiceRepository : JpaRepository<ServiceEntity, String> {
    fun findByCatalogId(catalogId: String): List<ServiceEntity>
    fun findByCatalogIdAndServiceType(catalogId: String, serviceType: String): List<ServiceEntity>
    fun findByCatalogIdAndPublished(catalogId: String, published: Boolean): List<ServiceEntity>
}
