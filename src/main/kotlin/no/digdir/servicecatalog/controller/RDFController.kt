package no.digdir.servicecatalog.controller

import no.digdir.servicecatalog.rdf.jenaLangFromAcceptHeader
import no.digdir.servicecatalog.service.RDFService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@CrossOrigin
@RequestMapping(
    produces = ["text/turtle", "text/n3", "application/rdf+json", "application/rdf+xml",
        "application/n-triples", "application/n-quads", "application/trig", "application/trix"],
    value = ["/rdf/catalogs"])
class RDFController(private val rdfService: RDFService) {

    @GetMapping(value = ["/{catalogId}"])
    fun getCatalogRDF(@RequestHeader(HttpHeaders.ACCEPT) accept: String?, @PathVariable catalogId: String): ResponseEntity<String> =
        ResponseEntity(rdfService.serializeCatalog(catalogId, jenaLangFromAcceptHeader(accept)), HttpStatus.OK)

    @GetMapping(value = ["/{catalogId}/public-services/{id}"])
    fun getPublicServiceRDF(
        @RequestHeader(HttpHeaders.ACCEPT) accept: String?,
        @PathVariable catalogId: String,
        @PathVariable id: String
    ): ResponseEntity<String> =
        rdfService.serializeService(catalogId, id, jenaLangFromAcceptHeader(accept))
            .let { ResponseEntity(it, HttpStatus.OK) }
}