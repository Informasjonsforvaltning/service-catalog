package no.digdir.servicecatalog.controller

import no.digdir.servicecatalog.model.JsonPatchOperation
import no.digdir.servicecatalog.model.Service
import no.digdir.servicecatalog.model.ServiceToBeCreated
import no.digdir.servicecatalog.security.EndpointPermissions
import no.digdir.servicecatalog.service.ServiceService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin
@RequestMapping(value = ["/internal/catalogs/{catalogId}/services"])
class ServiceController(private val serviceService: ServiceService, private val endpointPermissions: EndpointPermissions) {

    @GetMapping
    fun getAllServices(@AuthenticationPrincipal jwt: Jwt, @PathVariable catalogId: String): ResponseEntity<List<Service>> =
        if (endpointPermissions.hasOrgReadPermission(jwt, catalogId)) {
            ResponseEntity(serviceService.findServicesByCatalogId(catalogId), HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.FORBIDDEN)
        }

    @GetMapping(value = [ "/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getServiceById(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable catalogId: String,
        @PathVariable id: String): ResponseEntity<Service> =
        if (endpointPermissions.hasOrgReadPermission(jwt, catalogId)) {
            serviceService.findServiceById(id, catalogId)
                ?.let { ResponseEntity(it, HttpStatus.OK) }
                ?: ResponseEntity(HttpStatus.NOT_FOUND)
        } else {
            ResponseEntity(HttpStatus.FORBIDDEN)
        }

    @PatchMapping(value = ["/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun patchService(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable catalogId: String,
        @PathVariable id: String,
        @RequestBody patchOperations: List<JsonPatchOperation>
    ): ResponseEntity<Service> =
        if (endpointPermissions.hasOrgWritePermission(jwt, catalogId)) {
            serviceService.patchService(id, catalogId, patchOperations)
                ?.let { ResponseEntity(it, HttpStatus.OK) }
                ?: ResponseEntity(HttpStatus.NOT_FOUND)
        } else {
            ResponseEntity(HttpStatus.FORBIDDEN)
        }

    @DeleteMapping(value = ["/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun deleteService(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable catalogId: String,
        @PathVariable id: String,
    ): ResponseEntity<HttpStatus> =
        if (endpointPermissions.hasOrgWritePermission(jwt, catalogId)) {
            serviceService.deleteService(id, catalogId)
            ResponseEntity(HttpStatus.NO_CONTENT)
        } else {
            ResponseEntity(HttpStatus.FORBIDDEN)
        }

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createService(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable catalogId: String,
        @RequestBody serviceToBeCreated: ServiceToBeCreated
    ): ResponseEntity<HttpStatus> =
        if (endpointPermissions.hasOrgWritePermission(jwt, catalogId)) {
            val created = serviceService.createService(catalogId, serviceToBeCreated)
            ResponseEntity(
                locationHeaderForCreated(newId = created.id, catalogId),
                HttpStatus.CREATED
            )
        } else ResponseEntity<HttpStatus>(HttpStatus.FORBIDDEN)

    @PostMapping(value = ["/{id}/publish"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun publishService(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable catalogId: String,
        @PathVariable id: String,
    ): ResponseEntity<Service> =
        if (endpointPermissions.hasOrgWritePermission(jwt, catalogId)) {
            ResponseEntity(serviceService.publishService(id, catalogId), HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.FORBIDDEN)
        }

    @PostMapping(value = ["/{id}/unpublish"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun unpublishService(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable catalogId: String,
        @PathVariable id: String,
    ): ResponseEntity<Service> =
        if (endpointPermissions.hasOrgWritePermission(jwt, catalogId)) {
            ResponseEntity(serviceService.unpublishService(id, catalogId), HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.FORBIDDEN)
        }
}

private fun locationHeaderForCreated(newId: String, catalogId: String): HttpHeaders =
    HttpHeaders().apply {
        add(HttpHeaders.LOCATION, "/internal/catalogs/$catalogId/services/$newId")
        add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.LOCATION)
    }
