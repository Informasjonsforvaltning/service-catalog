package no.digdir.servicecatalog.repository

import no.digdir.servicecatalog.model.Service
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ServiceRepository : JpaRepository<Service, String> {
    fun getByCatalogId(catalogId: String): List<Service>
    fun getByCatalogIdAndPublished(catalogId: String, published: Boolean): List<Service>
}
