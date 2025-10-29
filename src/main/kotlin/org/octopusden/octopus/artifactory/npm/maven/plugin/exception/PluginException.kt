package org.octopusden.octopus.artifactory.npm.maven.plugin.exception

sealed class PluginException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)