package org.octopusden.octopus.artifactory.integration.plugins.ft

import com.platformlib.process.api.ProcessInstance
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.octopusden.octopus.artifactory.integration.plugins.ft.runner.gradleProcessInstance
import org.octopusden.octopus.infrastructure.artifactory.client.dto.Module

class GradleFunctionalTest: BaseFunctionalTest() {

    override val defaultTasks = listOf("clean", "build", "publish", "--info", "--stacktrace")
    override val artifactoryProperties = listOf(
        "-Dartifactory.url=$artifactoryUrl",
        "-Dartifactory.repoKey=$ARTIFACTORY_REPO_KEY",
        "-Dartifactory.username=$ARTIFACTORY_USERNAME",
        "-Dartifactory.password=$ARTIFACTORY_PASSWORD"
    )

    override val serviceProperties = listOf(
        "-Pcomponent-registry-service-url=$componentsRegistryServiceUrl",
        "-Prelease-management-service-url=$releaseManagementServiceUrl",
    )

    @Test
    fun testSimpleProject() {
        val buildName = "simple-project-gradle"
        val buildNumber = "2.0.0"

        assertBuildInfoNotFound(buildName, buildNumber)

        val instance = gradleProcessInstance {
            testProjectName = "gradle-projects/simple-project"
            tasks = defaultTasks
            additionalArguments = artifactoryProperties + serviceProperties + listOf(
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
            additionalArguments = artifactoryProperties + serviceProperties + listOf(
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
            additionalArguments = serviceProperties + listOf(
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
            additionalArguments = artifactoryProperties + serviceProperties + listOf(
                "-Pversion=$buildNumber",
                "-PbuildInfo.build.name=$buildName",
                "-PbuildInfo.build.number=$buildNumber",
            )
        }

        assertFailedOperations(instance, "No package.json found and no dependencies resolved, skipping NPM build info integration", buildName, buildNumber)
    }

    /**
     * Tests NPM build info integration with multiple dependency combinations.
     *
     * This test validates the plugin's behavior across four different scenarios:
     *
     * **Scenario 1: Project with package.json only (dependency-ee)**
     * - Project has a package.json file but no external dependencies
     * - Expected: Build info contains both Gradle and NPM modules
     *
     * **Scenario 2: Project with package.json and dependencies (dependency-ei)**
     * - Project has a package.json file AND declares dependencies with NPM build info
     * - Dependencies file references dependency-ee which has NPM build info
     * - Expected: Build info contains Gradle module, own NPM module, and NPM modules from dependency-ee
     *
     * **Scenario 3: Project without package.json and no dependencies (dependency-ie)**
     * - Project has no package.json file and no declared dependencies
     * - Expected: Build info contains only the Gradle module
     * - NPM integration is skipped due to missing package.json
     *
     * **Scenario 4: Project without package.json but with multiple dependencies (main-component)**
     * - Project has no package.json but declares multiple dependencies (dependency-ei, dependency-ie)
     * - dependency-ei and its dependency (dependency-ee) have NPM build info that should be aggregated
     * - Expected: Build info contains Gradle module + unique NPM modules from all transitive dependencies
     * - NPM modules are deduplicated by module ID to avoid duplicates from transitive dependencies
     */
    @Test
    fun testDependenciesResolution() {
        val eeBuildName = "dependency-ee_ee"
        val eiBuildName = "dependency-ei_ei"
        val ieBuildName = "dependency-ie_ie"
        val buildName = "main-component_ie"
        val buildNumber = "1.0.0"

        assertBuildInfoNotFound(eeBuildName, buildNumber)
        val eeInstance = gradleProcessInstance {
            testProjectName = "gradle-projects/simple-project"
            tasks = defaultTasks
            additionalArguments = artifactoryProperties + serviceProperties + listOf(
                "-Pversion=$buildNumber",
                "-PbuildInfo.build.name=$eeBuildName",
                "-PbuildInfo.build.number=$buildNumber"
            )
        }
        assertEquals(0, eeInstance.exitCode)
        val eeBuildInfo = artifactoryClient.getBuildInfo(eeBuildName, buildNumber).buildInfo
        val eeModules = eeBuildInfo.modules!!.toList()
        assertEquals(2, eeModules.size)
        assertEquals("gradle", eeModules[0].type)
        assertEquals("npm", eeModules[1].type)

        assertBuildInfoNotFound(eiBuildName, buildNumber)
        val eiInstance = gradleProcessInstance {
            testProjectName = "gradle-projects/project-with-dependencies"
            tasks = defaultTasks
            additionalArguments = artifactoryProperties + serviceProperties + listOf(
                "-Pversion=$buildNumber",
                "-PbuildInfo.build.name=$eiBuildName",
                "-PbuildInfo.build.number=$buildNumber",
                "-PdependenciesFilePath=dependencies.json"
            )
        }
        assertEquals(0, eiInstance.exitCode)
        val eiBuildInfo = artifactoryClient.getBuildInfo(eiBuildName, buildNumber).buildInfo
        val eiModules = eiBuildInfo.modules!!.toList()
        assertEquals(3, eiModules.size)
        assertEquals("gradle", eiModules[0].type)
        assertEquals("npm", eiModules[1].type)
        assertModule(eeModules[1], eiModules[2])

        assertBuildInfoNotFound(ieBuildName, buildNumber)
        val ieInstance = gradleProcessInstance {
            testProjectName = "gradle-projects/missing-package-json"
            tasks = defaultTasks
            additionalArguments = artifactoryProperties + serviceProperties + listOf(
                "-Pversion=$buildNumber",
                "-PbuildInfo.build.name=$ieBuildName",
                "-PbuildInfo.build.number=$buildNumber",
            )
        }
        assertEquals(0, ieInstance.exitCode)
        val ieBuildInfo = artifactoryClient.getBuildInfo(ieBuildName, buildNumber).buildInfo
        val ieModules = ieBuildInfo.modules!!.toList()
        assertEquals(1, ieModules.size)
        assertEquals("gradle", ieModules[0].type)

        assertBuildInfoNotFound(buildName, buildNumber)
        val instance = gradleProcessInstance {
            testProjectName = "gradle-projects/project-with-dependencies-missing-package-json"
            tasks = defaultTasks
            additionalArguments = artifactoryProperties + serviceProperties + listOf(
                "-Pversion=$buildNumber",
                "-PbuildInfo.build.name=$buildName",
                "-PbuildInfo.build.number=$buildNumber",
                "-PdependenciesFilePath=dependencies.json"
            )
        }
        assertEquals(0, instance.exitCode)
        val buildInfo = artifactoryClient.getBuildInfo(buildName, buildNumber).buildInfo
        val modules = buildInfo.modules!!.toList()
        assertEquals(3, modules.size)
        assertEquals("gradle", modules[0].type)
        assertModule(eiModules.find { it.id == modules[1].id }!!, modules[1])
        assertModule(eiModules.find { it.id == modules[2].id }!!, modules[2])
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

    private fun assertModule(expected: Module, actual: Module) {
        assertEquals(expected.id, actual.id)
        assertEquals(expected.type, actual.type)
        assertEquals(expected.artifacts?.size ?: 0, actual.artifacts?.size ?: 0)
        assertEquals(expected.dependencies?.size ?: 0, actual.dependencies?.size ?: 0)
        expected.dependencies?.forEach { expectedDependency ->
            val actualDependency = actual.dependencies?.find { it.id == expectedDependency.id }
            assertTrue(actualDependency != null, "Expected dependency with id ${expectedDependency.id} not found in actual dependencies")
            assertEquals(expectedDependency.id, actualDependency!!.id)
        }
    }

}
