package no.digdir.service_catalog.utils

import no.digdir.service_catalog.model.Service
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap

const val MONGO_USER = "testuser"
const val MONGO_PASSWORD = "testpassword"
const val MONGO_PORT = 27017
const val MONGO_DB_NAME = "serviceCatalog"

val MONGO_ENV_VALUES: Map<String, String> = ImmutableMap.of(
    "MONGO_INITDB_ROOT_USERNAME", MONGO_USER,
    "MONGO_INITDB_ROOT_PASSWORD", MONGO_PASSWORD
)

val SERVICE_0 = Service("0", "title 0")
val SERVICE_1 = Service("1", "title 1")

val SERVICES = listOf(SERVICE_0, SERVICE_1)
