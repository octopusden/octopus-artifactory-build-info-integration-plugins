package org.octopusden.octopus.task

import com.google.common.net.HttpHeaders
import java.nio.charset.StandardCharsets
import org.apache.http.entity.ContentType
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.mockserver.client.MockServerClient
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse


abstract class ConfigureMockServer : DefaultTask() {
    @get:Input
    abstract val host: Property<String>
    @get:Input
    abstract val port: Property<Int>

    private val mockServerClient get() = MockServerClient(host.get(), port.get())

    @TaskAction
    fun configureMockServer() {
        mockServerClient.reset()
        mockServerClient.`when`(
            HttpRequest.request().withMethod("GET")
                .withPath("/rest/release-engineering/3/component/{component-name}/version/{version}/build")
                .withPathParameter("component-name")
                .withPathParameter("version")
        ).respond { request ->
            val component = request.getFirstPathParameter("component-name")
            val resourcePath = "/builds-$component.json"
            val resourceStream = this::class.java.getResourceAsStream(resourcePath)

            if (resourceStream != null) {
                val content = resourceStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
                HttpResponse.response()
                    .withStatusCode(200)
                    .withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.mimeType)
                    .withBody(content)
            } else {
                HttpResponse.response()
                    .withStatusCode(404)
                    .withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.mimeType)
                    .withBody("""{"error": "Build info not found for component: $component"}""")
            }
        }
    }
}