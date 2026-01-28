package org.octopusden.octopus.artifactory.integration.plugins.ft.runner

import java.nio.file.Path

enum class BuildTool(
    val commandResolver: (Path) -> String,
    val propertyPrefix: String
) {
    GRADLE(
        commandResolver = { projectPath -> "$projectPath/gradlew" },
        propertyPrefix = "-P"
    ),
    MAVEN(
        commandResolver = { _ ->
            System.getenv("MAVEN_HOME")?.let { "$it/bin/mvn" }
                ?: System.getenv("M2_HOME")?.let { "$it/bin/mvn" }
                ?: "mvn"
        },
        propertyPrefix = "-D"
    );
    fun buildPluginVersionProperty(version: String): String {
        val pluginName = when (this) {
            GRADLE -> "octopus-artifactory-npm-gradle-plugin"
            MAVEN -> "octopus-artifactory-npm-maven-plugin"
        }
        return "${propertyPrefix}${pluginName}.version=$version"
    }
}