package org.octopusden.octopus.artifactory.integration.plugins.ft.runner

import java.nio.file.Path

enum class BuildTool(
    val commandResolver: (Path) -> String,
    val propertyPrefix: String,
    val stagingProperty: String
) {
    GRADLE(
        commandResolver = { projectPath -> "$projectPath/gradlew" },
        propertyPrefix = "-P",
        stagingProperty = "-Puse_dev_repository=plugins"
    ),
    MAVEN(
        commandResolver = { _ ->
            System.getenv("MAVEN_HOME")?.let { "$it/bin/mvn" }
                ?: System.getenv("M2_HOME")?.let { "$it/bin/mvn" }
                ?: "mvn"
        },
        propertyPrefix = "-D",
        stagingProperty = "-Pstaging"
    );
    fun buildPluginVersionProperty(version: String): String {
        val pluginName = when (this) {
            GRADLE -> "octopus-artifactory-npm-gradle-plugin"
            MAVEN -> "octopus-artifactory-npm-maven-plugin"
        }
        return "${propertyPrefix}${pluginName}.version=$version"
    }
}