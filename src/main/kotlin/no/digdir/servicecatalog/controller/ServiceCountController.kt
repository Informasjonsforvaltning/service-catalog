package no.digdir.servicecatalog.controller

import no.digdir.servicecatalog.model.*
import no.digdir.servicecatalog.security.EndpointPermissions
import no.digdir.servicecatalog.service.CountService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@CrossOrigin
@RequestMapping(value = ["/internal/catalogs/count"])
class CatalogCountController(
    private val endpointPermissions: EndpointPermissions,
    private val countService: CountService
) {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getServiceCountsForPermittedCatalogs(
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<List<ServiceCount>> {
        return when {
            endpointPermissions.hasSysAdminPermission(jwt) -> {
                ResponseEntity(
                    countService.getServiceCountForAllCatalogs(),
                    HttpStatus.OK
                )
            }
            else -> ResponseEntity(
                countService.getServiceCountForListOfCatalogs(
                    endpointPermissions.getOrgsWithMinimumReadPermission(
                        jwt,
                    )
                ), HttpStatus.OK
            )
        }
    }
}
