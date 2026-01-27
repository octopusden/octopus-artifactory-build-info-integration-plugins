package org.octopusden.octopus.artifactory.integration.plugins.ft.runner

import com.platformlib.process.api.ProcessInstance
import com.platformlib.process.builder.ProcessBuilder
import com.platformlib.process.factory.ProcessBuilders
import com.platformlib.process.local.specification.LocalProcessSpec
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

fun gradleProcessInstance(init: BuildToolConfig.() -> Unit): ProcessInstance {
    return buildToolProcessInstance {
        buildTool = BuildTool.GRADLE
        defaultArguments = arrayOf("--no-daemon")
        init()
    }
}

fun mavenProcessInstance(init: BuildToolConfig.() -> Unit): ProcessInstance {
    return buildToolProcessInstance {
        buildTool = BuildTool.MAVEN
        init()
    }
}

private fun buildToolProcessInstance(init: BuildToolConfig.() -> Unit): ProcessInstance {
    val config = BuildToolConfig().apply(init)

    val projectPath = getResourcePath("/${config.testProjectName}")
    if (!Files.isDirectory(projectPath)) {
        throw IllegalArgumentException(
            "The specified project '${config.testProjectName}' hasn't been found at $projectPath"
        )
    }

    val command = config.buildTool.commandResolver(projectPath)
    val pluginVersion = System.getProperty("octopusArtifactoryIntegrationPluginVersion")
    val pluginVersionProperty = config.buildTool.buildPluginVersionProperty(pluginVersion)

    val defaultEnv = mapOf("JAVA_HOME" to System.getProperty("java.home"))
    val mergedEnv = defaultEnv + config.envVariables

    val arguments = buildList {
        add(pluginVersionProperty)
        addAll(config.tasks)
        addAll(config.additionalArguments)
    }

    return ProcessBuilders
        .newProcessBuilder<ProcessBuilder>(LocalProcessSpec.LOCAL_COMMAND)
        .envVariables(mergedEnv)
        .redirectStandardOutput(System.out)
        .redirectStandardError(System.err)
        .defaultExtensionMapping()
        .workDirectory(projectPath)
        .processInstance { it.unlimited() }
        .commandAndArguments(command, *config.defaultArguments)
        .build()
        .execute(*arguments.toTypedArray())
        .toCompletableFuture()
        .join()
}

private fun getResourcePath(path: String): Path {
    val resource = BuildToolConfig::class.java.getResource(path)
        ?: error("'$path' not found in resources")
    return Paths.get(resource.toURI())
}

