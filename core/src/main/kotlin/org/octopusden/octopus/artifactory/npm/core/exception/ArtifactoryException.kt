package org.octopusden.octopus.artifactory.npm.core.exception

class ArtifactoryException(
    message: String,
    cause: Throwable? = null
) : CoreException(message, cause)