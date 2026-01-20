package org.octopusden.octopus.artifactory.npm.core.configuration

data class ArtifactoryConfiguration (
    private val rawUrl: String,
    val username: String?,
    val password: String?,
    val token: String?
) {
    val url: String = rawUrl.trimEnd('/').let {
        if (it.endsWith("/artifactory")) it else "$it/artifactory"
    }
}