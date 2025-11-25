package no.digdir.servicecatalog.utils

import no.digdir.servicecatalog.repository.ServiceRepository
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.net.HttpURLConnection
import java.net.URL

abstract class ApiTestContext {
    @LocalServerPort
    var port: Int = 0

    @Autowired
    private lateinit var serviceRepository: ServiceRepository

    @BeforeEach
    fun resetDatabase() {
        serviceRepository.deleteAll()
        serviceRepository.saveAll(SERVICES.map { it.toDBO() })
        serviceRepository.saveAll(PUBLIC_SERVICES.map { it.toDBO() })
        serviceRepository.save(PUBLIC_SERVICE_DIFFERENT_CATALOG.toDBO())
    }

    internal class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
            TestPropertyValues.of(
                "spring.datasource.url=jdbc:postgresql://${postgreSQLContainer.host}:${
                    postgreSQLContainer.getMappedPort(
                        5432
                    )
                }/service_catalog",
                "spring.datasource.username=postgres",
                "spring.datasource.password=postgres",
            ).applyTo(configurableApplicationContext.environment)
        }
    }

    companion object {
        val postgreSQLContainer: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:14-alpine")
            .withExposedPorts(5432)
            .withUsername("postgres")
            .withPassword("postgres")
            .withDatabaseName("service_catalog")

        init {
            startMockServer()
            postgreSQLContainer.start()

            try {
                val con = URL("http://localhost:5050/ping").openConnection() as HttpURLConnection
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
