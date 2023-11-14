package no.digdir.servicecatalog.rdf

import org.apache.jena.rdf.model.ResourceFactory

class DCATNO {
    companion object {
        const val uri = "https://data.norge.no/vocabulary/dcatno#"

        val containsService = ResourceFactory.createProperty("${uri}containsService")
    }
}

class CPSV {
    companion object {
        const val uri = "http://purl.org/vocab/cpsv#"

        val PublicService = ResourceFactory.createResource("${uri}PublicService")
    }
}

class CV {
    companion object {
        const val uri = "http://data.europa.eu/m8g/"

        val hasCompetentAuthority = ResourceFactory.createProperty("${uri}hasCompetentAuthority")
    }
}
