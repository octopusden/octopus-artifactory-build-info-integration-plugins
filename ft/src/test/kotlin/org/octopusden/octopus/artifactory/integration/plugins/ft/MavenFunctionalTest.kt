package org.octopusden.octopus.artifactory.integration.plugins.ft

import com.platformlib.process.api.ProcessInstance
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.octopusden.octopus.artifactory.integration.plugins.ft.runner.mavenProcessInstance
import org.octopusden.octopus.infrastructure.artifactory.client.ArtifactoryClassicClient
import org.octopusden.octopus.infrastructure.artifactory.client.exception.NotFoundException
import org.octopusden.octopus.infrastructure.client.commons.ClientParametersProvider
import org.octopusden.octopus.infrastructure.client.commons.StandardBasicCredCredentialProvider

class MavenFunctionalTest {

    companion object {
        const val ARTIFACTORY_USERNAME = "admin"
        const val ARTIFACTORY_PASSWORD = "password"
        const val ARTIFACTORY_REPO_KEY = "example-repo-local"
    }

    private val defaultTasks = arrayOf("clean", "install", "deploy", "-X")
    private val artifactoryHost = System.getProperty("artifactoryTestHost")
    private val artifactoryUrl = "http://$artifactoryHost"
    private val artifactoryProperties = arrayOf(
        "-DartifactoryHost=$artifactoryHost",
        "-DartifactoryRepository=$ARTIFACTORY_REPO_KEY",
        "-DartifactoryUsername=$ARTIFACTORY_USERNAME",
        "-DartifactoryPassword=$ARTIFACTORY_PASSWORD"
    )

    private val artifactoryClient = ArtifactoryClassicClient(object : ClientParametersProvider {
        override fun getApiUrl() = artifactoryUrl
        override fun getAuth() = StandardBasicCredCredentialProvider(
            username = BaseFunctionalTest.ARTIFACTORY_USERNAME,
            password = BaseFunctionalTest.ARTIFACTORY_PASSWORD
        )
    })

    @Test
    fun testSimpleProject() {
        val buildName = "simple-project-maven"
        val buildNumber = "3.0.90"

        assertBuildInfoNotFound(buildName, buildNumber)

        val instance = mavenProcessInstance {
            testProjectName = "maven-projects/simple-project"
            tasks = defaultTasks
            additionalArguments = artifactoryProperties + arrayOf(
                "-Dversion=$buildNumber",
                "-Dartifactory.build.name=$buildName",
                "-Dartifactory.build.version=$buildNumber",
            )
        }

        assertEquals(0, instance.exitCode)

        val buildInfoResult = artifactoryClient.getBuildInfo(buildName, buildNumber)
        val modules = buildInfoResult.buildInfo.modules!!
        val moduleList = modules.toList()

        assertEquals(2, modules.size)

        assertEquals("npm", moduleList[1].type)

        assertTrue(moduleList[0].artifacts!!.isNotEmpty())
        assertTrue(moduleList[1].artifacts!!.isEmpty())

        assertTrue(modules.all { it.dependencies!!.isNotEmpty() })
    }

    @Test
    fun testMissingBuildInfoParameters() {
        val buildName = "simple-project-maven"
        val buildNumber = "3.0.90"

        assertBuildInfoNotFound(buildName, buildNumber)

        val instance = mavenProcessInstance {
            testProjectName = "maven-projects/missing-parameters"
            tasks = defaultTasks
            additionalArguments = artifactoryProperties + arrayOf(
                "-Dversion=$buildNumber"

            )
        }

        assertFailedOperations(instance, "The parameters 'buildName', 'buildNumber'.*are missing or invalid", buildName, buildNumber)
    }

    @Test
    fun testMissingArtifactoryConfigurationParameters() {
        val buildName = "simple-project-maven"
        val buildNumber = "3.0.6"

        assertBuildInfoNotFound(buildName, buildNumber)

        val instance = mavenProcessInstance {
            testProjectName = "maven-projects/missing-parameters"
            tasks = defaultTasks
            additionalArguments = arrayOf(
                "-Dversion=$buildNumber",
                "-Dartifactory.build.name=$buildName",
                "-Dartifactory.build.version=$buildNumber",
                "-DartifactoryHost=$artifactoryHost"
            )
        }

        assertFailedOperations(instance, "Artifactory credentials are not properly configured", buildName, buildNumber)
    }

    @Test
    fun testMissingPackageJsonFile() {
        val buildName = "simple-project-maven"
        val buildNumber = "3.0.6"

        assertBuildInfoNotFound(buildName, buildNumber)

        val instance = mavenProcessInstance {
            testProjectName = "maven-projects/missing-package-json"
            tasks = defaultTasks
            additionalArguments = artifactoryProperties + arrayOf(
                "-Dversion=$buildNumber",
                "-Dartifactory.build.name=$buildName",
                "-Dartifactory.build.version=$buildNumber"
            )
        }

        assertFailedOperations(instance, "package.json not found in directory", buildName, buildNumber)
    }

    private fun assertBuildInfoNotFound(buildName: String, buildNumber: String) {
        assertThrows<NotFoundException> {
            artifactoryClient.getBuildInfo(buildName, buildNumber)
        }
    }

    private fun assertFailedOperations(instance: ProcessInstance, errorMessage: String, buildName: String, buildNumber: String) {
        assertEquals(1, instance.exitCode)
        assertTrue(instance.stdOut.any { Regex(errorMessage).containsMatchIn(it) })
        assertBuildInfoNotFound(buildName, buildNumber)
    }
}