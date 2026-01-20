package org.octopusden.octopus.artifactory.npm.core.exception

class ConfigurationException(
    message: String,
    cause: Throwable? = null
) : CoreException(message, cause)