package org.octopusden.octopus.artifactory.npm.gradle.plugin

import org.gradle.api.Action
import org.gradle.api.Project
import javax.inject.Inject

abstract class ArtifactoryNpmExtension @Inject constructor(
    private val project: Project
) {
    internal val configuration: ArtifactoryNpmSettings = project.objects.newInstance(ArtifactoryNpmSettings::class.java)

    internal val taskConfiguration: ArtifactoryNpmTaskConfiguration =
        ArtifactoryNpmTaskConfiguration(project, configuration)

    fun configuration(action: Action<ArtifactoryNpmSettings>) {
        action.execute(configuration)
    }
}