package no.digdir.servicecatalog.service

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.json.Json
import jakarta.json.JsonException
import no.digdir.servicecatalog.exception.CustomBadRequestException
import no.digdir.servicecatalog.exception.CustomInternalServerErrorException
import no.digdir.servicecatalog.model.JsonPatchOperation
import java.io.StringReader

inline fun <reified T> patchOriginal(original: T, operations: List<JsonPatchOperation>): T {
    validateOperations(operations)
    try {
        return applyPatch(original, operations)
    } catch (ex: Exception) {
        when (ex) {
            is JsonException -> throw CustomBadRequestException(message = ex.message)
            is JsonProcessingException -> throw CustomBadRequestException(message = ex.message)
            is IllegalArgumentException -> throw CustomBadRequestException(message = ex.message)
            else -> throw CustomInternalServerErrorException(message = ex.message)
        }
    }
}

inline fun <reified T> applyPatch(originalObject: T, operations: List<JsonPatchOperation>): T {
    if (operations.isNotEmpty()) {
        with(jacksonObjectMapper()) {
            val changes = Json.createReader(StringReader(writeValueAsString(operations))).readArray()
            val original = Json.createReader(StringReader(writeValueAsString(originalObject))).readObject()

            return Json.createPatch(changes).apply(original)
                .let { readValue(it.toString()) }
        }
    }
    return originalObject
}

fun validateOperations(operations: List<JsonPatchOperation>) {
    val invalidPaths = listOf("/id", "/catalogId", "/published")
    if (operations.any { it.path in invalidPaths }) {
        throw CustomBadRequestException(message = "Patch of paths $invalidPaths is not permitted")
    }
}