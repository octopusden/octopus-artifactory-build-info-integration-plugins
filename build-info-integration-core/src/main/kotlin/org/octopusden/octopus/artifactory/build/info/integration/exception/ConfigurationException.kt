package org.octopusden.octopus.artifactory.build.info.integration.exception

class ConfigurationException(
    message: String,
    cause: Throwable? = null
) : CoreException(message, cause)