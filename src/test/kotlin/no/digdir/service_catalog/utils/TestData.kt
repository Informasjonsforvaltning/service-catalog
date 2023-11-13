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
val SERVICE_2 = Service("2", "title 2")

val SERVICES = listOf(SERVICE_0, SERVICE_1, SERVICE_2)

val PUBLIC_SERVICE_0 =
    PublicService("0", "910244132",
        title = LocalizedStrings("NB Tittel 0", "NN Tittel 0", "EN Tittel 0"),
        description = LocalizedStrings("Beskrivelse 0", "Beskriving 0", "Description 0"))
val PUBLIC_SERVICE_1 =
    PublicService("1", "910244132",
        title = LocalizedStrings("NB Tittel 1", "NN Tittel 1", "EN Tittel 1"),
        description = LocalizedStrings("Beskrivelse 1", "Beskriving 1", "Description 1"),
    )
val PUBLIC_SERVICE_2 =
    PublicService("2", "910244132",
        title = LocalizedStrings("NB Tittel 2", "NN Tittel 2", "EN Tittel 2"), null)

val PUBLIC_SERVICES = listOf(PUBLIC_SERVICE_0, PUBLIC_SERVICE_1, PUBLIC_SERVICE_2)
