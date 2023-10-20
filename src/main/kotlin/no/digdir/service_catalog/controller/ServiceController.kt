package no.digdir.service_catalog.controller

import no.digdir.service_catalog.model.Service
import no.digdir.service_catalog.mongodb.ServiceRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@CrossOrigin
@RequestMapping(value = ["/services"])
class ServiceController(private val serviceRepository: ServiceRepository) {

    @GetMapping
    fun getAllServices(): ResponseEntity<List<Service>> =
        ResponseEntity(serviceRepository.findAll(), HttpStatus.OK)

}
