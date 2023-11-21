package no.digdir.servicecatalog.utils

import no.digdir.servicecatalog.mongodb.PublicServiceRepository
import no.digdir.servicecatalog.mongodb.ServiceRepository
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.net.HttpURLConnection
import java.net.URL

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
    }

    internal class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
            TestPropertyValues.of(
                "spring.data.mongodb.uri=mongodb://$MONGO_USER:$MONGO_PASSWORD@localhost:${mongoContainer.getMappedPort(MONGO_PORT)}/$MONGO_DB_NAME?authSource=admin",
            ).applyTo(configurableApplicationContext.environment)
        }
    }

    companion object {
        val mongoContainer: GenericContainer<*> = GenericContainer("mongo:latest")
            .withEnv(MONGO_ENV_VALUES)
            .withExposedPorts(MONGO_PORT)
            .waitingFor(Wait.forListeningPort())

        init {
            startMockServer()
            mongoContainer.start()

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
