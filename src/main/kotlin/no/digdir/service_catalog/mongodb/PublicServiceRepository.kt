package no.digdir.service_catalog.mongodb

import no.digdir.service_catalog.model.PublicService
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface PublicServiceRepository : MongoRepository<PublicService, String> {
    fun getByCatalogId(catalogId: String): List<PublicService>
}
