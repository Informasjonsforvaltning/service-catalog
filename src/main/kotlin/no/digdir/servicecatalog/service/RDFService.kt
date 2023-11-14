package no.digdir.servicecatalog.service

import no.digdir.servicecatalog.configuration.ApplicationProperties
import no.digdir.servicecatalog.model.PublicService
import no.digdir.servicecatalog.rdf.CPSV
import no.digdir.servicecatalog.rdf.CV
import no.digdir.servicecatalog.rdf.DCATNO
import no.digdir.servicecatalog.rdf.addLocalizedStringsAsProperty
import no.digdir.servicecatalog.rdf.serialize
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.Resource
import org.apache.jena.riot.Lang
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF
import org.springframework.stereotype.Service

@Service
class RDFService(
    private val applicationProperties: ApplicationProperties,
    private val publicServiceService: PublicServiceService) {

    fun serializeCatalog(catalogId: String, lang: Lang): String {
        val model = ModelFactory.createDefaultModel()
        model.setDefaultPrefixes()

        val catalog = model.createResource(catalogURI(catalogId))
            .addProperty(RDF.type, DCAT.Catalog)
            .addProperty(DCTerms.title, "Catalog for $catalogId")
            .addProperty(DCTerms.publisher, model.createResource(publisherURI(catalogId)))

        publicServiceService.publishedServicesInCatalog(catalogId).forEach {
            catalog.addPublicService(it)
        }

        return model.serialize(lang)
    }

    fun serializeService(catalogId: String, id: String, lang: Lang): String? =
        with(publicServiceService.getPublishedPublicServiceInCatalog(id, catalogId)) {
            val model = ModelFactory.createDefaultModel()
            model.setDefaultPrefixes()
            model.createPublicServiceResource(this, catalogURI(catalogId))
            model.serialize(lang)
        }

    private fun Model.setDefaultPrefixes() {
        setNsPrefix("dct", DCTerms.NS)
        setNsPrefix("dcat", DCAT.NS)
        setNsPrefix("dcatno", DCATNO.uri)
        setNsPrefix("cpsv", CPSV.uri)
        setNsPrefix("cv", CV.uri)
    }

    private fun Resource.addPublicService(publicService: PublicService): Resource {
        val publicServiceResource = model.createPublicServiceResource(publicService, uri)
        addProperty(DCATNO.containsService, publicServiceResource)
        return this
    }

    private fun publisherURI(catalogId: String): String =
        "https://data.brreg.no/enhetsregisteret/api/enheter/$catalogId"

    private fun catalogURI(catalogId: String): String =
        "${applicationProperties.serviceCatalogUri}/rdf/catalogs/$catalogId"

    private fun publicServiceURI(id: String, catalogURI: String): String =
        "${catalogURI}/public-services/$id"

    private fun Model.createPublicServiceResource(publicService: PublicService, catalogUri: String): Resource {
        val publicServiceResource = createResource(publicServiceURI(publicService.id, catalogUri))
            .addProperty(RDF.type, CPSV.PublicService)
            .addProperty(CV.hasCompetentAuthority, createResource(publisherURI(publicService.catalogId)))
            .addLocalizedStringsAsProperty(DCTerms.title, publicService.title)
            .addLocalizedStringsAsProperty(DCTerms.description, publicService.description)

        publicServiceResource.addProperty(DCTerms.identifier, publicServiceResource)
        return publicServiceResource
    }
}
