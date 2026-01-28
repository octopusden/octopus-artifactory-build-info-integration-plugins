pluginManagement {
    plugins {
        kotlin("jvm") version extra["kotlin.version"] as String
        id("io.github.gradle-nexus.publish-plugin") version extra["nexus-publish.version"] as String
        id("com.jfrog.artifactory") version extra["jfrog-artifactory.version"] as String
        id("org.octopusden.octopus.oc-template") version extra["octopus-oc-template.version"] as String
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
