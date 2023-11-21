package no.digdir.servicecatalog.configuration

import no.digdir.servicecatalog.exception.CustomBadRequestException
import no.digdir.servicecatalog.exception.CustomInternalServerErrorException
import no.digdir.servicecatalog.exception.CustomNotAcceptableException
import no.digdir.servicecatalog.exception.CustomNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class RestResponseEntityExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(value = [RuntimeException::class])
    fun handleCustomExceptions(
        ex: RuntimeException, request: WebRequest
    ): ResponseEntity<Any> =
        when (ex::class) {
            CustomNotFoundException::class -> ResponseEntity(HttpStatus.NOT_FOUND)
            CustomNotAcceptableException::class -> ResponseEntity(HttpStatus.NOT_FOUND)
            CustomBadRequestException::class -> ResponseEntity(ex.message, HttpStatus.BAD_REQUEST)
            CustomInternalServerErrorException::class -> ResponseEntity(ex.message, HttpStatus.INTERNAL_SERVER_ERROR)
            else -> ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }

}
