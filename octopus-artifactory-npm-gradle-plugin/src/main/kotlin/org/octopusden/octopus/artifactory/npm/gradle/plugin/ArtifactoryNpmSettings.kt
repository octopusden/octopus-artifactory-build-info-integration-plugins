package org.octopusden.octopus.artifactory.npm.gradle.plugin

import org.gradle.api.provider.Property

abstract class ArtifactoryNpmSettings {
    abstract val buildName: Property<String>
    abstract val buildNumber: Property<String>
    abstract val npmBuildNameSuffix: Property<String>
    abstract val npmRepository: Property<String>
    abstract val packageJsonPath: Property<String>
    abstract val skip: Property<Boolean>
    abstract val cleanupNpmBuildInfo: Property<Boolean>

    init {
        npmRepository.convention("npm")
        npmBuildNameSuffix.convention("_npm")
        packageJsonPath.convention("")
        skip.convention(false)
        cleanupNpmBuildInfo.convention(true)
    }
}