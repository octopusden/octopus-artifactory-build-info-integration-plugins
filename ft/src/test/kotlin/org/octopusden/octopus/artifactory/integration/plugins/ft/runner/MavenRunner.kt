package org.octopusden.octopus.artifactory.integration.plugins.ft.runner

import com.platformlib.process.api.ProcessInstance
import com.platformlib.process.builder.ProcessBuilder
import com.platformlib.process.factory.ProcessBuilders
import com.platformlib.process.local.specification.LocalProcessSpec
import java.lang.IllegalArgumentException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

open class TestMavenDSL {
    lateinit var testProjectName: String
    var additionalArguments: Array<String> = arrayOf()
    var tasks: Array<String> = arrayOf()
}

fun mavenProcessInstance(init: TestMavenDSL.() -> Unit): ProcessInstance {
    val testGradleDSL = TestMavenDSL()
    init.invoke(testGradleDSL)

    val projectPath = getResourcePath("/${testGradleDSL.testProjectName}")
    if (!Files.isDirectory(projectPath)) {
        throw IllegalArgumentException("The specified project '${testGradleDSL.testProjectName}' hasn't been found at $projectPath")
    }

    return ProcessBuilders
        .newProcessBuilder<ProcessBuilder>(LocalProcessSpec.LOCAL_COMMAND)
        .envVariables(mapOf(
            "JAVA_HOME" to System.getProperty("java.home")
        ))
        .redirectStandardOutput(System.out)
        .redirectStandardError(System.err)
        .defaultExtensionMapping()
        .workDirectory(projectPath)
        .processInstance { processInstanceConfiguration -> processInstanceConfiguration.unlimited() }
        .commandAndArguments("/Users/aksetiyawan/MacOS/MAVEN/LATEST/bin/mvn")
        .build()
        .execute(
            *(listOf(
                "-Doctopus-artifactory-npm-maven-plugin.version=${System.getProperty("octopusArtifactoryIntegrationPluginVersion")}",
            ) + testGradleDSL.tasks + testGradleDSL.additionalArguments).toTypedArray())
        .toCompletableFuture()
        .join()
}

private fun getResourcePath(path: String): Path {
    val resource = TestMavenDSL::class.java.getResource(path)
        ?: error("'$path' not found in resources")
    return Paths.get(resource.toURI())
}
