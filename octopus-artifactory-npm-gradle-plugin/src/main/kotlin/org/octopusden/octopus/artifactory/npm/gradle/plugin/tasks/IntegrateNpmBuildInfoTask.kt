package org.octopusden.octopus.artifactory.npm.gradle.plugin.tasks

import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

abstract class IntegrateNpmBuildInfoTask : BaseNpmBuildInfoTask() {

    @TaskAction
    fun execute() {
        integrateNpmBuildInfo()
    }

    internal fun integrateNpmBuildInfo() {
        try {
            validateParameters()
            initializeServices()

            val buildInfoConfiguration = createBuildInfoConfiguration()
            val artifactoryConfiguration = createArtifactoryConfiguration()

            integrationService.generateNpmBuildInfo(
                getPackageJsonFile().absolutePath,
                buildInfoConfiguration,
                artifactoryConfiguration
            )

            integrationService.integrateNpmBuildInfo(buildInfoConfiguration)
            logger.lifecycle("NPM build info integrated successfully")
        } catch (e: Exception) {
            logger.error("Failed to integrate NPM build info: ${e.message}", e)
            throw GradleException("Failed to integrate NPM build info", e)
        }
    }
}