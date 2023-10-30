package no.digdir.service_catalog.controller

import no.digdir.service_catalog.model.PublicService
import no.digdir.service_catalog.security.EndpointPermissions
import no.digdir.service_catalog.service.PublicServiceService
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
@RequestMapping(value = ["/catalogs/{catalogId}/public-services"])
class PublicServiceController(private val publicServiceService: PublicServiceService, private val endpointPermissions: EndpointPermissions) {

    @GetMapping
    fun getAllPublicServices(@AuthenticationPrincipal jwt: Jwt, @PathVariable catalogId: String): ResponseEntity<List<PublicService>> =
        if (endpointPermissions.hasOrgReadPermission(jwt, catalogId)) {
            ResponseEntity(publicServiceService.findPublicServicesByCatalogId(catalogId), HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.FORBIDDEN)
        }
}
