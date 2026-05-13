package no.digdir.servicecatalog.service

import no.digdir.servicecatalog.configuration.ApplicationProperties
import no.digdir.servicecatalog.domain.ContactPoint
import no.digdir.servicecatalog.domain.Evidence
import no.digdir.servicecatalog.domain.Output
import no.digdir.servicecatalog.domain.hasData
import no.digdir.servicecatalog.dto.PublicServiceDTO
import no.digdir.servicecatalog.dto.ServiceDTO
import no.digdir.servicecatalog.rdf.*
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.Resource
import org.apache.jena.riot.Lang
import org.apache.jena.sparql.vocabulary.FOAF
import org.apache.jena.vocabulary.DCAT
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF
import org.apache.jena.vocabulary.VCARD4
import org.springframework.stereotype.Service

@Service
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

    fun serializeService(catalogId: String, id: String, lang: Lang): String =
        with(serviceService.getPublishedServiceInCatalog(id, catalogId)) {
            val model = ModelFactory.createDefaultModel()
            model.setDefaultPrefixes()
            model.createServiceResource(this, catalogURI(catalogId))
            model.serialize(lang)
        }

    fun serializePublicService(catalogId: String, id: String, lang: Lang): String =
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

    private fun Resource.addServiceToCatalog(service: ServiceDTO): Resource {
        val serviceResource = model.createServiceResource(service, uri)
        addProperty(DCATNO.containsService, serviceResource)
        return this
    }

    private fun Resource.addPublicServiceToCatalog(publicService: PublicServiceDTO): Resource {
        val publicServiceResource = model.createPublicServiceResource(publicService, uri)
        addProperty(DCATNO.containsService, publicServiceResource)
        return this
    }

    private fun Resource.addProducesOutput(outputs: List<Output>?): Resource {
        outputs?.filter { it.isValid() }
            ?.forEach { output ->
                val outputResource = if (output.identifier != null) {
                    val identifierURI = "$uri/output/${output.identifier}"
                    model.createResource(identifierURI)
                } else {
                    model.createResource()
                }

                outputResource
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

    private fun Resource.addSubject(subject: Set<String>?): Resource {
        subject?.forEach {
            addAsResourceIfValid(DCTerms.subject, it)
        }
        return this
    }

    private fun Resource.addDctType(resources: Set<String>?): Resource {
        resources?.forEach {
            addAsResourceIfValid(DCTerms.type, it)
        }
        return this
    }

    private fun Resource.addThematicArea(thematicArea: Set<String>?): Resource {
        thematicArea?.forEach {
            addAsResourceIfValid(CV.thematicArea, it)
        }
        return this
    }

    private fun Resource.addRequiredEvidence(evidenceList: List<Evidence>?): Resource {
        evidenceList?.filter { it.isValid() }
            ?.forEach { evidence ->
                val evidenceResource = if ( evidence.identifier != null) {
                    val evidenceURI = "$uri/evidence/${evidence.identifier}"
                    model.createResource(evidenceURI)
                        .addProperty(DCTerms.identifier, model.createResource(evidenceURI))
                } else {
                    model.createResource()
                }

                evidenceResource
                    .addProperty(RDF.type, CPSVNO.RequiredEvidence)
                    .addLocalizedStringsAsProperty(DCTerms.title, evidence.title)
                    .addLocalizedStringsAsProperty(DCTerms.description, evidence.description)
                    .addStringsAsResources(DCTerms.language, evidence.language)
                    .addStringsAsResources(FOAF.page, evidence.relatedDocumentation)
                    .addStringsAsResources(DCTerms.isPartOf, evidence.dataset)

                addProperty(CPSVNO.hasRequiredEvidence, evidenceResource)
            }
        return this
    }

    private fun Output.isValid(): Boolean =
        title != null && title.hasData()

    private fun Evidence.isValid(): Boolean =
        title != null && title.hasData()

    private fun Resource.addContactPoints(contactPoints: List<ContactPoint>?): Resource {
        contactPoints?.filter { it.isValid() }
            ?.forEach { contactPoint ->
                val contactResource = model.createResource()
                    .addProperty(RDF.type, CV.ContactPoint)
                    .addLocalizedStringsAsProperty(VCARD4.category, contactPoint.category)
                    .addStringsAsResources(VCARD4.language, contactPoint.language)
                    .addPropertyIfExists(CV.contactPage, contactPoint.contactPage)
                    .addPropertyIfExists(CV.telephone, contactPoint.telephone?.replace(Regex("\\s"), ""))
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

    private fun Model.createPublicServiceResource(publicService: PublicServiceDTO, catalogUri: String): Resource {
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
            .addSubject(publicService.subject)
            .addDctType(publicService.dctType)
            .addThematicArea(publicService.losTheme)
            .addRequiredEvidence(publicService.evidence)
            .addCosts(publicService.costs)

        publicServiceResource.addProperty(DCTerms.identifier, publicServiceResource)
        return publicServiceResource
    }

    private fun Model.createServiceResource(service: ServiceDTO, catalogUri: String): Resource {
        val serviceResource = createResource(serviceURI(service.id, catalogUri))
            .addProperty(RDF.type, CPSVNO.Service)
            .addProperty(CV.ownedBy, createResource(publisherURI(service.catalogId)))
            .addLocalizedStringsAsProperty(DCTerms.title, service.title)
            .addLocalizedStringsAsProperty(DCTerms.description, service.description)
            .addProducesOutput(service.produces)
            .addContactPoints(service.contactPoints)
            .addPropertyIfExists(FOAF.homepage, service.homepage)
            .addAsResourceIfValid(ADMS.status, service.status)
            .addSpatial(service.spatial)
            .addSubject(service.subject)
            .addThematicArea(service.losTheme)
            .addRequiredEvidence(service.evidence)
            .addCosts(service.costs)

        serviceResource.addProperty(DCTerms.identifier, serviceResource)
        return serviceResource
    }
}
