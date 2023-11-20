package no.digdir.service_catalog.configuration

class CustomNotFoundException(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause)
class CustomNotAcceptableException(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause)
class CustomBadRequestException(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause)
class CustomInternalServerErrorException(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause)
