package org.octopusden.octopus.artifactory.npm.gradle.plugin.tasks

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.octopusden.octopus.artifactory.build.info.integration.dto.DependencyVersion
import java.io.File

abstract class IntegrateNpmBuildInfoTask : BaseNpmBuildInfoTask() {
    @get:Input
    abstract val packageJsonPath: Property<String>

    @get:Input
    @get:Optional
    abstract val dependenciesFilePath: Property<String>

    @TaskAction
    fun execute() {
        integrateNpmBuildInfo()
    }

    internal fun integrateNpmBuildInfo() {
        try {
            val packageJsonDir = resolvePackageJsonDir()
            val dependencies = resolveDependencies()
            if (packageJsonDir == null && dependencies.isEmpty()) {
                logger.lifecycle("No package.json found and no dependencies resolved, skipping NPM build info integration")
                return
            }

            initializeServices()

            val buildInfoConfiguration = createBuildInfoConfiguration()
            val artifactoryConfiguration = createArtifactoryConfiguration()

            if (packageJsonDir != null) {
                integrationService.generateNpmBuildInfo(
                    packageJsonDir.absolutePath,
                    buildInfoConfiguration,
                    artifactoryConfiguration,
                )
            }

            integrationService.integrateNpmBuildInfo(
                buildInfoConfiguration,
                dependencies,
                packageJsonDir == null,
                skipWaitForXrayScan.get(),
            )
            logger.lifecycle("NPM build info integrated successfully")
        } catch (e: Exception) {
            logger.error("Failed to integrate NPM build info: ${e.message}", e)
            throw GradleException("Failed to integrate NPM build info", e)
        }
    }

    private fun resolvePackageJsonDir(): File? {
        val path = packageJsonPath.get()
        val isExplicitlyConfigured = path.isNotEmpty()
        val dir = if (isExplicitlyConfigured) File(project.projectDir, path) else project.projectDir
        if (!dir.isDirectory) {
            if (isExplicitlyConfigured) {
                throw GradleException("packageJsonPath '$path' is not a valid directory: ${dir.absolutePath}")
            }
            return null
        }
        val packageJsonFile = File(dir, "package.json")
        if (!packageJsonFile.exists() || !packageJsonFile.isFile) {
            if (isExplicitlyConfigured) {
                throw GradleException("package.json not found in configured packageJsonPath: ${dir.absolutePath}")
            }
            logger.info("package.json not found in ${dir.absolutePath}, skipping NPM build info generation")
            return null
        }
        return dir
    }

    private fun resolveDependencies(): List<DependencyVersion> {
        val dependenciesFile = getDependenciesFile() ?: return emptyList()
        val mapper = jacksonObjectMapper()
        return mapper.readValue(dependenciesFile, Array<DependencyVersion>::class.java).toList()
    }

    private fun getDependenciesFile(): File? {
        val filePath = (project.findProperty("dependenciesFilePath") as? String)?.takeIf { it.isNotBlank() }
            ?: dependenciesFilePath.orNull?.takeIf { it.isNotBlank() }
        if (filePath == null) {
            logger.info("dependenciesFilePath property is not set, skipping dependencies resolution")
            return null
        }
        val dependenciesFile = File(filePath).let { file ->
            if (file.isAbsolute) {
                file
            } else {
                File(project.projectDir, filePath)
            }
        }
        if (!dependenciesFile.exists() || !dependenciesFile.isFile) {
            logger.warn(
                "Dependencies file does not exist or is not a file: ${dependenciesFile.absolutePath}, skipping dependencies resolution",
            )
            return null
        }
        return dependenciesFile
    }
}
