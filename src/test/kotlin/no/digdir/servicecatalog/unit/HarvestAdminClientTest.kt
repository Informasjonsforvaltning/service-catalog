package no.digdir.servicecatalog.unit

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.digdir.servicecatalog.adapter.HarvestAdminClient
import no.digdir.servicecatalog.configuration.ApplicationProperties
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.net.URI
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
@Tag("unit")
class HarvestAdminClientTest {

    @Mock
    private lateinit var applicationProperties: ApplicationProperties

    @Mock
    private lateinit var restTemplate: RestTemplate

    private lateinit var client: HarvestAdminClient

    @BeforeEach
    fun setUp() {
        client = HarvestAdminClient(applicationProperties, restTemplate)
    }

    private val mapper = jacksonObjectMapper()
    private val catalogId = "123456789"
    private val harvestAdminUri = "http://harvest-admin:8080"
    private val serviceCatalogUri = "http://service-catalog:8080"

    @AfterEach
    fun cleanup() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `createNewDataSource posts to correct URL`() {
        setupApplicationProperties()
        stubPostForEntity()

        client.createNewDataSource(catalogId)

        val uriCaptor = captor<URI>()
        verify(restTemplate).postForEntity(uriCaptor.capture(), any<HttpEntity<*>>(), eq(Any::class.java))

        assertEquals(
            URI("$harvestAdminUri/organizations/$catalogId/datasources"),
            uriCaptor.value
        )
    }

    @Test
    fun `createNewDataSource sends correct body`() {
        setupApplicationProperties()
        stubPostForEntity()

        client.createNewDataSource(catalogId)

        val entityCaptor = captor<HttpEntity<*>>()
        verify(restTemplate).postForEntity(any<URI>(), entityCaptor.capture(), eq(Any::class.java))

        val body = mapper.convertValue(entityCaptor.value.body, Map::class.java)
        assertEquals("CPSV-AP-NO", body["dataSourceType"])
        assertEquals("publicService", body["dataType"])
        assertEquals("$serviceCatalogUri/catalogs/$catalogId", body["url"])
        assertEquals("text/turtle", body["acceptHeaderValue"])
        assertEquals(catalogId, body["publisherId"])
        assertEquals("Automatically generated data source for $catalogId", body["description"])
    }

    @Test
    fun `createNewDataSource sets JSON content type`() {
        setupApplicationProperties()
        stubPostForEntity()

        client.createNewDataSource(catalogId)

        val entityCaptor = captor<HttpEntity<*>>()
        verify(restTemplate).postForEntity(any<URI>(), entityCaptor.capture(), eq(Any::class.java))

        assertEquals(MediaType.APPLICATION_JSON, entityCaptor.value.headers.contentType)
    }

    @Test
    fun `createNewDataSource includes bearer token when authenticated`() {
        setupApplicationProperties()
        stubPostForEntity()
        setSecurityContext("my-jwt-token")

        client.createNewDataSource(catalogId)

        val entityCaptor = captor<HttpEntity<*>>()
        verify(restTemplate).postForEntity(any<URI>(), entityCaptor.capture(), eq(Any::class.java))

        assertEquals("Bearer my-jwt-token", entityCaptor.value.headers.getFirst(HttpHeaders.AUTHORIZATION))
    }

    @Test
    fun `createNewDataSource omits authorization header when not authenticated`() {
        setupApplicationProperties()
        stubPostForEntity()

        client.createNewDataSource(catalogId)

        val entityCaptor = captor<HttpEntity<*>>()
        verify(restTemplate).postForEntity(any<URI>(), entityCaptor.capture(), eq(Any::class.java))

        assertNull(entityCaptor.value.headers.getFirst(HttpHeaders.AUTHORIZATION))
    }

    @Test
    fun `createNewDataSource does not throw on RestTemplate failure`() {
        setupApplicationProperties()
        whenever(restTemplate.postForEntity(any<URI>(), any<HttpEntity<*>>(), eq(Any::class.java)))
            .thenThrow(RestClientException("connection refused"))

        client.createNewDataSource(catalogId)
    }

    @Test
    fun `triggerHarvest posts to correct URL`() {
        setupApplicationProperties()
        stubPostForEntity()

        client.triggerHarvest(catalogId)

        val uriCaptor = captor<URI>()
        verify(restTemplate).postForEntity(uriCaptor.capture(), any<HttpEntity<*>>(), eq(Any::class.java))

        assertEquals(
            URI("$harvestAdminUri/organizations/$catalogId/datasources/start-harvesting"),
            uriCaptor.value
        )
    }

    @Test
    fun `triggerHarvest sends correct body`() {
        setupApplicationProperties()
        stubPostForEntity()

        client.triggerHarvest(catalogId)

        val entityCaptor = captor<HttpEntity<*>>()
        verify(restTemplate).postForEntity(any<URI>(), entityCaptor.capture(), eq(Any::class.java))

        val body = mapper.convertValue(entityCaptor.value.body, Map::class.java)
        assertEquals("$serviceCatalogUri/catalogs/$catalogId", body["url"])
        assertEquals("publicService", body["dataType"])
    }

    @Test
    fun `triggerHarvest sets JSON content type`() {
        setupApplicationProperties()
        stubPostForEntity()

        client.triggerHarvest(catalogId)

        val entityCaptor = captor<HttpEntity<*>>()
        verify(restTemplate).postForEntity(any<URI>(), entityCaptor.capture(), eq(Any::class.java))

        assertEquals(MediaType.APPLICATION_JSON, entityCaptor.value.headers.contentType)
    }

    @Test
    fun `triggerHarvest includes bearer token when authenticated`() {
        setupApplicationProperties()
        stubPostForEntity()
        setSecurityContext("another-token")

        client.triggerHarvest(catalogId)

        val entityCaptor = captor<HttpEntity<*>>()
        verify(restTemplate).postForEntity(any<URI>(), entityCaptor.capture(), eq(Any::class.java))

        assertEquals("Bearer another-token", entityCaptor.value.headers.getFirst(HttpHeaders.AUTHORIZATION))
    }

    @Test
    fun `triggerHarvest omits authorization header when not authenticated`() {
        setupApplicationProperties()
        stubPostForEntity()

        client.triggerHarvest(catalogId)

        val entityCaptor = captor<HttpEntity<*>>()
        verify(restTemplate).postForEntity(any<URI>(), entityCaptor.capture(), eq(Any::class.java))

        assertNull(entityCaptor.value.headers.getFirst(HttpHeaders.AUTHORIZATION))
    }

    @Test
    fun `triggerHarvest does not throw on RestTemplate failure`() {
        setupApplicationProperties()
        whenever(restTemplate.postForEntity(any<URI>(), any<HttpEntity<*>>(), eq(Any::class.java)))
            .thenThrow(RestClientException("connection refused"))

        client.triggerHarvest(catalogId)
    }

    private fun setupApplicationProperties() {
        whenever(applicationProperties.harvestAdminUri).thenReturn(harvestAdminUri)
        whenever(applicationProperties.serviceCatalogUri).thenReturn(serviceCatalogUri)
    }

    private fun stubPostForEntity() {
        whenever(restTemplate.postForEntity(any<URI>(), any<HttpEntity<*>>(), eq(Any::class.java)))
            .thenReturn(ResponseEntity.ok("ok"))
    }

    private fun setSecurityContext(tokenValue: String) {
        val jwt = Jwt.withTokenValue(tokenValue)
            .header("alg", "RS256")
            .claim("sub", "test-user")
            .build()
        SecurityContextHolder.getContext().authentication = JwtAuthenticationToken(jwt)
    }

    private inline fun <reified T : Any> captor(): ArgumentCaptor<T> =
        ArgumentCaptor.forClass(T::class.java)
}
