package no.digdir.servicecatalog.integration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.digdir.servicecatalog.model.ServiceCount
import no.digdir.servicecatalog.utils.ApiTestContext
import no.digdir.servicecatalog.utils.LIST_OF_SERVICE_COUNTS_ROOT
import no.digdir.servicecatalog.utils.SERVICE_COUNT_1
import no.digdir.servicecatalog.utils.apiAuthorizedRequest
import no.digdir.servicecatalog.utils.jwt.Access
import no.digdir.servicecatalog.utils.jwt.JwtToken
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.ContextConfiguration


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["spring.profiles.active=test"]
)
@ContextConfiguration(initializers = [ApiTestContext.Initializer::class])
@Tag("integration")
class ServiceCountTests : ApiTestContext() {
    private val mapper = jacksonObjectMapper()
    val path = "/internal/catalogs/count"

    @Test
    fun `get service counts when sysAdmin`() {

        val response = apiAuthorizedRequest(
            path,
            port,
            null,
            JwtToken(Access.ROOT).toString(),
            HttpMethod.GET
        )
        Assertions.assertEquals(HttpStatus.OK.value(), response["status"])

        val result: List<ServiceCount> = mapper.readValue(response["body"] as String)
        Assertions.assertEquals(LIST_OF_SERVICE_COUNTS_ROOT, result)
    }

    @Test
    fun `get service counts with read permission`() {
        val response = apiAuthorizedRequest(
            path,
            port,
            null,
            JwtToken(Access.ORG_READ).toString(),
            HttpMethod.GET
        )
        Assertions.assertEquals(HttpStatus.OK.value(), response["status"])

        val result: List<ServiceCount> = mapper.readValue(response["body"] as String)
        Assertions.assertEquals(listOf(SERVICE_COUNT_1), result)
    }

    @Test
    fun `get service counts with write permission`() {
        val response = apiAuthorizedRequest(
            path,
            port,
            null,
            JwtToken(Access.ORG_WRITE).toString(),
            HttpMethod.GET
        )
        Assertions.assertEquals(HttpStatus.OK.value(), response["status"])

        val result: List<ServiceCount> = mapper.readValue(response["body"] as String)
        Assertions.assertEquals(listOf(SERVICE_COUNT_1), result)
    }


}