package no.digdir.servicecatalog.integration

import no.digdir.servicecatalog.utils.ApiTestContext
import no.digdir.servicecatalog.utils.TestResponseReader
import no.digdir.servicecatalog.utils.apiAuthorizedRequest
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.riot.Lang
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.util.MimeType
import java.io.StringReader

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["spring.profiles.active=test"])
@ContextConfiguration(initializers = [ApiTestContext.Initializer::class])
@Tag("integration")
class RDF: ApiTestContext() {
    val responseReader = TestResponseReader()

    @Nested
    internal inner class GetCatalogRDF {
        private val catalogPath = "/rdf/catalogs/910244132"

        @Test
        fun `able to get rdf catalog`() {
            val response = apiAuthorizedRequest(
                catalogPath,
                port,
                null,
                null,
                HttpMethod.GET,
                MediaType.asMediaType(MimeType.valueOf(Lang.TURTLE.headerString))
            )
            Assertions.assertEquals(HttpStatus.OK.value(), response["status"])

            val expected = responseReader.parseFile("service_catalog.ttl", Lang.TURTLE.name)
            val result = ModelFactory
                .createDefaultModel()
                .read(StringReader(response["body"] as String), null, Lang.TURTLE.name)
            Assertions.assertTrue(expected.isIsomorphicWith(result))
        }

        @Test
        fun `able to get rdf catalog with media type xml`() {
            val response = apiAuthorizedRequest(
                catalogPath,
                port,
                null,
                null,
                HttpMethod.GET,
                MediaType.asMediaType(MimeType.valueOf(Lang.RDFXML.headerString))
            )
            Assertions.assertEquals(HttpStatus.OK.value(), response["status"])

            val expected = responseReader.parseFile("service_catalog.ttl", Lang.TURTLE.name)
            val result = ModelFactory
                .createDefaultModel()
                .read(StringReader(response["body"] as String), null, Lang.RDFXML.name)
            Assertions.assertTrue(expected.isIsomorphicWith(result))
        }

        @Test
        fun `not acceptable for media type jason`() {
            val response = apiAuthorizedRequest(
                catalogPath,
                port,
                null,
                null,
                HttpMethod.GET,
                MediaType.APPLICATION_JSON
            )
            Assertions.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response["status"])
        }
    }

    @Nested
    internal inner class GetPublicServiceRDF {
        private val publicServicePath = "/rdf/catalogs/910244132/public-services/0"
        private val notExistingPublicServicePath = "/rdf/catalogs/910244132/public-services/1000"

        @Test
        fun `able to get rdf for public service`() {
            val response = apiAuthorizedRequest(
                publicServicePath,
                port,
                null,
                null,
                HttpMethod.GET,
                MediaType.asMediaType(MimeType.valueOf(Lang.TURTLE.headerString)))
            Assertions.assertEquals(HttpStatus.OK.value(), response["status"])

            val expected = responseReader.parseFile("public_service.ttl", Lang.TURTLE.name)
            val result = ModelFactory.createDefaultModel().read(StringReader(response["body"] as String), null, Lang.TURTLE.name)
            Assertions.assertTrue(expected.isIsomorphicWith(result))
        }

        @Test
        fun `not found when public service not in database`() {
            val response = apiAuthorizedRequest(
                notExistingPublicServicePath,
                port,
                null,
                null,
                HttpMethod.GET,
                MediaType.asMediaType(MimeType.valueOf(Lang.TURTLE.headerString)))
            Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), response["status"])
        }

        @Test
        fun `not acceptable for media type jason`() {
            val response = apiAuthorizedRequest(
                publicServicePath,
                port,
                null,
                null,
                HttpMethod.GET,
                MediaType.APPLICATION_JSON
            )
            Assertions.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response["status"])
        }

        @Test
        fun `able to get rdf for public service with media type xml`() {
            val response = apiAuthorizedRequest(
                publicServicePath,
                port,
                null,
                null,
                HttpMethod.GET,
                MediaType.asMediaType(MimeType.valueOf(Lang.RDFXML.headerString))
            )
            Assertions.assertEquals(HttpStatus.OK.value(), response["status"])

            val expected = responseReader.parseFile("public_service.ttl", Lang.TURTLE.name)
            val result = ModelFactory
                .createDefaultModel()
                .read(StringReader(response["body"] as String), null, Lang.RDFXML.name)
            Assertions.assertTrue(expected.isIsomorphicWith(result))
        }
    }
}
