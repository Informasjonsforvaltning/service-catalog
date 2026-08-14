package no.digdir.servicecatalog.rdf

import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdf.model.ResourceFactory

class DCATNO {
    companion object {
        const val URI = "https://data.norge.no/vocabulary/dcatno#"

        val containsService: Property = ResourceFactory.createProperty("${URI}containsService")
    }
}

class CPSV {
    companion object {
        const val URI = "http://purl.org/vocab/cpsv#"

        val produces: Property = ResourceFactory.createProperty("${URI}produces")

        val PublicService: Resource = ResourceFactory.createResource("${URI}PublicService")
    }
}

class CPSVNO {
    companion object {
        const val URI = "https://data.norge.no/vocabulary/cpsvno#"

        val hasRequiredEvidence: Property = ResourceFactory.createProperty("${URI}hasRequiredEvidence")

        val Service: Resource = ResourceFactory.createResource("${URI}Service")
        val RequiredEvidence: Resource = ResourceFactory.createResource("${URI}RequiredEvidence")
    }
}

class CV {
    companion object {
        const val URI = "http://data.europa.eu/m8g/"

        val hasCompetentAuthority: Property = ResourceFactory.createProperty("${URI}hasCompetentAuthority")
        val ownedBy: Property = ResourceFactory.createProperty("${URI}ownedBy")
        val contactPage: Property = ResourceFactory.createProperty("${URI}contactPage")
        val telephone: Property = ResourceFactory.createProperty("${URI}telephone")
        val email: Property = ResourceFactory.createProperty("${URI}email")
        val contactPoint: Property = ResourceFactory.createProperty("${URI}contactPoint")
        val thematicArea: Property = ResourceFactory.createProperty("${URI}thematicArea")
        val currency: Property = ResourceFactory.createProperty("${URI}currency")
        val hasCost: Property = ResourceFactory.createProperty("${URI}hasCost")
        val hasValue: Property = ResourceFactory.createProperty("${URI}hasValue")

        val Output: Resource = ResourceFactory.createResource("${URI}Output")
        val ContactPoint: Resource = ResourceFactory.createResource("${URI}ContactPoint")
        val Cost: Resource = ResourceFactory.createResource("${URI}Cost")
    }
}

class ADMS {
    companion object {
        const val URI = "http://www.w3.org/ns/adms#"

        val status: Property = ResourceFactory.createProperty("${URI}status")
    }
}
