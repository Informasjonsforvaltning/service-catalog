package no.digdir.servicecatalog.integration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.digdir.servicecatalog.model.JsonPatchOperation
import no.digdir.servicecatalog.model.LocalizedStrings
import no.digdir.servicecatalog.model.OpEnum
import no.digdir.servicecatalog.model.Service
import no.digdir.servicecatalog.utils.ApiTestContext
import no.digdir.servicecatalog.utils.SERVICES
import no.digdir.servicecatalog.utils.SERVICE_1
import no.digdir.servicecatalog.utils.SERVICE_2
import no.digdir.servicecatalog.utils.SERVICE_TO_BE_CREATED
import no.digdir.servicecatalog.utils.apiAuthorizedRequest
import no.digdir.servicecatalog.utils.jwt.Access
import no.digdir.servicecatalog.utils.jwt.JwtToken
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.ContextConfiguration
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = ["spring.profiles.active=test"])
@ContextConfiguration(initializers = [ApiTestContext.Initializer::class])
@Tag("integration")
class Services: ApiTestContext() {
    private val mapper = jacksonObjectMapper()

    @Nested
    internal inner class GetServices {
        val path = "/internal/catalogs/910244132/services"
        @Test
        fun `able to get all services`() {
            val response = apiAuthorizedRequest(
                path,
                port,
                null,
                JwtToken(Access.ORG_READ).toString(),
                HttpMethod.GET)
            Assertions.assertEquals(HttpStatus.OK.value(), response["status"])

            val result: List<Service> = mapper.readValue(response["body"] as String)
            Assertions.assertEquals(SERVICES, result)
        }

        @Test
        fun `unauthorized when missing token`() {
            val response = apiAuthorizedRequest(
                path,
                port,
                null,
                null,
                HttpMethod.GET)
            Assertions.assertEquals(HttpStatus.UNAUTHORIZED.value(), response["status"])
        }

        @Test
        fun `forbidden when authorized for other catalog`() {
            val response = apiAuthorizedRequest(
                path,
                port,
                null,
                JwtToken(Access.WRONG_ORG_WRITE).toString(),
                HttpMethod.GET)
            Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response["status"])
        }
    }

    @Nested
    internal inner class GetService {
        val path = "/internal/catalogs/910244132/services/01"
        @Test
        fun `able to get service by id`() {
            val response = apiAuthorizedRequest(
                path,
                port,
                null,
                JwtToken(Access.ORG_READ).toString(),
                HttpMethod.GET)
            Assertions.assertEquals(HttpStatus.OK.value(), response["status"])
            val result: Service = mapper.readValue(response["body"] as String)
            Assertions.assertEquals(SERVICE_1, result)
        }

        @Test
        fun `unauthorized when missing token`() {
            val response = apiAuthorizedRequest(
                path,
                port,
                null,
                null,
                HttpMethod.GET)
            Assertions.assertEquals(HttpStatus.UNAUTHORIZED.value(), response["status"])
        }

        @Test
        fun `receive not found when service is not found`() {
            val response = apiAuthorizedRequest(
                "/internal/catalogs/910244132/services/1000",
                port,
                null,
                JwtToken(Access.ORG_READ).toString(),
                HttpMethod.GET)
            Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), response["status"])
        }
    }

    @Nested
    internal inner class PatchService {
        val path = "/internal/catalogs/910244132/services/02"
        val replaceOperation = JsonPatchOperation(
            op = OpEnum.REPLACE,
            path = "/title/nb",
            value = "oppdatert tittel")
        @Test
        fun `unauthorized when missing token`() {
            val operations = listOf(replaceOperation)
            val response = apiAuthorizedRequest(
                path,
                port,
                mapper.writeValueAsString(operations),
                null,
                HttpMethod.PATCH)
            Assertions.assertEquals(HttpStatus.UNAUTHORIZED.value(), response["status"])
        }

        @Test
        fun `forbidden when authorized for other catalog`() {
            val operations = listOf(replaceOperation)
            val response = apiAuthorizedRequest(
                path,
                port,
                mapper.writeValueAsString(operations),
                JwtToken(Access.WRONG_ORG_WRITE).toString(),
                HttpMethod.PATCH)
            Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response["status"])
        }

        @Test
        fun `forbidden when authenticated as read user`() {
            val operations = listOf(replaceOperation)
            val response = apiAuthorizedRequest(
                path,
                port,
                mapper.writeValueAsString(operations),
                JwtToken(Access.ORG_READ).toString(),
                HttpMethod.PATCH)
            Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response["status"])
        }

        @Test
        fun `able to replace service nb tittle when authenticated as write user`() {
            val operations = listOf(replaceOperation)
            val response = apiAuthorizedRequest(
                path,
                port,
                mapper.writeValueAsString(operations),
                JwtToken(Access.ORG_WRITE).toString(),
                HttpMethod.PATCH)

            Assertions.assertEquals(HttpStatus.OK.value(), response["status"])

            val result: Service = mapper.readValue(response["body"] as String)
            val expected = SERVICE_2.copy(title = SERVICE_2.title!!.copy(nb = "oppdatert tittel"))

            Assertions.assertEquals(expected, result)
        }

        @Test
        fun `able to remove service title when authenticated as write user`() {
            val operations = listOf(
                JsonPatchOperation(
                op = OpEnum.REMOVE,
                path = "/title/nb")
            )
            val response = apiAuthorizedRequest(
                path,
                port,
                mapper.writeValueAsString(operations),
                JwtToken(Access.ORG_WRITE).toString(),
                HttpMethod.PATCH)
            Assertions.assertEquals(HttpStatus.OK.value(), response["status"])

            val result: Service = mapper.readValue(response["body"] as String)
            val expected = SERVICE_2.copy(title = SERVICE_2.title!!.copy(nb = null))

            Assertions.assertEquals(expected, result)
        }

        @Test
        fun `able to add service description when authenticated as write user`() {
            val operations = listOf(
                JsonPatchOperation(
                op = OpEnum.ADD,
                path = "/description",
                value = LocalizedStrings("added nb description", null, null)
                )
            )
            val response = apiAuthorizedRequest(
                path,
                port,
                mapper.writeValueAsString(operations),
                JwtToken(Access.ORG_WRITE).toString(),
                HttpMethod.PATCH)
            Assertions.assertEquals(HttpStatus.OK.value(), response["status"])

            val result: Service = mapper.readValue(response["body"] as String)
            val expected = SERVICE_2
                .copy(description = LocalizedStrings(nb = "added nb description", null, null))

            Assertions.assertEquals(expected, result)
        }

        @Test
        fun `able to move service nb title to nn when authenticated as write user`() {
            val operations = listOf(
                JsonPatchOperation(
                op = OpEnum.MOVE,
                path = "/title/nn",
                from = "/title/nb")
            )
            val response = apiAuthorizedRequest(
                path,
                port,
                mapper.writeValueAsString(operations),
                JwtToken(Access.ORG_WRITE).toString(),
                HttpMethod.PATCH)
            Assertions.assertEquals(HttpStatus.OK.value(), response["status"])

            val result: Service = mapper.readValue(response["body"] as String)
            val expected = SERVICE_2
                .copy(title = SERVICE_2.title!!.copy(nb = null, nn = "NB Tittel 02"))

            Assertions.assertEquals(expected, result)
        }

        @Test
        fun `able to copy service nb description to nn when authenticated as write user`() {
            val operations = listOf(
                JsonPatchOperation(
                op = OpEnum.COPY,
                path = "/title/nn",
                from = "/title/nb")
            )
            val response = apiAuthorizedRequest(
                path,
                port,
                mapper.writeValueAsString(operations),
                JwtToken(Access.ORG_WRITE).toString(),
                HttpMethod.PATCH)
            Assertions.assertEquals(HttpStatus.OK.value(), response["status"])

            val result: Service = mapper.readValue(response["body"] as String)
            val expected = SERVICE_2
                .copy(title = SERVICE_2.title!!.copy(nb = "NB Tittel 02", nn = "NB Tittel 02"))

            Assertions.assertEquals(expected, result)
        }

        @Test
        fun `not found when service not in database`() {
            val operations = listOf(replaceOperation)
            val response = apiAuthorizedRequest(
                "/internal/catalogs/910244132/services/1000",
                port,
                mapper.writeValueAsString(operations),
                JwtToken(Access.ORG_WRITE).toString(),
                HttpMethod.PATCH)
            Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), response["status"])
        }

        @Test
        fun `not found when service in different catalog`() {
            val operations = listOf(replaceOperation)
            val response = apiAuthorizedRequest(
                "/internal/catalogs/123456789/services/01",
                port,
                mapper.writeValueAsString(operations),
                JwtToken(Access.WRONG_ORG_WRITE).toString(),
                HttpMethod.PATCH)
            Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), response["status"])
        }
    }

    @Nested
    internal inner class DeleteService {
        val path = "/internal/catalogs/910244132/services/02"

        @Test
        fun `unauthorized when missing token` () {
            val response = apiAuthorizedRequest(
                path,
                port,
                null,
                null,
                HttpMethod.DELETE)

            Assertions.assertEquals(HttpStatus.UNAUTHORIZED.value(), response["status"])
        }

        @Test
        fun `forbidden when authenticated as read user` () {
            val response = apiAuthorizedRequest(
                path,
                port,
                null,
                JwtToken(Access.ORG_READ).toString(),
                HttpMethod.DELETE)

            Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response["status"])
        }

        @Test
        fun `not found when service in different catalog`() {
            val response = apiAuthorizedRequest(
                "/internal/catalogs/123456789/services/01",
                port,
                null,
                JwtToken(Access.WRONG_ORG_WRITE).toString(),
                HttpMethod.DELETE)
            Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), response["status"])
        }

        @Test
        fun `not found when service not in database`() {
            val response = apiAuthorizedRequest(
                "/internal/catalogs/910244132/services/1000",
                port,
                null,
                JwtToken(Access.ORG_WRITE).toString(),
                HttpMethod.DELETE)
            Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), response["status"])
        }

        @Test
        fun `able to delete service when authenticated as a write user`() {
            val response = apiAuthorizedRequest(
                path,
                port,
                null,
                JwtToken(Access.ORG_WRITE).toString(),
                HttpMethod.DELETE)
            Assertions.assertEquals(HttpStatus.NO_CONTENT.value(), response["status"])

            val notFoundResponse = apiAuthorizedRequest(
                path,
                port,
                null,
                JwtToken(Access.ORG_READ).toString(),
                HttpMethod.GET)
            Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), notFoundResponse["status"])
        }
    }

    @Nested
    internal inner class CreateService {
        private val path = "/internal/catalogs/910244132/services"

        @Test
        fun `create Service as OrgWrite`() {
            val createResponse = apiAuthorizedRequest(
                path,
                port,
                mapper.writeValueAsString(SERVICE_TO_BE_CREATED),
                JwtToken(Access.ORG_WRITE).toString(),
                HttpMethod.POST
            )
            assertEquals(HttpStatus.CREATED.value(), createResponse["status"])
            val responseHeaders: HttpHeaders = createResponse["header"] as HttpHeaders
            val location = responseHeaders.location
            assertNotNull(location)

            val after = apiAuthorizedRequest(
                location.toString(),
                port,
                null,
                JwtToken(Access.ORG_ADMIN).toString(),
                HttpMethod.GET
            )
            assertEquals(HttpStatus.OK.value(), after["status"])

            val created: Service = mapper.readValue(after["body"] as String)
            assertEquals(SERVICE_TO_BE_CREATED.title, created.title)
        }

        @Test
        fun `create Service as OrgAdmin`() {
            val createResponse = apiAuthorizedRequest(
                path,
                port,
                mapper.writeValueAsString(SERVICE_TO_BE_CREATED),
                JwtToken(Access.ORG_ADMIN).toString(),
                HttpMethod.POST
            )
            assertEquals(HttpStatus.CREATED.value(), createResponse["status"])
        }

        @Test
        fun `create Service as OrgRead forbidden`() {
            val createResponse = apiAuthorizedRequest(
                path,
                port,
                mapper.writeValueAsString(SERVICE_TO_BE_CREATED),
                JwtToken(Access.ORG_READ).toString(),
                HttpMethod.POST
            )
            assertEquals(HttpStatus.FORBIDDEN.value(), createResponse["status"])
        }

        @Test
        fun `create Service as WrongOrg forbidden`() {
            val createResponse = apiAuthorizedRequest(
                path,
                port,
                mapper.writeValueAsString(SERVICE_TO_BE_CREATED),
                JwtToken(Access.WRONG_ORG_WRITE).toString(),
                HttpMethod.POST
            )
            assertEquals(HttpStatus.FORBIDDEN.value(), createResponse["status"])
        }
    }


    @Nested
    internal inner class PublishService {
        val path = "/internal/catalogs/910244132/services/01/publish"
        @Test
        fun `unauthorized when missing token` () {
            val response = apiAuthorizedRequest(
                path,
                port,
                null,
                null,
                HttpMethod.POST)

            Assertions.assertEquals(HttpStatus.UNAUTHORIZED.value(), response["status"])
        }

        @Test
        fun `forbidden when authenticated as read user` () {
            val response = apiAuthorizedRequest(
                path,
                port,
                null,
                JwtToken(Access.ORG_READ).toString(),
                HttpMethod.POST)

            Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response["status"])
        }

        @Test
        fun `not found when service in different catalog`() {
            val response = apiAuthorizedRequest(
                "/internal/catalogs/123456789/services/01/publish",
                port,
                null,
                JwtToken(Access.WRONG_ORG_WRITE).toString(),
                HttpMethod.POST)
            Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), response["status"])
        }

        @Test
        fun `not found when service not in database`() {
            val response = apiAuthorizedRequest(
                "/internal/catalogs/910244132/services/1000/publish",
                port,
                null,
                JwtToken(Access.ORG_WRITE).toString(),
                HttpMethod.POST)
            Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), response["status"])
        }

        @Test
        fun `able to publish service when authenticated as a write user`() {
            val response = apiAuthorizedRequest(
                path,
                port,
                null,
                JwtToken(Access.ORG_WRITE).toString(),
                HttpMethod.POST)
            Assertions.assertEquals(HttpStatus.OK.value(), response["status"])

            val result: Service = mapper.readValue(response["body"] as String)
            val expected = SERVICE_1.copy(published = true)
            Assertions.assertEquals(expected, result)
        }

        @Test
        fun `bad request when updating published with normal patch updates`() {
            val operations = listOf(
                JsonPatchOperation(
                op = OpEnum.REPLACE,
                path = "/published",
                value = true)
            )
            val response = apiAuthorizedRequest(
                "/internal/catalogs/910244132/services/01",
                port,
                mapper.writeValueAsString(operations),
                JwtToken(Access.ORG_WRITE).toString(),
                HttpMethod.PATCH)

            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response["status"])
        }
    }
}
