package org.octopusden.octopus.artifactory.npm.gradle.plugin.tasks

import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class IntegrateNpmBuildInfoTask : BaseNpmBuildInfoTask() {

    @get:Input
    abstract val packageJsonPath: Property<String>

    @TaskAction
    fun execute() {
        integrateNpmBuildInfo()
    }

    internal fun integrateNpmBuildInfo() {
        try {
            initializeServices()

            val buildInfoConfiguration = createBuildInfoConfiguration()
            val artifactoryConfiguration = createArtifactoryConfiguration()

            integrationService.generateNpmBuildInfo(
                packageJsonPath().absolutePath,
                buildInfoConfiguration,
                artifactoryConfiguration
            )

            integrationService.integrateNpmBuildInfo(buildInfoConfiguration, skipWaitForXrayScan.get())
            logger.lifecycle("NPM build info integrated successfully")
        } catch (e: Exception) {
            logger.error("Failed to integrate NPM build info: ${e.message}", e)
            throw GradleException("Failed to integrate NPM build info", e)
        }
    }

    private fun packageJsonPath(): File {
        val path = packageJsonPath.get()
        val dir = if (path.isEmpty()) project.projectDir else File(project.projectDir, path)
        if (!dir.isDirectory) {
            throw GradleException("packageJsonPath must be a directory: ${dir.absolutePath}")
        }
        return dir
    }

}