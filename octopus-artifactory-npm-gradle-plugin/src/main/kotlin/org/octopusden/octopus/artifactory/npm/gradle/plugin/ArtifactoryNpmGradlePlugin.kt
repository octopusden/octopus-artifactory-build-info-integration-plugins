package org.octopusden.octopus.artifactory.npm.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class ArtifactoryNpmGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create(
            "artifactoryNpm",
            ArtifactoryNpmExtension::class.java,
            project
        )
        project.afterEvaluate {
            extension.taskConfiguration.registerTasks()
            extension.taskConfiguration.configureBuildFinishedHook()
        }
    }
}