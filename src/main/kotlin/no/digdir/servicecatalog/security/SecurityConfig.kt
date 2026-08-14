package no.digdir.servicecatalog.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.jwt.JwtClaimNames.AUD
import org.springframework.security.oauth2.jwt.JwtClaimValidator
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtIssuerValidator
import org.springframework.security.oauth2.jwt.JwtTimestampValidator
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource

@Configuration
class SecurityConfig(
    @param:Value("\${application.cors.originPatterns}")
    val corsOriginPatterns: Array<String>,
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            cors {
                configurationSource = CorsConfigurationSource {
                    val config = CorsConfiguration()
                    config.allowCredentials = false
                    config.allowedHeaders = listOf("*")
                    config.maxAge = 3600L
                    config.allowedOriginPatterns = corsOriginPatterns.toList()
                    config.allowedMethods = listOf("GET", "POST", "OPTIONS", "DELETE", "PATCH")
                    config
                }
            }
            authorizeHttpRequests {
                authorize(HttpMethod.GET, "/actuator/health/readiness", permitAll)
                authorize(HttpMethod.GET, "/actuator/health/liveness", permitAll)
                authorize(HttpMethod.GET, "/internal/**", authenticated)
                authorize(HttpMethod.GET, "/**", permitAll)
                authorize(anyRequest, authenticated)
            }
            oauth2ResourceServer { jwt { } }
        }
        return http.build()
    }

    @Bean
    fun jwtDecoder(properties: OAuth2ResourceServerProperties): JwtDecoder {
        val jwkSetUri =
            requireNotNull(properties.jwt.jwkSetUri) {
                "spring.security.oauth2.resourceserver.jwt.jwk-set-uri is required"
            }
        val issuerUri =
            requireNotNull(properties.jwt.issuerUri) {
                "spring.security.oauth2.resourceserver.jwt.issuer-uri is required"
            }
        val jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build()
        jwtDecoder.setJwtValidator(
            DelegatingOAuth2TokenValidator(
                JwtTimestampValidator(),
                JwtIssuerValidator(issuerUri),
                JwtClaimValidator(AUD) { aud: List<String> -> aud.contains("service-catalog") },
            ),
        )
        return jwtDecoder
    }
}
