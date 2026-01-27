package org.octopusden.octopus.artifactory.integration.plugins.ft

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertThrows
import org.octopusden.octopus.artifactory.integration.plugins.ft.runner.gradleProcessInstance
import org.octopusden.octopus.infrastructure.artifactory.client.ArtifactoryClassicClient
import org.octopusden.octopus.infrastructure.artifactory.client.exception.NotFoundException
import org.octopusden.octopus.infrastructure.client.commons.ClientParametersProvider
import org.octopusden.octopus.infrastructure.client.commons.StandardBasicCredCredentialProvider

class BaseFunctionalTest {
    companion object {
        const val ARTIFACTORY_USERNAME = "admin"
        const val ARTIFACTORY_PASSWORD = "password"
        const val ARTIFACTORY_REPO_KEY = "example-repo-local"
    }

    private val defaultTasks = arrayOf("clean", "build", "publish", "--info", "--stacktrace")
    private val artifactoryUrl = "http://${System.getProperty("artifactoryTestHost")}"
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
        val buildNumber = "31.1.0"

        assertThrows<NotFoundException> {
            artifactoryClient.getBuildInfo(buildName, buildNumber)
        }

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
        val buildNumber = "33.1.1"

        assertThrows<NotFoundException> {
            artifactoryClient.getBuildInfo(buildName, buildNumber)
        }

        val instance = gradleProcessInstance {
            testProjectName = "gradle-projects/simple-project"
            tasks = defaultTasks
            additionalArguments = artifactoryProperties + arrayOf(
                "-Pversion=$buildNumber"
            )
        }

        assertEquals(0, instance.exitCode)
        assertTrue(instance.stdOut.contains("Skipping NPM build info integration: buildName not configured"))

        val buildInfoResult = artifactoryClient.getBuildInfo(buildName, buildNumber)
        val modules = buildInfoResult.buildInfo.modules!!
        val moduleList = modules.toList()

        assertEquals(1, modules.size)

        assertEquals("gradle", moduleList[0].type)

        assertTrue(moduleList[0].artifacts!!.isNotEmpty())
        assertTrue(moduleList[0].dependencies!!.isNotEmpty())
    }

    @Test
    fun testMissingArtifactoryConfigurationParameters() {
        val buildName = "simple-project-gradle"
        val buildNumber = "36.1.2"

        assertThrows<NotFoundException> {
            artifactoryClient.getBuildInfo(buildName, buildNumber)
        }

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

        assertEquals(0, instance.exitCode)
        assertTrue(instance.stdErr.contains("Failed to integrate NPM build info: Artifactory credentials are not properly configured. " +
                "Please set system property 'artifactory.accessToken' or both 'artifactory.username' and 'artifactory.password'."))

        val buildInfoResult = artifactoryClient.getBuildInfo(buildName, buildNumber)
        val modules = buildInfoResult.buildInfo.modules!!
        val moduleList = modules.toList()

        assertEquals(1, modules.size)

        assertEquals("gradle", moduleList[0].type)

        assertTrue(moduleList[0].artifacts!!.isNotEmpty())
        assertTrue(moduleList[0].dependencies!!.isNotEmpty())
    }

    @Test
    fun testMissingPackageJsonFile() {
        val buildName = "simple-project-gradle"
        val buildNumber = "36.1.4"

        assertThrows<NotFoundException> {
            artifactoryClient.getBuildInfo(buildName, buildNumber)
        }

        val instance = gradleProcessInstance {
            testProjectName = "gradle-projects/missing-package-json"
            tasks = defaultTasks
            additionalArguments = artifactoryProperties + arrayOf(
                "-Pversion=$buildNumber",
                "-PbuildInfo.build.name=$buildName",
                "-PbuildInfo.build.number=$buildNumber",
            )
        }

        assertEquals(0, instance.exitCode)
        assertTrue(instance.stdOut.contains("Skipping NPM build info integration: package.json not found"))

        val buildInfoResult = artifactoryClient.getBuildInfo(buildName, buildNumber)
        val modules = buildInfoResult.buildInfo.modules!!
        val moduleList = modules.toList()

        assertEquals(1, modules.size)

        assertEquals("gradle", moduleList[0].type)

        assertTrue(moduleList[0].artifacts!!.isNotEmpty())
        assertTrue(moduleList[0].dependencies!!.isNotEmpty())
    }

}
