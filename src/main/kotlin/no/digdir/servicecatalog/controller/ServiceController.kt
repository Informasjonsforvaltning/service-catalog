package no.digdir.servicecatalog.controller

import no.digdir.servicecatalog.model.Service
import no.digdir.servicecatalog.mongodb.ServiceRepository
import no.digdir.servicecatalog.security.EndpointPermissions
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@CrossOrigin
@RequestMapping(value = ["/catalogs/{catalogId}/services"])
class ServiceController(private val serviceRepository: ServiceRepository, private val endpointPermissions: EndpointPermissions) {

    @GetMapping
    fun getAllServices(@AuthenticationPrincipal jwt: Jwt, @PathVariable catalogId: String): ResponseEntity<List<Service>> =
        if (endpointPermissions.hasOrgReadPermission(jwt, catalogId)) {
            ResponseEntity(serviceRepository.findAll(), HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.FORBIDDEN)
        }
}
