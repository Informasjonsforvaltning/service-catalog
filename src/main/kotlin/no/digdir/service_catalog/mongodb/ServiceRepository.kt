package no.digdir.service_catalog.mongodb

import no.digdir.service_catalog.model.Service
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface ServiceRepository : MongoRepository<Service, String?>
