package no.digdir.service_catalog.utils

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import no.digdir.service_catalog.utils.jwt.JwkStore

private val mockserver = WireMockServer(5050)

fun startMockServer() {
    if(!mockserver.isRunning) {
        mockserver.stubFor(get(urlEqualTo("/ping"))
                .willReturn(aResponse()
                        .withStatus(200))
        )

        mockserver.stubFor(get(urlEqualTo("/auth/realms/fdk/protocol/openid-connect/certs"))
                .willReturn(okJson(JwkStore.get())))

        mockserver.start()
    }
}

fun stopMockServer() {

    if (mockserver.isRunning) mockserver.stop()

}