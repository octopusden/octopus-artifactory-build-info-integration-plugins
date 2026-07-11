pluginManagement {
    val pluginVersion = object {
        val kotlin = providers.gradleProperty("kotlin.version")
        val nexusPublish = providers.gradleProperty("nexus-publish.version")
        val jfrogArtifactory = providers.gradleProperty("jfrog-artifactory.version")
        val ocTemplate = providers.gradleProperty("octopus-oc-template.version")
        val octopusQuality = providers.gradleProperty("octopus-quality.version")
        val detekt = providers.gradleProperty("detekt.version")
        val ktlint = providers.gradleProperty("ktlint-gradle.version")
    }
    plugins {
        kotlin("jvm") version pluginVersion.kotlin.get()
        id("io.github.gradle-nexus.publish-plugin") version pluginVersion.nexusPublish.get()
        id("com.jfrog.artifactory") version pluginVersion.jfrogArtifactory.get()
        id("org.octopusden.octopus.oc-template") version pluginVersion.ocTemplate.get()
        // Octopus quality-gates convention plugin + Kotlin linters (see build.gradle.kts).
        id("org.octopusden.octopus-quality") version pluginVersion.octopusQuality.get()
        id("io.gitlab.arturbosch.detekt") version pluginVersion.detekt.get()
        id("org.jlleitschuh.gradle.ktlint") version pluginVersion.ktlint.get()
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
