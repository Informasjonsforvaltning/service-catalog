package no.digdir.servicecatalog.service

import no.digdir.servicecatalog.configuration.ApplicationProperties
import no.digdir.servicecatalog.model.ContactPoint
import no.digdir.servicecatalog.model.LocalizedStrings
import no.digdir.servicecatalog.model.Output
import no.digdir.servicecatalog.model.PublicService
import no.digdir.servicecatalog.model.Service
import no.digdir.servicecatalog.model.hasData
import no.digdir.servicecatalog.rdf.*
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.RDFNode
import org.apache.jena.rdf.model.Resource
import org.apache.jena.riot.Lang
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF
import org.apache.jena.vocabulary.VCARD
import org.apache.jena.vocabulary.VCARD4

@org.springframework.stereotype.Service
class RDFService(
    private val applicationProperties: ApplicationProperties,
    private val publicServiceService: PublicServiceService,
    private val serviceService: ServiceService) {

    fun serializeCatalog(catalogId: String, lang: Lang): String {
        val model = ModelFactory.createDefaultModel()
        model.setDefaultPrefixes()

        val catalog = model.createResource(catalogURI(catalogId))
            .addProperty(RDF.type, DCAT.Catalog)
            .addProperty(DCTerms.title, "Catalog for $catalogId")
            .addProperty(DCTerms.publisher, model.createResource(publisherURI(catalogId)))

        publicServiceService.publishedServicesInCatalog(catalogId).forEach {
            catalog.addPublicServiceToCatalog(it)
        }

        serviceService.publishedServicesInCatalog(catalogId).forEach {
            catalog.addServiceToCatalog(it)
        }

        return model.serialize(lang)
    }

    fun serializeService(catalogId: String, id: String, lang: Lang): String? =
        with(serviceService.getPublishedServiceInCatalog(id, catalogId)) {
            val model = ModelFactory.createDefaultModel()
            model.setDefaultPrefixes()
            model.createServiceResource(this, catalogURI(catalogId))
            model.serialize(lang)
        }

    fun serializePublicService(catalogId: String, id: String, lang: Lang): String? =
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
        setNsPrefix("vcard", VCARD4.getURI())
        setNsPrefix("foaf", FOAF.getURI())
        setNsPrefix("adms", ADMS.uri)
    }

    private fun Resource.addServiceToCatalog(service: Service): Resource {
        val serviceResource = model.createServiceResource(service, uri)
        addProperty(DCATNO.containsService, serviceResource)
        return this
    }

    private fun Resource.addPublicServiceToCatalog(publicService: PublicService): Resource {
        val publicServiceResource = model.createPublicServiceResource(publicService, uri)
        addProperty(DCATNO.containsService, publicServiceResource)
        return this
    }

    private fun Resource.addProducesOutput(outputs: List<Output>?): Resource {
        outputs?.filter { it.isValid() }
            ?.forEach { output ->
                val identifierURI = "$uri/output/${output.identifier}"
                val outputResource = model.createResource(identifierURI)
                    .addProperty(RDF.type, CV.Output)
                    .addLocalizedStringsAsProperty(DCTerms.title, output.title)
                    .addLocalizedStringsAsProperty(DCTerms.description, output.description)
                    .addStringsAsResources(DCTerms.language, output.language)

                addProperty(CPSV.produces, outputResource)
            }
        return this
    }

    private fun Resource.addSpatial(resources: List<String>?): Resource {
        resources?.forEach {
            addAsResourceIfValid(DCTerms.spatial, it)
        }
        return this
    }

    private fun Output.isValid(): Boolean =
        title != null && title.hasData()

    private fun Resource.addContactPoints(contactPoints: List<ContactPoint>?): Resource {
        contactPoints?.filter { it.isValid() }
            ?.forEach { contactPoint ->
                val contactResource = model.createResource()
                    .addProperty(RDF.type, CV.ContactPoint)
                    .addLocalizedStringsAsProperty(VCARD4.category, contactPoint.category)
                    .addStringsAsResources(VCARD4.language, contactPoint.language)
                    .addPropertyIfExists(CV.contactPage, contactPoint.contactPage)
                    .addPropertyIfExists(CV.telephone, contactPoint.telephone)
                    .addPropertyIfExists(CV.email, contactPoint.email)
                addProperty(CV.contactPoint, contactResource)
            }
        return this
    }

    private fun ContactPoint.isValid(): Boolean =
        when {
            !contactPage.isNullOrBlank() -> true
            !email.isNullOrBlank() -> true
            !telephone.isNullOrBlank() -> true
            else -> false
        }

    private fun publisherURI(catalogId: String): String =
        "https://data.brreg.no/enhetsregisteret/api/enheter/$catalogId"

    private fun catalogURI(catalogId: String): String =
        "${applicationProperties.serviceCatalogUri}/rdf/catalogs/$catalogId"

    private fun serviceURI(id: String, catalogURI: String): String =
        "${catalogURI}/services/$id"

    private fun publicServiceURI(id: String, catalogURI: String): String =
        "${catalogURI}/public-services/$id"

    private fun Model.createPublicServiceResource(publicService: PublicService, catalogUri: String): Resource {
        val publicServiceResource = createResource(publicServiceURI(publicService.id, catalogUri))
            .addProperty(RDF.type, CPSV.PublicService)
            .addProperty(CV.hasCompetentAuthority, createResource(publisherURI(publicService.catalogId)))
            .addLocalizedStringsAsProperty(DCTerms.title, publicService.title)
            .addLocalizedStringsAsProperty(DCTerms.description, publicService.description)
            .addProducesOutput(publicService.produces)
            .addContactPoints(publicService.contactPoints)
            .addPropertyIfExists(FOAF.homepage, publicService.homepage)
            .addAsResourceIfValid(ADMS.status, publicService.status)
            .addSpatial(publicService.spatial)

        publicServiceResource.addProperty(DCTerms.identifier, publicServiceResource)
        return publicServiceResource
    }

    private fun Model.createServiceResource(service: Service, catalogUri: String): Resource {
        val serviceResource = createResource(serviceURI(service.id, catalogUri))
            .addProperty(RDF.type, CPSVNO.Service)
            .addProperty(CV.ownedBy, createResource(publisherURI(service.catalogId)))
            .addLocalizedStringsAsProperty(DCTerms.title, service.title)
            .addLocalizedStringsAsProperty(DCTerms.description, service.description)
            .addProducesOutput(service.produces)
            .addContactPoints(service.contactPoints)
            .addPropertyIfExists(FOAF.homepage, service.homepage)
            .addAsResourceIfValid(ADMS.status, service.status)

        serviceResource.addProperty(DCTerms.identifier, serviceResource)
        return serviceResource
    }
}
