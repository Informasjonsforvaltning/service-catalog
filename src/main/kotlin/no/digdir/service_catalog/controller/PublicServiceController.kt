package no.digdir.service_catalog.controller

import no.digdir.service_catalog.model.*
import no.digdir.service_catalog.security.EndpointPermissions
import no.digdir.service_catalog.service.PublicServiceService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@CrossOrigin
@RequestMapping(value = ["/catalogs/{catalogId}/public-services"])
class PublicServiceController(private val publicServiceService: PublicServiceService, private val endpointPermissions: EndpointPermissions) {

    @GetMapping
    fun getAllPublicServices(@AuthenticationPrincipal jwt: Jwt, @PathVariable catalogId: String): ResponseEntity<List<PublicService>> =
        if (endpointPermissions.hasOrgReadPermission(jwt, catalogId)) {
            ResponseEntity(publicServiceService.findPublicServicesByCatalogId(catalogId), HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.FORBIDDEN)
        }

    @GetMapping(value = [ "/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getPublicServiceById(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable catalogId: String,
        @PathVariable id: String): ResponseEntity<PublicService> =
            if (endpointPermissions.hasOrgReadPermission(jwt, catalogId)) {
                publicServiceService.findPublicServiceById(id, catalogId)
                    ?.let { ResponseEntity(it, HttpStatus.OK) }
                    ?: ResponseEntity(HttpStatus.NOT_FOUND)
            } else {
                ResponseEntity(HttpStatus.FORBIDDEN)
            }

    @PatchMapping(value = ["/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun patchPublicService(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable catalogId: String,
        @PathVariable id: String,
        @RequestBody patchOperations: List<JsonPatchOperation>
    ): ResponseEntity<PublicService> =
        if (endpointPermissions.hasOrgWritePermission(jwt, catalogId)) {
            publicServiceService.patchPublicService(id, catalogId, patchOperations)
                ?.let { ResponseEntity(it, HttpStatus.OK) }
                ?: ResponseEntity(HttpStatus.NOT_FOUND)
        } else {
            ResponseEntity(HttpStatus.FORBIDDEN)
        }

    @DeleteMapping(value = ["/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun deletePublicService(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable catalogId: String,
        @PathVariable id: String,
    ): ResponseEntity<HttpStatus> =
        if (endpointPermissions.hasOrgWritePermission(jwt, catalogId)) {
            publicServiceService.deletePublicService(id, catalogId)
            ResponseEntity(HttpStatus.NO_CONTENT)
        } else {
            ResponseEntity(HttpStatus.FORBIDDEN)
        }

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createPublicService(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable catalogId: String,
        @RequestBody publicServiceToBeCreated: PublicServiceToBeCreated
    ): ResponseEntity<HttpStatus> =
        if (endpointPermissions.hasOrgWritePermission(jwt, catalogId)) {
            val created = publicServiceService.createPublicService(catalogId, publicServiceToBeCreated)
            ResponseEntity(
                locationHeaderForCreated(newId = created.id, catalogId),
                HttpStatus.CREATED
            )
        } else ResponseEntity<HttpStatus>(HttpStatus.FORBIDDEN)

    @PostMapping(value = ["/{id}/publish"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun publishPublicService(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable catalogId: String,
        @PathVariable id: String,
    ): ResponseEntity<PublicService> =
        if (endpointPermissions.hasOrgWritePermission(jwt, catalogId)) {
            ResponseEntity(publicServiceService.publishPublicService(id, catalogId), HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.FORBIDDEN)
        }
}

private fun locationHeaderForCreated(newId: String, catalogId: String): HttpHeaders =
    HttpHeaders().apply {
        add(HttpHeaders.LOCATION, "/$catalogId/catalogs/{catalogId}/public-services/$newId")
        add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.LOCATION)
    }
