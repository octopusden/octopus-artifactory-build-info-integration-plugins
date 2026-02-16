package org.octopusden.octopus.artifactory.npm.gradle.plugin.tasks

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.octopusden.octopus.artifactory.build.info.integration.dto.DependencyVersion
import java.io.File

abstract class IntegrateNpmBuildInfoTask : BaseNpmBuildInfoTask() {

    @get:Input
    abstract val packageJsonPath: Property<String>

    @get:Input
    abstract val dependenciesFilePath: Property<String>

    @TaskAction
    fun execute() {
        integrateNpmBuildInfo()
    }

    internal fun integrateNpmBuildInfo() {
        try {
            initializeServices()

            val buildInfoConfiguration = createBuildInfoConfiguration()
            val artifactoryConfiguration = createArtifactoryConfiguration()

            val packageJsonDir = getPackageJsonPath()
            val packageJsonAvailable = isPackageJsonFileAvailable(packageJsonDir)

            if (packageJsonAvailable) {
                integrationService.generateNpmBuildInfo(
                    packageJsonDir.absolutePath,
                    buildInfoConfiguration,
                    artifactoryConfiguration
                )
            }

            integrationService.integrateNpmBuildInfo(buildInfoConfiguration, resolveDependencies(), !packageJsonAvailable, skipWaitForXrayScan.get())
            logger.lifecycle("NPM build info integrated successfully")
        } catch (e: Exception) {
            logger.error("Failed to integrate NPM build info: ${e.message}", e)
            throw GradleException("Failed to integrate NPM build info", e)
        }
    }

    private fun getPackageJsonPath(): File {
        val path = packageJsonPath.get()
        val dir = if (path.isEmpty()) project.projectDir else File(project.projectDir, path)
        if (!dir.isDirectory) {
            throw GradleException("packageJsonPath must be a directory: ${dir.absolutePath}")
        }
        return dir
    }

    private fun isPackageJsonFileAvailable(packageJsonPath: File): Boolean {
        val packageJsonFile = File(packageJsonPath, "package.json")
        return packageJsonFile.exists() && packageJsonFile.isFile
    }

    private fun resolveDependencies(): List<DependencyVersion> {
        val dependenciesFile = getDependenciesFile() ?: return emptyList()
        val mapper = jacksonObjectMapper()
        return try {
            mapper.readValue(dependenciesFile, Array<DependencyVersion>::class.java).toList()
        } catch (e: Exception) {
            logger.warn("Failed to read dependencies from file: ${dependenciesFile.absolutePath}, skipping dependencies resolution. Error: ${e.message}")
            emptyList()
        }
    }

    private fun getDependenciesFile(): File? {
        var dependenciesFile: File
        try {
            getProjectOrSettingsProperty("dependenciesFilePath", dependenciesFilePath)
        } catch (_: GradleException) {
            logger.warn("dependenciesFilePath property is not set, skipping dependencies resolution")
            return null
        }.let {
            dependenciesFile = File(project.projectDir, it)
        }
        if (!dependenciesFile.exists() || !dependenciesFile.isFile) {
            logger.warn("Dependencies file does not exist or is not a file: ${dependenciesFile.absolutePath}, skipping dependencies resolution")
            return null
        }
        return dependenciesFile
    }

}