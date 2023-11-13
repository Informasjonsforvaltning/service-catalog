package no.digdir.service_catalog.integration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.digdir.service_catalog.model.JsonPatchOperation
import no.digdir.service_catalog.model.LocalizedStrings
import no.digdir.service_catalog.model.OpEnum
import no.digdir.service_catalog.model.PublicService
import no.digdir.service_catalog.utils.*
import no.digdir.service_catalog.utils.jwt.Access
import no.digdir.service_catalog.utils.jwt.JwtToken
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.ContextConfiguration
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = ["spring.profiles.active=test"])
@ContextConfiguration(initializers = [ApiTestContext.Initializer::class])
@Tag("integration")
class PublicServices: ApiTestContext() {
    private val mapper = jacksonObjectMapper()

    @Nested
    internal inner class GetPublicServices {
        @Test
        fun `able to get all public services`() {
            val response = apiAuthorizedRequest(
                "/catalogs/910244132/public-services",
                port,
                null,
                JwtToken(Access.ORG_READ).toString(),
                HttpMethod.GET)
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
                HttpMethod.GET)
            Assertions.assertEquals(HttpStatus.UNAUTHORIZED.value(), response["status"])
        }

        @Test
        fun `forbidden when authorized for other catalog`() {
            val response = apiAuthorizedRequest(
                "/catalogs/910244132/public-services",
                port,
                null,
                JwtToken(Access.WRONG_ORG_WRITE).toString(),
                HttpMethod.GET)
            Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response["status"])
        }
    }

    @Nested
    internal inner class GetPublicService {
        @Test
        fun `able to get public service by id`() {
            val response = apiAuthorizedRequest(
                "/catalogs/910244132/public-services/1",
                port,
                null,
                JwtToken(Access.ORG_READ).toString(),
                HttpMethod.GET)
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
                HttpMethod.GET)
            Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), response["status"])
        }
    }

    @Nested
    internal inner class PatchPublicService {
        val pathService1 = "/catalogs/910244132/public-services/1"
        val replaceOperation = JsonPatchOperation(
            op = OpEnum.REPLACE,
            path = "/title/nb",
            value = "oppdater tittel")
        @Test
        fun `unauthorized when missing token`() {
            val operations = listOf(replaceOperation)
            val response = apiAuthorizedRequest(
                pathService1,
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
                pathService1,
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
                pathService1,
                port,
                mapper.writeValueAsString(operations),
                JwtToken(Access.ORG_READ).toString(),
                HttpMethod.PATCH)
            Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response["status"])
        }

        @Test
        fun `able to replace public service nb tittle when authenticated as write user`() {
            val operations = listOf(replaceOperation)
            val response = apiAuthorizedRequest(
                pathService1,
                port,
                mapper.writeValueAsString(operations),
                JwtToken(Access.ORG_WRITE).toString(),
                HttpMethod.PATCH)

            Assertions.assertEquals(HttpStatus.OK.value(), response["status"])

            val result: PublicService = mapper.readValue(response["body"] as String)
            val expected = PUBLIC_SERVICE_1.copy(title = PUBLIC_SERVICE_1.title!!.copy(nb = "oppdater tittel"))

            Assertions.assertEquals(expected, result)
        }

        @Test
        fun `able to remove public service title when authenticated as write user`() {
            val operations = listOf(JsonPatchOperation(
                op = OpEnum.REMOVE,
                path = "/title/nb"))
            val response = apiAuthorizedRequest(
                pathService1,
                port,
                mapper.writeValueAsString(operations),
                JwtToken(Access.ORG_WRITE).toString(),
                HttpMethod.PATCH)
            Assertions.assertEquals(HttpStatus.OK.value(), response["status"])

            val result: PublicService = mapper.readValue(response["body"] as String)
            val expected = PUBLIC_SERVICE_1.copy(title = PUBLIC_SERVICE_1.title!!.copy(nb = null))

            Assertions.assertEquals(expected, result)
        }

        @Test
        fun `able to add public service description when authenticated as write user`() {
            val operations = listOf(JsonPatchOperation(
                op = OpEnum.ADD,
                path = "/description",
                value = LocalizedStrings("added nb description", null, null)))
            val response = apiAuthorizedRequest(
                "/catalogs/910244132/public-services/2",
                port,
                mapper.writeValueAsString(operations),
                JwtToken(Access.ORG_WRITE).toString(),
                HttpMethod.PATCH)
            Assertions.assertEquals(HttpStatus.OK.value(), response["status"])

            val result: PublicService = mapper.readValue(response["body"] as String)
            val expected = PUBLIC_SERVICE_2
                .copy(description = LocalizedStrings(nb = "added nb description", null, null))

            Assertions.assertEquals(expected, result)
        }

        @Test
        fun `able to move public service nb title to nn when authenticated as write user`() {
            val operations = listOf(JsonPatchOperation(
                op = OpEnum.MOVE,
                path = "/title/nn",
                from = "/title/nb"))
            val response = apiAuthorizedRequest(
                pathService1,
                port,
                mapper.writeValueAsString(operations),
                JwtToken(Access.ORG_WRITE).toString(),
                HttpMethod.PATCH)
            Assertions.assertEquals(HttpStatus.OK.value(), response["status"])

            val result: PublicService = mapper.readValue(response["body"] as String)
            val expected = PUBLIC_SERVICE_1
                .copy(title = PUBLIC_SERVICE_1.title!!.copy(null, nn = "NB Tittel 1"))

            Assertions.assertEquals(expected, result)
        }

        @Test
        fun `able to copy public service nb description to nn when authenticated as write user`() {
            val operations = listOf(JsonPatchOperation(
                op = OpEnum.COPY,
                path = "/title/nn",
                from = "/title/nb"))
            val response = apiAuthorizedRequest(
                pathService1,
                port,
                mapper.writeValueAsString(operations),
                JwtToken(Access.ORG_WRITE).toString(),
                HttpMethod.PATCH)
            Assertions.assertEquals(HttpStatus.OK.value(), response["status"])

            val result: PublicService = mapper.readValue(response["body"] as String)
            val expected = PUBLIC_SERVICE_1
                .copy(title = PUBLIC_SERVICE_1.title!!.copy(nb = "NB Tittel 1", nn = "NB Tittel 1"))

            Assertions.assertEquals(expected, result)
        }

        @Test
        fun `not found when public service not in database`() {
            val operations = listOf(replaceOperation)
            val response = apiAuthorizedRequest(
                "/catalogs/910244132/public-services/1000",
                port,
                mapper.writeValueAsString(operations),
                JwtToken(Access.ORG_WRITE).toString(),
                HttpMethod.PATCH)
            Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), response["status"])
        }

        @Test
        fun `not found when public service in different catalog`() {
            val operations = listOf(replaceOperation)
            val response = apiAuthorizedRequest(
                "/catalogs/123456789/public-services/1",
                port,
                mapper.writeValueAsString(operations),
                JwtToken(Access.WRONG_ORG_WRITE).toString(),
                HttpMethod.PATCH)
            Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), response["status"])
        }
    }

    @Nested
    internal inner class DeletePublicService {
        val pathService1 = "/catalogs/910244132/public-services/1"

        @Test
        fun `unauthorized when missing token` () {
            val response = apiAuthorizedRequest(
                pathService1,
                port,
                null,
                null,
                HttpMethod.DELETE)

            Assertions.assertEquals(HttpStatus.UNAUTHORIZED.value(), response["status"])
        }

        @Test
        fun `forbidden when authenticated as read user` () {
            val response = apiAuthorizedRequest(
                pathService1,
                port,
                null,
                JwtToken(Access.ORG_READ).toString(),
                HttpMethod.DELETE)

            Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response["status"])
        }

        @Test
        fun `not found when public service in different catalog`() {
            val response = apiAuthorizedRequest(
                "/catalogs/123456789/public-services/1",
                port,
                null,
                JwtToken(Access.WRONG_ORG_WRITE).toString(),
                HttpMethod.DELETE)
            Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), response["status"])
        }

        @Test
        fun `not found when public service not in database`() {
            val response = apiAuthorizedRequest(
                "/catalogs/910244132/public-services/1000",
                port,
                null,
                JwtToken(Access.ORG_WRITE).toString(),
                HttpMethod.DELETE)
            Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), response["status"])
        }

        @Test
        fun `able to delete public service when authenticated as a write user`() {
            val response = apiAuthorizedRequest(
                pathService1,
                port,
                null,
                JwtToken(Access.ORG_WRITE).toString(),
                HttpMethod.DELETE)
            Assertions.assertEquals(HttpStatus.NO_CONTENT.value(), response["status"])

            val notFoundResponse = apiAuthorizedRequest(
                pathService1,
                port,
                null,
                JwtToken(Access.ORG_READ).toString(),
                HttpMethod.GET)
            Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), notFoundResponse["status"])
        }
    }

    @Nested
    internal inner class CreatePublicService {
        private val path = "/catalogs/910244132/public-services"

        @Test
        fun `create PublicService as OrgWrite`() {
            val before = apiAuthorizedRequest(
                path,
                port,
                null,
                JwtToken(Access.ORG_ADMIN).toString(),
                HttpMethod.GET
            )
            assertEquals(HttpStatus.OK.value(), before["status"])

            val createResponse = apiAuthorizedRequest(
                path,
                port,
                mapper.writeValueAsString(PUBLIC_SERVICE_TO_BE_CREATED),
                JwtToken(Access.ORG_WRITE).toString(),
                HttpMethod.POST
            )
            assertEquals(HttpStatus.CREATED.value(), createResponse["status"])

            val after = apiAuthorizedRequest(
                path,
                port,
                null,
                JwtToken(Access.ORG_ADMIN).toString(),
                HttpMethod.GET
            )
            assertEquals(HttpStatus.OK.value(), after["status"])

            val allPublicServicesBefore: List<PublicService> = mapper.readValue(before["body"] as String)
            val allPublicServicesAfter: List<PublicService> = mapper.readValue(after["body"] as String)
            assertEquals(allPublicServicesBefore.size + 1, allPublicServicesAfter.size)
        }

        @Test
        fun `create PublicService as OrgAdmin`() {
            val createResponse = apiAuthorizedRequest(
                path,
                port,
                mapper.writeValueAsString(PUBLIC_SERVICE_TO_BE_CREATED),
                JwtToken(Access.ORG_ADMIN).toString(),
                HttpMethod.POST
            )
            assertEquals(HttpStatus.CREATED.value(), createResponse["status"])
        }

        @Test
        fun `create PublicService as OrgRead forbidden`() {
            val createResponse = apiAuthorizedRequest(
                path,
                port,
                mapper.writeValueAsString(PUBLIC_SERVICE_TO_BE_CREATED),
                JwtToken(Access.ORG_READ).toString(),
                HttpMethod.POST
            )
            assertEquals(HttpStatus.FORBIDDEN.value(), createResponse["status"])
        }

        @Test
        fun `create PublicService as WrongOrg forbidden`() {
            val createResponse = apiAuthorizedRequest(
                path,
                port,
                mapper.writeValueAsString(PUBLIC_SERVICE_TO_BE_CREATED),
                JwtToken(Access.WRONG_ORG_WRITE).toString(),
                HttpMethod.POST
            )
            assertEquals(HttpStatus.FORBIDDEN.value(), createResponse["status"])
        }
    }


    @Nested
    internal inner class PublishPublicService {
        val pathService1 = "/catalogs/910244132/public-services/1/publish"
        @Test
        fun `unauthorized when missing token` () {
            val response = apiAuthorizedRequest(
                pathService1,
                port,
                null,
                null,
                HttpMethod.POST)

            Assertions.assertEquals(HttpStatus.UNAUTHORIZED.value(), response["status"])
        }

        @Test
        fun `forbidden when authenticated as read user` () {
            val response = apiAuthorizedRequest(
                pathService1,
                port,
                null,
                JwtToken(Access.ORG_READ).toString(),
                HttpMethod.POST)

            Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response["status"])
        }

        @Test
        fun `not found when public service in different catalog`() {
            val response = apiAuthorizedRequest(
                "/catalogs/123456789/public-services/1/publish",
                port,
                null,
                JwtToken(Access.WRONG_ORG_WRITE).toString(),
                HttpMethod.POST)
            Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), response["status"])
        }

        @Test
        fun `not found when public service not in database`() {
            val response = apiAuthorizedRequest(
                "/catalogs/910244132/public-services/1000/publish",
                port,
                null,
                JwtToken(Access.ORG_WRITE).toString(),
                HttpMethod.POST)
            Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), response["status"])
        }

        @Test
        fun `able to publish public service when authenticated as a write user`() {
            val response = apiAuthorizedRequest(
                pathService1,
                port,
                null,
                JwtToken(Access.ORG_WRITE).toString(),
                HttpMethod.POST)
            Assertions.assertEquals(HttpStatus.OK.value(), response["status"])

            val result: PublicService = mapper.readValue(response["body"] as String)
            val expected = PUBLIC_SERVICE_1.copy(isPublished = true)
            Assertions.assertEquals(expected, result)
        }

        @Test
        fun `bad request when updating isPublished with normal patch updates`() {
            val operations = listOf(JsonPatchOperation(
                op = OpEnum.REPLACE,
                path = "/isPublished",
                value = true))
            val response = apiAuthorizedRequest(
                "/catalogs/910244132/public-services/1",
                port,
                mapper.writeValueAsString(operations),
                JwtToken(Access.ORG_WRITE).toString(),
                HttpMethod.PATCH)

            Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response["status"])
        }
    }
}
