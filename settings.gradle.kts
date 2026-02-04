pluginManagement {
    val pluginVersion = object {
        val kotlin = providers.gradleProperty("kotlin.version")
        val nexusPublish = providers.gradleProperty("nexus-publish.version")
        val jfrogArtifactory = providers.gradleProperty("jfrog-artifactory.version")
        val ocTemplate = providers.gradleProperty("octopus-oc-template.version")
    }
    plugins {
        kotlin("jvm") version pluginVersion.kotlin.get()
        id("io.github.gradle-nexus.publish-plugin") version pluginVersion.nexusPublish.get()
        id("com.jfrog.artifactory") version pluginVersion.jfrogArtifactory.get()
        id("org.octopusden.octopus.oc-template") version pluginVersion.ocTemplate.get()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "octopus-artifactory-integration"

include("build-info-integration-core")
include("octopus-artifactory-npm-maven-plugin")
include("octopus-artifactory-npm-gradle-plugin")
include("ft")
