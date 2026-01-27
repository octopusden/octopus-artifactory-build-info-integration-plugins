package org.octopusden.octopus.artifactory.integration.plugins.ft

import com.platformlib.process.api.ProcessInstance
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertThrows
import org.octopusden.octopus.artifactory.integration.plugins.ft.runner.gradleProcessInstance
import org.octopusden.octopus.infrastructure.artifactory.client.ArtifactoryClassicClient
import org.octopusden.octopus.infrastructure.artifactory.client.exception.NotFoundException
import org.octopusden.octopus.infrastructure.client.commons.ClientParametersProvider
import org.octopusden.octopus.infrastructure.client.commons.StandardBasicCredCredentialProvider

class GradleFunctionalTest {
    companion object {
        const val ARTIFACTORY_USERNAME = "admin"
        const val ARTIFACTORY_PASSWORD = "password"
        const val ARTIFACTORY_REPO_KEY = "example-repo-local"
    }

    private val defaultTasks = arrayOf("clean", "build", "publish", "--info", "--stacktrace")
    private val artifactoryHost = System.getProperty("artifactoryTestHost")
    private val artifactoryUrl = "http://$artifactoryHost"
    private val artifactoryProperties = arrayOf(
        "-Dartifactory.url=$artifactoryUrl",
        "-Dartifactory.repoKey=$ARTIFACTORY_REPO_KEY",
        "-Dartifactory.username=$ARTIFACTORY_USERNAME",
        "-Dartifactory.password=$ARTIFACTORY_PASSWORD"
    )

    private val artifactoryClient = ArtifactoryClassicClient(object : ClientParametersProvider {
        override fun getApiUrl() = artifactoryUrl
        override fun getAuth() = StandardBasicCredCredentialProvider(
            username = ARTIFACTORY_USERNAME,
            password = ARTIFACTORY_PASSWORD
        )
    })

    @Test
    fun testSimpleProject() {
        val buildName = "simple-project-gradle"
        val buildNumber = "31.10.2"

        assertBuildInfoNotFound(buildName, buildNumber)

        val instance = gradleProcessInstance {
            testProjectName = "gradle-projects/simple-project"
            tasks = defaultTasks
            additionalArguments = artifactoryProperties + arrayOf(
                "-Pversion=$buildNumber",
                "-PbuildInfo.build.name=$buildName",
                "-PbuildInfo.build.number=$buildNumber"
            )
        }

        assertEquals(0, instance.exitCode)

        val buildInfoResult = artifactoryClient.getBuildInfo(buildName, buildNumber)
        val modules = buildInfoResult.buildInfo.modules!!
        val moduleList = modules.toList()

        assertEquals(2, modules.size)

        assertEquals("gradle", moduleList[0].type)
        assertEquals("npm", moduleList[1].type)

        assertTrue(moduleList[0].artifacts!!.isNotEmpty())
        assertTrue(moduleList[1].artifacts!!.isEmpty())

        assertTrue(modules.all { it.dependencies!!.isNotEmpty() })
    }

    @Test
    fun testMissingBuildInfoParameters() {
        val buildName = "simple-project-gradle"
        val buildNumber = "33.11.3"

        assertBuildInfoNotFound(buildName, buildNumber)

        val instance = gradleProcessInstance {
            testProjectName = "gradle-projects/simple-project"
            tasks = defaultTasks
            additionalArguments = artifactoryProperties + arrayOf(
                "-Pversion=$buildNumber"
            )
        }

        assertFailedOperations(instance, "Skipping NPM build info integration: buildName not configured", buildName, buildNumber)
    }

    @Test
    fun testMissingArtifactoryConfigurationParameters() {
        val buildName = "simple-project-gradle"
        val buildNumber = "36.12.4"

        assertBuildInfoNotFound(buildName, buildNumber)

        val instance = gradleProcessInstance {
            testProjectName = "gradle-projects/simple-project"
            tasks = defaultTasks
            additionalArguments = arrayOf(
                "-Pversion=$buildNumber",
                "-PbuildInfo.build.name=$buildName",
                "-PbuildInfo.build.number=$buildNumber",
                "-Dartifactory.url=$artifactoryUrl"
            )
        }

        assertFailedOperations(instance, "Artifactory credentials are not properly configured", buildName, buildNumber)
    }

    @Test
    fun testMissingPackageJsonFile() {
        val buildName = "simple-project-gradle"
        val buildNumber = "36.13.6"

        assertBuildInfoNotFound(buildName, buildNumber)

        val instance = gradleProcessInstance {
            testProjectName = "gradle-projects/missing-package-json"
            tasks = defaultTasks
            additionalArguments = artifactoryProperties + arrayOf(
                "-Pversion=$buildNumber",
                "-PbuildInfo.build.name=$buildName",
                "-PbuildInfo.build.number=$buildNumber",
            )
        }

        assertFailedOperations(instance, "Skipping NPM build info integration: package.json not found", buildName, buildNumber)
    }

    private fun assertBuildInfoNotFound(buildName: String, buildNumber: String) {
        assertThrows<NotFoundException> {
            artifactoryClient.getBuildInfo(buildName, buildNumber)
        }
    }

    private fun assertFailedOperations(instance: ProcessInstance, errorMessage: String, buildName: String, buildNumber: String) {
        assertEquals(0, instance.exitCode)
        assertTrue(instance.stdErr.any { it.contains(errorMessage) } || instance.stdOut.any { it.contains(errorMessage) })

        val buildInfoResult = artifactoryClient.getBuildInfo(buildName, buildNumber)
        val modules = buildInfoResult.buildInfo.modules!!
        val moduleList = modules.toList()

        assertEquals(1, modules.size)

        assertEquals("gradle", moduleList[0].type)

        assertTrue(moduleList[0].artifacts!!.isNotEmpty())
        assertTrue(moduleList[0].dependencies!!.isNotEmpty())
    }
}
