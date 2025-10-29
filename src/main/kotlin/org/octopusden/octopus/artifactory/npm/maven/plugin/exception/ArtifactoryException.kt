package org.octopusden.octopus.artifactory.npm.maven.plugin.exception

class ArtifactoryException(
    message: String,
    cause: Throwable? = null
) : PluginException(message, cause)