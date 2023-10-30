package no.digdir.service_catalog.utils

import org.springframework.http.HttpStatus
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

fun apiGet(port: Int, endpoint: String, acceptHeader: String?): Map<String,Any> {

    return try {
        val connection = URL("http://localhost:$port$endpoint").openConnection() as HttpURLConnection
        if(acceptHeader != null) connection.setRequestProperty("Accept", acceptHeader)
        connection.connect()

        if(isOK(connection.responseCode)) {
            val responseBody = connection.inputStream.bufferedReader().use(BufferedReader::readText)
            mapOf(
                    "body"   to responseBody,
                    "header" to connection.headerFields.toString(),
                    "status" to connection.responseCode)
        } else {
            mapOf(
                    "status" to connection.responseCode,
                    "header" to " ",
                    "body"   to " "
            )
        }
    } catch (e: Exception) {
        mapOf(
                "status" to e.toString(),
                "header" to " ",
                "body"   to " "
        )
    }}

private fun isOK(response: Int?): Boolean =
        if(response == null) false
        else HttpStatus.resolve(response)?.is2xxSuccessful == true

fun apiAuthorizedRequest(path: String, port: Int, body: String?, token: String?, method: String): Map<String, Any> {
    val connection  = URL("http://localhost:$port$path").openConnection() as HttpURLConnection
    connection.requestMethod = method
    connection.setRequestProperty("Content-type", "application/json")
    connection.setRequestProperty("Accept", "application/json")

    if(!token.isNullOrEmpty()) {
        connection.setRequestProperty("Authorization", "Bearer $token")
    }

    return try {
        connection.doOutput = true
        connection.connect()

        if(body != null) {
            val writer = OutputStreamWriter(connection.outputStream)
            writer.write(body)
            writer.close()
        }

        if(isOK(connection.responseCode)){
            mapOf(
                    "body"   to connection.inputStream.bufferedReader().use(BufferedReader :: readText),
                    "header" to connection.headerFields.toString(),
                    "status" to connection.responseCode
            )
        } else {
            mapOf(
                    "status" to connection.responseCode,
                    "header" to " ",
                    "body" to " "
            )
        }
    } catch (e: Exception) {
        mapOf(
                "status" to e.toString(),
                "header" to " ",
                "body"   to " "
        )
    }
}
