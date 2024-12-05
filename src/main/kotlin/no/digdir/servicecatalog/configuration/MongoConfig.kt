package no.digdir.servicecatalog.configuration

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.web.util.UriUtils
import java.nio.charset.Charset


@Configuration
open class MongoConfig(private val props: MongoProperties) {

    private val uri: String = "mongodb://${
        UriUtils.encodeUserInfo(props.username, Charset.defaultCharset())
    }:${
        UriUtils.encodeUserInfo(props.password, Charset.defaultCharset())
    }@${props.host}?authSource=${props.auth}${
        if(props.rs.isNotBlank()) "&replicaSet=${props.rs}" else ""
    }"

    @Bean
    open fun mongoClient(): MongoClient =
        MongoClients.create(uri)

    @Bean
    open fun mongoTemplate(mongoClient: MongoClient): MongoOperations {
        return MongoTemplate(mongoClient, props.db)
    }

}
