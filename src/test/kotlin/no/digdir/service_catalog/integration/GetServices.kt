package no.digdir.service_catalog.integration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.digdir.service_catalog.model.Service
import no.digdir.service_catalog.utils.ApiTestContext
import no.digdir.service_catalog.utils.SERVICES
import no.digdir.service_catalog.utils.apiAuthorizedRequest
import no.digdir.service_catalog.utils.apiGet
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
class GetServices: ApiTestContext() {
    private val mapper = jacksonObjectMapper()

    @Test
    fun `able to get all services`() {
        val response = apiAuthorizedRequest("/catalogs/910244132/services", port, null, JwtToken(Access.ORG_READ).toString(), "GET")
        Assertions.assertEquals(HttpStatus.OK.value(), response["status"])

        val result: List<Service> = mapper.readValue(response["body"] as String)
        Assertions.assertEquals(SERVICES, result)
    }

    @Test
    fun `unauthorized when missing token`() {
        val response = apiAuthorizedRequest("/catalogs/910244132/services", port, null, null, "GET")
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED.value(), response["status"])
    }

    @Test
    fun `forbidden when authorized for other catalog`() {
        val response = apiAuthorizedRequest("/catalogs/910244132/services", port, null, JwtToken(Access.WRONG_ORG_READ).toString(), "GET")
        Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response["status"])
    }
}
