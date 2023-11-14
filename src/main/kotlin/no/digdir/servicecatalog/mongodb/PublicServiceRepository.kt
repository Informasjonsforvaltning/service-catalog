package no.digdir.servicecatalog.mongodb

import no.digdir.servicecatalog.model.PublicService
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface PublicServiceRepository : MongoRepository<PublicService, String> {
    fun getByCatalogId(catalogId: String): List<PublicService>
    fun getByCatalogIdAndPublished(catalogId: String, isPublished: Boolean): List<PublicService>
}
