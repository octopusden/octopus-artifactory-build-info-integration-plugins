package org.octopusden.octopus.artifactory.integration.plugins.ft

import com.platformlib.process.api.ProcessInstance
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.octopusden.octopus.artifactory.integration.plugins.ft.runner.gradleProcessInstance

class GradleFunctionalTest: BaseFunctionalTest() {

    override val defaultTasks = listOf("clean", "build", "publish", "--info", "--stacktrace")
    override val artifactoryProperties = listOf(
        "-Dartifactory.url=$artifactoryUrl",
        "-Dartifactory.repoKey=$ARTIFACTORY_REPO_KEY",
        "-Dartifactory.username=$ARTIFACTORY_USERNAME",
        "-Dartifactory.password=$ARTIFACTORY_PASSWORD"
    )

    @Test
    fun testSimpleProject() {
        val buildName = "simple-project-gradle"
        val buildNumber = "2.0.0"

        assertBuildInfoNotFound(buildName, buildNumber)

        val instance = gradleProcessInstance {
            testProjectName = "gradle-projects/simple-project"
            tasks = defaultTasks
            additionalArguments = artifactoryProperties + listOf(
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
        val buildNumber = "2.0.1"

        assertBuildInfoNotFound(buildName, buildNumber)

        val instance = gradleProcessInstance {
            testProjectName = "gradle-projects/simple-project"
            tasks = defaultTasks
            additionalArguments = artifactoryProperties + listOf(
                "-Pversion=$buildNumber"
            )
        }

        assertFailedOperations(instance, "Skipping NPM build info integration: buildName not configured", buildName, buildNumber)
    }

    @Test
    fun testMissingArtifactoryConfigurationParameters() {
        val buildName = "simple-project-gradle"
        val buildNumber = "2.0.2"

        assertBuildInfoNotFound(buildName, buildNumber)

        val instance = gradleProcessInstance {
            testProjectName = "gradle-projects/simple-project"
            tasks = defaultTasks
            additionalArguments = listOf(
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
        val buildNumber = "2.0.3"

        assertBuildInfoNotFound(buildName, buildNumber)

        val instance = gradleProcessInstance {
            testProjectName = "gradle-projects/missing-package-json"
            tasks = defaultTasks
            additionalArguments = artifactoryProperties + listOf(
                "-Pversion=$buildNumber",
                "-PbuildInfo.build.name=$buildName",
                "-PbuildInfo.build.number=$buildNumber",
            )
        }

        assertFailedOperations(instance, "Skipping NPM build info integration: package.json not found", buildName, buildNumber)
    }

    override fun assertFailedOperations(instance: ProcessInstance, errorMessage: String, buildName: String, buildNumber: String) {
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
