package org.octopusden.octopus.artifactory.integration.plugins.ft.runner

import com.platformlib.process.api.ProcessInstance
import com.platformlib.process.builder.ProcessBuilder
import com.platformlib.process.factory.ProcessBuilders
import com.platformlib.process.local.specification.LocalProcessSpec
import java.lang.IllegalArgumentException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

open class TestGradleDSL {
    lateinit var testProjectName: String
    var additionalArguments: Array<String> = arrayOf()
    var additionalEnvVariables: Map<String, String> = mapOf()
    var tasks: Array<String> = arrayOf()
}

fun gradleProcessInstance(init: TestGradleDSL.() -> Unit): Pair<ProcessInstance, Path> {
    val testGradleDSL = TestGradleDSL()
    init.invoke(testGradleDSL)

    val projectPath = getResourcePath("/${testGradleDSL.testProjectName}", "Test project")
    if (!Files.isDirectory(projectPath)) {
        throw IllegalArgumentException("The specified project '${testGradleDSL.testProjectName}' hasn't been found at $projectPath")
    }

    return Pair(ProcessBuilders
        .newProcessBuilder<ProcessBuilder>(LocalProcessSpec.LOCAL_COMMAND)
        .envVariables(mapOf(
            "JAVA_HOME" to System.getProperty("java.home")
        ) + testGradleDSL.additionalEnvVariables)
        .redirectStandardOutput(System.out)
        .redirectStandardError(System.err)
        .defaultExtensionMapping()
        .workDirectory(projectPath)
        .processInstance { processInstanceConfiguration -> processInstanceConfiguration.unlimited() }
        .commandAndArguments("$projectPath/gradlew", "--no-daemon")
        .build()
        .execute(
            *(listOf(
                "-Poctopus-artifactory-npm-gradle-plugin.version=${System.getProperty("octopusArtifactoryIntegrationPluginVersion")}",
            ) + testGradleDSL.tasks + testGradleDSL.additionalArguments).toTypedArray())
        .toCompletableFuture()
        .join(), projectPath)
}

private fun getResourcePath(path: String, description: String): Path {
    val resource = TestGradleDSL::class.java.getResource(path)
        ?: error("$description '$path' not found in resources")
    return Paths.get(resource.toURI())
}
