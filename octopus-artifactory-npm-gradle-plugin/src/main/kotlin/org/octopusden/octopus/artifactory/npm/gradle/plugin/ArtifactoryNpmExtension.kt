package org.octopusden.octopus.artifactory.npm.gradle.plugin

import org.gradle.api.Project
import javax.inject.Inject

abstract class ArtifactoryNpmExtension @Inject constructor(
    project: Project
) {
    private val settings: ArtifactoryNpmSettings = project.objects.newInstance(ArtifactoryNpmSettings::class.java)

    internal val taskConfiguration: ArtifactoryNpmTaskConfiguration =
        ArtifactoryNpmTaskConfiguration(project, settings)
}