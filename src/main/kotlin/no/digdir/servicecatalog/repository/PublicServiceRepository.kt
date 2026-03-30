package no.digdir.servicecatalog.repository

import no.digdir.servicecatalog.model.PublicService
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PublicServiceRepository : JpaRepository<PublicService, String> {
    fun getByCatalogId(catalogId: String): List<PublicService>
    fun getByCatalogIdAndPublished(catalogId: String, published: Boolean): List<PublicService>
}
