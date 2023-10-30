package no.digdir.service_catalog.integration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.digdir.service_catalog.model.PublicService
import no.digdir.service_catalog.utils.ApiTestContext
import no.digdir.service_catalog.utils.PUBLIC_SERVICES
import no.digdir.service_catalog.utils.PUBLIC_SERVICE_1
import no.digdir.service_catalog.utils.apiAuthorizedRequest
import no.digdir.service_catalog.utils.jwt.Access
import no.digdir.service_catalog.utils.jwt.JwtToken
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ContextConfiguration

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = ["spring.profiles.active=test"])
@ContextConfiguration(initializers = [ApiTestContext.Initializer::class])
@Tag("integration")
class PublicServices: ApiTestContext() {
    private val mapper = jacksonObjectMapper()

    @Test
    fun `able to get all public services`() {
        val response = apiAuthorizedRequest(
            "/catalogs/910244132/public-services",
            port,
            null,
            JwtToken(Access.ORG_READ).toString(),
            "GET")
        Assertions.assertEquals(HttpStatus.OK.value(), response["status"])

        val result: List<PublicService> = mapper.readValue(response["body"] as String)
        Assertions.assertEquals(PUBLIC_SERVICES, result)
    }

    @Test
    fun `unauthorized when missing token`() {
        val response = apiAuthorizedRequest(
            "/catalogs/910244132/public-services",
            port,
            null,
            null,
            "GET")
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED.value(), response["status"])
    }

    @Test
    fun `forbidden when authorized for other catalog`() {
        val response = apiAuthorizedRequest(
            "/catalogs/910244132/public-services",
            port,
            null,
            JwtToken(Access.WRONG_ORG_READ).toString(),
            "GET")
        Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response["status"])
    }

    @Test
    fun `able to get public service by id`() {
        val response = apiAuthorizedRequest(
            "/catalogs/910244132/public-services/1",
            port,
            null,
            JwtToken(Access.ORG_READ).toString(),
            "GET")
        Assertions.assertEquals(HttpStatus.OK.value(), response["status"])
        val result: PublicService = mapper.readValue(response["body"] as String)
        Assertions.assertEquals(PUBLIC_SERVICE_1, result)
    }

    @Test
    fun `receive not found when public service is not found`() {
        val response = apiAuthorizedRequest(
            "/catalogs/910244132/public-services/1000",
            port,
            null,
            JwtToken(Access.ORG_READ).toString(),
            "GET")
        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), response["status"])
    }
}
