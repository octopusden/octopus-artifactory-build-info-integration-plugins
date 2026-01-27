package org.octopusden.octopus.artifactory.npm.gradle.plugin

import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.octopusden.octopus.artifactory.npm.gradle.plugin.tasks.IntegrateNpmBuildInfoTask

class ArtifactoryNpmTaskConfiguration(
    private val project: Project,
    private val settings: ArtifactoryNpmSettings
) {
    private var integrationTask: TaskProvider<IntegrateNpmBuildInfoTask>? = null

    fun registerTasks() {
        integrationTask = project.tasks.register(
            "integrateNpmBuildInfo",
            IntegrateNpmBuildInfoTask::class.java
        ) { task ->
            task.group = "artifactory"
            task.description = "Integrates NPM dependencies into Artifactory build info"

            task.artifactoryUrl.set(settings.artifactoryUrl)
            task.artifactoryAccessToken.set(settings.artifactoryAccessToken)
            task.artifactoryUsername.set(settings.artifactoryUsername)
            task.artifactoryPassword.set(settings.artifactoryPassword)
            task.npmRepository.set(settings.npmRepository)
            task.buildName.set(settings.buildName)
            task.buildNumber.set(settings.buildNumber)
            task.npmBuildNameSuffix.set(settings.npmBuildNameSuffix)
            task.packageJsonPath.set(settings.packageJsonPath)
            task.cleanupNpmBuildInfo.set(settings.cleanupNpmBuildInfo)
        }
    }

    fun configureBuildFinishedHook() {
        project.gradle.buildFinished { result ->
            if (settings.skip.get()) {
                project.logger.info("Skipping NPM build info integration (artifactoryNpm.skip=true)")
                return@buildFinished
            }

            if (result.failure == null) {
                project.logger.lifecycle("Build finished successfully, integrating NPM build info...")
                try {
                    integrationTask?.get()?.integrateNpmBuildInfo()
                } catch (e: Exception) {
                    project.logger.error("Failed to integrate NPM build info: ${e.message}", e)
                    throw e
                }
            } else {
                project.logger.info("Build failed, skipping NPM build info integration")
            }
        }
    }
}