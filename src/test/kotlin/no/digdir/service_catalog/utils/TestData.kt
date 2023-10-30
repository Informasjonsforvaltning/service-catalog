package no.digdir.service_catalog.utils

import no.digdir.service_catalog.model.LocalizedStrings
import no.digdir.service_catalog.model.PublicService
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

val PUBLIC_SERVICE_0 =
    PublicService("0", "910244132",
        title = LocalizedStrings("Tittel 0", "Tittel 0", "Tittel 0"),
        description = LocalizedStrings("Beskrivelse 0", "Beskriving 0", "Description 0"))
val PUBLIC_SERVICE_1 =
    PublicService("1", "910244132",
        title = LocalizedStrings("Tittel 1", "Tittel 1", "Tittel 1"),
        description = LocalizedStrings("Beskrivelse 1", "Beskriving 1", "Description 1"))

val PUBLIC_SERVICES = listOf(PUBLIC_SERVICE_0, PUBLIC_SERVICE_1)
