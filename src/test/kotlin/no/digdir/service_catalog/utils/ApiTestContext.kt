package no.digdir.service_catalog.utils

import org.springframework.boot.test.web.server.LocalServerPort

abstract class ApiTestContext {
    @LocalServerPort
    var port: Int = 0
}
