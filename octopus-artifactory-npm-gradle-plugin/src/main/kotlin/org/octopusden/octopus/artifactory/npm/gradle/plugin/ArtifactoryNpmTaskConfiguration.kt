package org.octopusden.octopus.artifactory.npm.gradle.plugin

import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.octopusden.octopus.artifactory.npm.gradle.plugin.tasks.IntegrateNpmBuildInfoTask
import java.io.File

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

            task.npmRepository.set(settings.npmRepository)
            task.buildName.set(settings.buildName)
            task.buildNumber.set(settings.buildNumber)
            task.npmBuildNameSuffix.set(settings.npmBuildNameSuffix)
            task.packageJsonPath.set(settings.packageJsonPath)
            task.cleanupNpmBuildInfo.set(settings.cleanupNpmBuildInfo)
            task.skipWaitForXrayScan.set(settings.skipWaitForXrayScan)
        }
    }

    fun configureBuildFinishedHook() {
        project.gradle.buildFinished { result ->

            if (result.failure != null) {
                project.logger.info("Build failed, skipping NPM build info integration")
                return@buildFinished
            }

            if (!settings.buildName.isPresent && project.findProperty("buildInfo.build.name") == null) {
                project.logger.info("Skipping NPM build info integration: buildName not configured")
                return@buildFinished
            }

            if (!settings.buildNumber.isPresent && project.findProperty("buildInfo.build.number") == null) {
                project.logger.info("Skipping NPM build info integration: buildNumber not configured")
                return@buildFinished
            }

            if (settings.skip.get()) {
                project.logger.info("Skipping NPM build info integration (artifactoryNpm.skip=true)")
                return@buildFinished
            }

            project.logger.lifecycle("Build finished successfully, integrating NPM build info...")

            if (!isPackageJsonAvailable()) {
                project.logger.info("Skipping NPM build info integration: package.json not found")
                return@buildFinished
            }

            try {
                integrationTask?.get()?.integrateNpmBuildInfo()
            } catch (e: Exception) {
                project.logger.error("Failed to integrate NPM build info: ${e.message}", e)
                project.logger.warn("NPM build info integration failed, but build will continue")
            }
        }
    }

    private fun isPackageJsonAvailable(): Boolean {
        val path = settings.packageJsonPath.get()
        val packageJsonDir = if (path.isEmpty()) {
            project.projectDir
        } else {
            File(project.projectDir, path)
        }
        val packageJsonFile = File(packageJsonDir, "package.json")
        return packageJsonFile.exists() && packageJsonFile.isFile
    }
}