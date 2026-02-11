package org.octopusden.octopus.artifactory.build.info.integration.exception

sealed class CoreException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)