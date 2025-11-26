package org.octopusden.octopus.artifactory.npm.maven.plugin

import org.junit.jupiter.api.Test
import org.octopusden.octopus.infrastructure.artifactory.client.ArtifactoryClassicClient
import org.octopusden.octopus.infrastructure.artifactory.client.dto.BuildAgent
import org.octopusden.octopus.infrastructure.client.commons.ClientParametersProvider
import org.octopusden.octopus.infrastructure.client.commons.StandardBasicCredCredentialProvider
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PostITArtifactoryNpmMavenPluginTest {
    private val artifactoryClient = ArtifactoryClassicClient(object : ClientParametersProvider {
        override fun getApiUrl() = "http://localhost:18082"
        override fun getAuth() = StandardBasicCredCredentialProvider(
            "admin", "password"
        )
    })

    @Test
    fun assertNpmMavenBuildInfoIntegration() {
        val buildInfo = artifactoryClient.getBuildInfo("test-artifactory-npm-maven-plugin", "1.0.0").buildInfo

        assertEquals(BuildAgent("GENERIC", "2.66.0"), buildInfo.buildAgent)
        assertEquals(2, buildInfo.modules?.size)

        val mavenModule = buildInfo.modules?.find { it.type != "npm" }
        val npmModule = buildInfo.modules?.find { it.type == "npm" }

        assertEquals(1, mavenModule?.artifacts?.size)
        assertNull(mavenModule?.dependencies)
        assertNull(npmModule?.artifacts)
        assertEquals(3, npmModule?.dependencies?.size)
    }
}