package org.octopusden.octopus.artifactory.integration.plugins.ft

import com.platformlib.process.api.ProcessInstance
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.octopusden.octopus.artifactory.integration.plugins.ft.runner.mavenProcessInstance

class MavenFunctionalTest: BaseFunctionalTest() {

    override val defaultTasks = listOf("clean", "install", "deploy", "-X")
    override val artifactoryProperties = listOf(
        "-DartifactoryHost=$artifactoryHost",
        "-DartifactoryRepository=$ARTIFACTORY_REPO_KEY",
        "-DartifactoryUsername=$ARTIFACTORY_USERNAME",
        "-DartifactoryPassword=$ARTIFACTORY_PASSWORD"
    )

    @Test
    fun testSimpleProject() {
        val buildName = "simple-project-maven"
        val buildNumber = "3.0.911"

        assertBuildInfoNotFound(buildName, buildNumber)

        val instance = mavenProcessInstance {
            testProjectName = "maven-projects/simple-project"
            tasks = defaultTasks
            additionalArguments = artifactoryProperties + listOf(
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
        val buildNumber = "3.0.100"

        assertBuildInfoNotFound(buildName, buildNumber)

        val instance = mavenProcessInstance {
            testProjectName = "maven-projects/missing-parameters"
            tasks = defaultTasks
            additionalArguments = artifactoryProperties + listOf(
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
            additionalArguments = listOf(
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
            additionalArguments = artifactoryProperties + listOf(
                "-Dversion=$buildNumber",
                "-Dartifactory.build.name=$buildName",
                "-Dartifactory.build.version=$buildNumber"
            )
        }

        assertFailedOperations(instance, "package.json not found in directory", buildName, buildNumber)
    }

    override fun assertFailedOperations(instance: ProcessInstance, errorMessage: String, buildName: String, buildNumber: String) {
        assertEquals(1, instance.exitCode)
        assertTrue(instance.stdOut.any { Regex(errorMessage).containsMatchIn(it) })
        assertBuildInfoNotFound(buildName, buildNumber)
    }

}
