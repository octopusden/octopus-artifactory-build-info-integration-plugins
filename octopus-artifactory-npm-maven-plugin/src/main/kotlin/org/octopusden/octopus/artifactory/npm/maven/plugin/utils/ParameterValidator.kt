package org.octopusden.octopus.artifactory.npm.maven.plugin.utils

import org.octopusden.octopus.artifactory.npm.maven.plugin.exception.ParameterValidationException
import java.io.File
import java.net.URI

object ParameterValidator {

    private const val ARTIFACTORY_URL_PARAMETER = "artifactoryUrl"
    private const val PACKAGE_JSON_PATH_PARAMETER = "packageJsonPath"

    fun validateArtifactoryUrl(url: String) {
        try {
            val uri = URI(url)
            if (uri.scheme == null || uri.host == null) {
                throw ParameterValidationException(
                    "Invalid artifactory URL. Must be a valid URL with scheme and host.",
                    field = ARTIFACTORY_URL_PARAMETER,
                    value = url
                )
            }
        } catch (e: ParameterValidationException) {
            throw e
        } catch (e: Exception) {
            throw ParameterValidationException(
                "Invalid artifactory URL format",
                field = ARTIFACTORY_URL_PARAMETER,
                value = url,
                cause = e
            )
        }
    }

    fun validatePackageJsonPath(folderPath: File) {
        when {
            !folderPath.exists() -> throw ParameterValidationException(
                "Directory not found",
                field = PACKAGE_JSON_PATH_PARAMETER,
                value = folderPath.absolutePath
            )
            !folderPath.isDirectory -> throw ParameterValidationException(
                "Path is not a directory",
                field = PACKAGE_JSON_PATH_PARAMETER,
                value = folderPath.absolutePath
            )
        }

        val packageJsonFile = File(folderPath, "package.json")
        if (!packageJsonFile.exists()) {
            throw ParameterValidationException(
                "package.json not found in directory",
                field = PACKAGE_JSON_PATH_PARAMETER,
                value = folderPath.absolutePath
            )
        }
    }
}