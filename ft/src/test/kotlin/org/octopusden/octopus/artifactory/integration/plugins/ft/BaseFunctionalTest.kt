package org.octopusden.octopus.artifactory.integration.plugins.ft

import com.platformlib.process.api.ProcessInstance
import org.junit.jupiter.api.assertThrows
import org.octopusden.octopus.infrastructure.artifactory.client.ArtifactoryClassicClient
import org.octopusden.octopus.infrastructure.artifactory.client.exception.NotFoundException
import org.octopusden.octopus.infrastructure.client.commons.ClientParametersProvider
import org.octopusden.octopus.infrastructure.client.commons.StandardBasicCredCredentialProvider

abstract class BaseFunctionalTest {

    companion object {
        const val ARTIFACTORY_USERNAME = "admin"
        const val ARTIFACTORY_PASSWORD = "password"
        const val ARTIFACTORY_REPO_KEY = "example-repo-local"
    }

    protected val artifactoryHost = System.getProperty("artifactoryTestHost")
    protected val artifactoryUrl = "http://$artifactoryHost"

    abstract val defaultTasks: List<String>
    abstract val artifactoryProperties: List<String>

    protected val artifactoryClient = ArtifactoryClassicClient(object : ClientParametersProvider {
        override fun getApiUrl() = artifactoryUrl
        override fun getAuth() = StandardBasicCredCredentialProvider(
            username = ARTIFACTORY_USERNAME,
            password = ARTIFACTORY_PASSWORD
        )
    })

    protected fun assertBuildInfoNotFound(buildName: String, buildNumber: String) {
        assertThrows<NotFoundException> {
            artifactoryClient.getBuildInfo(buildName, buildNumber)
        }
    }

    abstract fun assertFailedOperations(instance: ProcessInstance, errorMessage: String, buildName: String, buildNumber: String)
}