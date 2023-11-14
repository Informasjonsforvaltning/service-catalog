package no.digdir.servicecatalog.rdf

import no.digdir.servicecatalog.model.LocalizedStrings
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.Resource
import org.apache.jena.riot.Lang
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.net.URI

fun Model.serialize(lang: Lang): String =
    ByteArrayOutputStream().use { out ->
        write(out, lang.name)
        out.flush()
        out.toString("UTF-8")
    }

fun Resource.addLocalizedStringsAsProperty(property: Property, strings: LocalizedStrings?): Resource {
    if (strings?.nb != null) addProperty(property, strings.nb, "nb")
    if (strings?.nn != null) addProperty(property, strings.nn, "nn")
    if (strings?.en != null) addProperty(property, strings.en, "en")
    return this
}

fun jenaLangFromAcceptHeader(accept: String?): Lang =
    when {
        accept == null -> Lang.TURTLE
        accept.contains(Lang.TURTLE.headerString) -> Lang.TURTLE
        accept.contains(Lang.RDFXML.headerString) -> Lang.RDFXML
        accept.contains(Lang.RDFJSON.headerString) -> Lang.RDFJSON
        accept.contains(Lang.NTRIPLES.headerString) -> Lang.NTRIPLES
        accept.contains(Lang.NQUADS.headerString) -> Lang.NQUADS
        accept.contains(Lang.TRIG.headerString) -> Lang.TRIG
        accept.contains(Lang.TRIX.headerString) -> Lang.TRIX
        accept.contains("text/n3") -> Lang.N3
        accept.contains("*/*") -> Lang.TURTLE
        else -> throw ResponseStatusException(HttpStatus.NOT_ACCEPTABLE)
    }

fun Model.safeCreateResource(value: String? = null): Resource =
    try {
        value
            ?.let(::URI)
            ?.takeIf { it.isAbsolute && !it.isOpaque && !it.host.isNullOrEmpty() }
            ?.let { createResource(value) }
            ?: createResource()
    } catch (e: Exception) {
        createResource()
    }