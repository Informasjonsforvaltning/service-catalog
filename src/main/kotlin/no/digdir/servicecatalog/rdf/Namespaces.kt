package no.digdir.servicecatalog.rdf

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.ResourceFactory

class DCATNO {
    companion object {
        const val uri = "https://data.norge.no/vocabulary/dcatno#"

        val containsService: Property = ResourceFactory.createProperty("${uri}containsService")
    }
}

class CPSV {
    companion object {
        const val uri = "http://purl.org/vocab/cpsv#"

        val produces: Property = ResourceFactory.createProperty("${uri}produces")

        val PublicService: Resource = ResourceFactory.createResource("${uri}PublicService")
    }
}

class CPSVNO {
    companion object {
        const val uri = "https://data.norge.no/vocabulary/cpsvno#"

        val Service: Resource = ResourceFactory.createResource("${uri}Service")
    }
}

class CV {
    companion object {
        const val uri = "http://data.europa.eu/m8g/"

        val hasCompetentAuthority: Property = ResourceFactory.createProperty("${uri}hasCompetentAuthority")
        val ownedBy: Property = ResourceFactory.createProperty("${uri}ownedBy")
        val contactPage: Property = ResourceFactory.createProperty("${uri}contactPage")
        val telephone: Property = ResourceFactory.createProperty("${uri}telephone")
        val email: Property = ResourceFactory.createProperty("${uri}email")
        val contactPoint: Property = ResourceFactory.createProperty("${uri}contactPoint")

        val Output: Resource = ResourceFactory.createResource("${uri}Output")
        val ContactPoint: Resource = ResourceFactory.createResource("${uri}ContactPoint")
    }
}
