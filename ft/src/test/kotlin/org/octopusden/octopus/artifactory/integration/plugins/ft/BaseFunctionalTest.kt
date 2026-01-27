package org.octopusden.octopus.artifactory.integration.plugins.ft

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.octopusden.octopus.artifactory.integration.plugins.ft.runner.gradleProcessInstance

class BaseFunctionalTest {
    companion object {
        const val ARTIFACTORY_USERNAME = "admin"
        const val ARTIFACTORY_PASSWORD = "password"
        const val ARTIFACTORY_REPO_KEY = "example-repo-local"
    }

    private val defaultTasks = arrayOf("clean", "build", "publish", "--info", "--stacktrace")
    private val artifactoryProperties = arrayOf(
        "-Dartifactory.url=${System.getProperty("artifactoryTestHost")}",
        "-Dartifactory.repoKey=$ARTIFACTORY_REPO_KEY",
        "-Dartifactory.username=$ARTIFACTORY_USERNAME",
        "-Dartifactory.password=$ARTIFACTORY_PASSWORD"
    )

    @Test
    fun testSimpleProject() {
        val buildName = "simple-project"
        val buildNumber = "3.0.0"
        val (instance, projectPath) = gradleProcessInstance {
            testProjectName = "gradle-projects/simple-project"
            tasks = defaultTasks
            additionalArguments = artifactoryProperties + arrayOf(
                "-Pversion=$buildNumber",
                "-PbuildInfo.build.name=$buildName",
                "-PbuildInfo.build.number=$buildNumber",
            )
        }
        assertEquals(0, instance.exitCode)
    }


}
