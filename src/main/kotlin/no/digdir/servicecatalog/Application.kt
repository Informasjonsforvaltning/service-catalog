package no.digdir.servicecatalog

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity

@EnableWebSecurity
@SpringBootApplication
@ConfigurationPropertiesScan
open class Application

val MAIN_LOGGER: Logger = LoggerFactory.getLogger(Application::class.java)

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
