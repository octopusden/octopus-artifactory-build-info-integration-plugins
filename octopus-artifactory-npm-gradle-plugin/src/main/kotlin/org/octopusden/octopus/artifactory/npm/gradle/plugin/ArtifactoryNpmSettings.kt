package org.octopusden.octopus.artifactory.npm.gradle.plugin

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

abstract class ArtifactoryNpmSettings {
    @get:Input
    abstract val artifactoryUrl: Property<String>

    @get:Input
    @get:Optional
    abstract val artifactoryAccessToken: Property<String>

    @get:Input
    @get:Optional
    abstract val artifactoryUsername: Property<String>

    @get:Input
    @get:Optional
    abstract val artifactoryPassword: Property<String>

    @get:Input
    abstract val npmRepository: Property<String>

    @get:Input
    abstract val buildName: Property<String>

    @get:Input
    abstract val buildNumber: Property<String>

    @get:Input
    abstract val npmBuildNameSuffix: Property<String>

    @get:Input
    abstract val packageJsonPath: Property<String>

    @get:Input
    abstract val skip: Property<Boolean>

    @get:Input
    abstract val cleanupNpmBuildInfo: Property<Boolean>

    init {
        npmRepository.convention("npm")
        npmBuildNameSuffix.convention("_npm")
        packageJsonPath.convention("")
        skip.convention(false)
        cleanupNpmBuildInfo.convention(true)
    }
}