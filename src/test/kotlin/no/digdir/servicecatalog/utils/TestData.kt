package no.digdir.servicecatalog.utils

import no.digdir.servicecatalog.model.*
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap

const val MONGO_USER = "testuser"
const val MONGO_PASSWORD = "testpassword"
const val MONGO_PORT = 27017
const val MONGO_DB_NAME = "serviceCatalog"

val MONGO_ENV_VALUES: Map<String, String> = ImmutableMap.of(
    "MONGO_INITDB_ROOT_USERNAME", MONGO_USER,
    "MONGO_INITDB_ROOT_PASSWORD", MONGO_PASSWORD
)

val SERVICE_0 = Service("00", "910244132",
    title = LocalizedStrings("NB Tittel 00", "NN Tittel 00", "EN Tittel 00"),
    description = LocalizedStrings("Beskrivelse 00", "Beskriving 00", "Description 00"),
    published = true,
    produces = listOf(Output(
        identifier = "321",
        title = LocalizedStrings(en = "Output title", nb = null, nn = null),
        description = LocalizedStrings(en = "Output description", nb = null, nn = null),
        language = listOf("http://publications.europa.eu/resource/authority/language/ENG")
    )),
    contactPoints = listOf(ContactPoint(
        category = LocalizedStrings(en = "Contact category title", nb = null, nn = null),
        email = "email@digdir.no",
        telephone = "+4712345678",
        contactPage = "digdir.no",
        language = listOf("http://publications.europa.eu/resource/authority/language/ENG")
    )),
    homepage = "digdir.no", status = "http://purl.org/adms/status/Completed"
)
val SERVICE_1 = Service("01", "910244132",
    title = LocalizedStrings("NB Tittel 0", "NN Tittel 0", "EN Tittel 0"),
    description = LocalizedStrings("Beskrivelse 0", "Beskriving 0", "Description 0"),
    published = false, produces = null, contactPoints = null, homepage = null, status = null)
val SERVICE_2 = Service("02", "910244132",
    title = LocalizedStrings("NB Tittel 02", "NN Tittel 02", "EN Tittel 02"),
    description = LocalizedStrings("Beskrivelse 02", "Beskriving 02", "Description 02"),
    published = false, produces = null, contactPoints = null, homepage = null, status = null)

val SERVICE_TO_BE_CREATED = ServiceToBeCreated(title = LocalizedStrings("Ny tittel", "Ny tittel", "New title"), null, null, null, null, null)

val SERVICES = listOf(SERVICE_0, SERVICE_1, SERVICE_2)

val PUBLIC_SERVICE_0 =
    PublicService("0", "910244132",
        title = LocalizedStrings("NB Tittel 0", "NN Tittel 0", "EN Tittel 0"),
        description = LocalizedStrings("Beskrivelse 0", "Beskriving 0", "Description 0"),
        published = true,
        produces = listOf(Output(
            identifier = "123",
            title = LocalizedStrings(en = "Output title", nb = null, nn = null),
            description = LocalizedStrings(en = "Output description", nb = null, nn = null),
            language = listOf("http://publications.europa.eu/resource/authority/language/ENG")
        )),
        contactPoints = listOf(ContactPoint(
            category = LocalizedStrings(en = "Contact category title", nb = null, nn = null),
            email = "email@digdir.no",
            telephone = "+4712345678",
            contactPage = "digdir.no",
            language = listOf("http://publications.europa.eu/resource/authority/language/ENG")
        )),
        homepage = "data.norge.no", status = "http://purl.org/adms/status/UnderDevelopment",
        spatial = listOf("Norge")
    )
val PUBLIC_SERVICE_1 =
    PublicService("1", "910244132",
        title = LocalizedStrings("NB Tittel 1", "NN Tittel 1", "EN Tittel 1"),
        description = LocalizedStrings("Beskrivelse 1", "Beskriving 1", "Description 1"),
        published = false, produces = null, contactPoints = null, homepage = null, status = null,
        spatial = listOf("Norge"))
val PUBLIC_SERVICE_2 =
    PublicService("2", "910244132",
        title = LocalizedStrings("NB Tittel 2", "NN Tittel 2", "EN Tittel 2"),
        description = null,
        published = true, produces = null, contactPoints = null, homepage = null, status = null,
        spatial = listOf("Norge"))
val PUBLIC_SERVICE_DIFFERENT_CATALOG =
    PublicService("123", "123456789",
        title = LocalizedStrings("NB Tittel 0", "NN Tittel 0", "EN Tittel 0"),
        description = null,
        published = true, produces = null, contactPoints = null, homepage = null, status = null, spatial = listOf("Norge"))

val PUBLIC_SERVICE_TO_BE_CREATED = PublicServiceToBeCreated(title = LocalizedStrings("NB Tittel 2", "NN Tittel 2", "EN Tittel 2"),
    null, null, null, null, null, null)

val PUBLIC_SERVICES = listOf(PUBLIC_SERVICE_0, PUBLIC_SERVICE_1, PUBLIC_SERVICE_2)

val SERVICE_COUNT_1 = ServiceCount(catalogId = "910244132", serviceCount = 3, publicServiceCount = 3)

val SERVICE_COUNT_2 = ServiceCount(catalogId = "123456789", serviceCount = 0, publicServiceCount = 1 )

val LIST_OF_SERVICE_COUNTS_ROOT = listOf(SERVICE_COUNT_1, SERVICE_COUNT_2)
