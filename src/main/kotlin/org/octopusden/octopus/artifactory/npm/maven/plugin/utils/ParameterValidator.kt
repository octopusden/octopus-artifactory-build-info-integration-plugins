package org.octopusden.octopus.artifactory.npm.maven.plugin.utils

import org.apache.maven.plugin.MojoExecutionException
import java.io.File
import java.net.URI

object ParameterValidator {
    fun validateArtifactoryUrl(url: String) {
        try {
            val uri = URI(url)
            if (uri.scheme == null || uri.host == null) {
                throw MojoExecutionException("Invalid artifactory URL: $url. Must be a valid URL with scheme and host.")
            }
        } catch (e: Exception) {
            throw MojoExecutionException("Invalid artifactory URL: $url", e)
        }
    }

    fun validatePackageJsonFile(file: File) {
        if (!file.exists()) {
            throw MojoExecutionException("package.json file not found at path: ${file.absolutePath}")
        }
        if (!file.isFile) {
            throw MojoExecutionException("Path exists but is not a file: ${file.absolutePath}")
        }
    }
}