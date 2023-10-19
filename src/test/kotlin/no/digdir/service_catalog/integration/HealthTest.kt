package no.digdir.service_catalog.integration

import no.digdir.service_catalog.utils.ApiTestContext
import no.digdir.service_catalog.utils.apiGet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("integration")
class HealthTest: ApiTestContext() {
    @Test
    fun ping() {
        val response = apiGet(port, "/actuator/health/liveness", null)

        assertEquals(HttpStatus.OK.value(), response["status"])
    }

    @Test
    fun ready() {
        val response = apiGet(port, "/actuator/health/readiness", null)

        assertEquals(HttpStatus.OK.value(), response["status"])
    }

}
