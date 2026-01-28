package org.octopusden.octopus.artifactory.npm.gradle.plugin

import org.gradle.api.Action
import org.gradle.api.Project
import javax.inject.Inject

abstract class ArtifactoryNpmExtension @Inject constructor(
    project: Project
) {
    private val configuration: ArtifactoryNpmSettings = project.objects.newInstance(ArtifactoryNpmSettings::class.java)

    internal val taskConfiguration: ArtifactoryNpmTaskConfiguration =
        ArtifactoryNpmTaskConfiguration(project, configuration)

    fun configuration(action: Action<ArtifactoryNpmSettings>) {
        action.execute(configuration)
    }
}