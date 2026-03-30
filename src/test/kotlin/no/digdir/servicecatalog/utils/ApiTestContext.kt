package no.digdir.servicecatalog.utils

import no.digdir.servicecatalog.repository.PublicServiceRepository
import no.digdir.servicecatalog.repository.ServiceRepository
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.containers.PostgreSQLContainer
import java.net.HttpURLConnection
import java.net.URI

abstract class ApiTestContext {
    @LocalServerPort
    var port: Int = 0

    @Autowired
    private lateinit var serviceRepository: ServiceRepository

    @Autowired
    private lateinit var publicServiceRepository: PublicServiceRepository

    @BeforeEach
    fun resetDatabase() {
        serviceRepository.deleteAll()
        serviceRepository.saveAll(SERVICES)

        publicServiceRepository.deleteAll()
        publicServiceRepository.saveAll(PUBLIC_SERVICES)
        publicServiceRepository.save(PUBLIC_SERVICE_DIFFERENT_CATALOG)
    }

    companion object {
        @JvmStatic
        @ServiceConnection
        val postgresContainer: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:16")

        init {
            startMockServer()
            postgresContainer.start()

            try {
                val con = URI("http://localhost:5050/ping").toURL().openConnection() as HttpURLConnection
                con.connect()
                if (con.responseCode != 200) {
                    stopMockServer()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                stopMockServer()
            }
        }
    }
}
