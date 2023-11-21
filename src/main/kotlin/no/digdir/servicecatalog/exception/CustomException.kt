package no.digdir.servicecatalog.exception

class CustomNotFoundException(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause)
class CustomNotAcceptableException(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause)
class CustomBadRequestException(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause)
class CustomInternalServerErrorException(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause)
