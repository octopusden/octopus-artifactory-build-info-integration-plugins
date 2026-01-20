package org.octopusden.octopus.artifactory.npm.core.exception

sealed class CoreException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)