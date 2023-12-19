package no.digdir.servicecatalog.controller

import no.digdir.servicecatalog.model.*
import no.digdir.servicecatalog.security.EndpointPermissions
import no.digdir.servicecatalog.service.PublicServiceService
import no.digdir.servicecatalog.service.ServiceService
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
    private val serviceService: ServiceService,
    private val publicServiceService: PublicServiceService
) {

    private fun combineServiceCounts(
        serviceCount: List<ServiceCount>,
        publicServiceCount: List<ServiceCount>
    ): List<CombinedServiceCounts> {
        val combinedCountsList = mutableListOf<CombinedServiceCounts>()

        serviceCount.forEach { service ->
            publicServiceCount.find { publicService ->
                publicService.catalogId == service.catalogId
            }?.let { matchingPublicService ->
                combinedCountsList.add(
                    CombinedServiceCounts(
                        catalogId = service.catalogId,
                        serviceCount = service.count,
                        publicServiceCount = matchingPublicService.count
                    )
                )
            }
        }

        return combinedCountsList
    }


    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getServiceCountsForPermittedCatalogs(
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<List<CombinedServiceCounts>> {
        return when {
            endpointPermissions.hasSysAdminPermission(jwt) -> {
                val combinedCounts = combineServiceCounts(
                    serviceService.getServiceCountForAllCatalogs(),
                    publicServiceService.getServiceCountForAllCatalogs()
                )
                ResponseEntity(
                    combinedCounts,
                    HttpStatus.OK
                )
            }

            else -> {
                val combinedCounts = combineServiceCounts(
                    serviceService.getServiceCountForListOfCatalogs(
                        endpointPermissions.getOrgsByPermissions(
                            jwt,
                            OpEnum.READ
                        )
                    ),
                    publicServiceService.getServiceCountForListOfCatalogs(
                        endpointPermissions.getOrgsByPermissions(
                            jwt,
                            OpEnum.READ
                        )
                    )
                )
                ResponseEntity(combinedCounts, HttpStatus.OK)
            }
        }
    }


}