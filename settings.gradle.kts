plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "octopus-artifactory-integration"

include("build-info-integration-core")
include("octopus-artifactory-npm-maven-plugin")
