package no.digdir.servicecatalog.configuration

import com.mongodb.client.MongoClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.MongoTemplate


@Configuration
open class MongoConfig(
    @Value("\${spring.data.mongodb.database}")
    private val database: String
) {

    @Bean
    open fun mongoTemplate(mongoClient: MongoClient): MongoOperations {
        return MongoTemplate(mongoClient, database)
    }

}
