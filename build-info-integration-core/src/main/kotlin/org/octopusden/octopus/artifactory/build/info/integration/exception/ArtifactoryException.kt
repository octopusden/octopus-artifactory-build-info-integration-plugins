package org.octopusden.octopus.artifactory.build.info.integration.exception

class ArtifactoryException(
    message: String,
    cause: Throwable? = null
) : CoreException(message, cause)