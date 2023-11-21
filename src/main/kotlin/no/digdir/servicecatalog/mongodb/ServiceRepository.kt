package no.digdir.servicecatalog.mongodb

import no.digdir.servicecatalog.model.Service
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface ServiceRepository : MongoRepository<Service, String?>
