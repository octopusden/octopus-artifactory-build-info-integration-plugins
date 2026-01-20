package org.octopusden.octopus.artifactory.npm.core.configuration

data class BuildInfoConfiguration(
    val buildName: String,
    val buildNumber: String,
    val npmBuildNameSuffix: String,
    val npmRepository: String,
    val cleanupNpmBuildInfo: Boolean
) {
    val npmBuildName: String
        get() = "${buildName}${npmBuildNameSuffix}"
}