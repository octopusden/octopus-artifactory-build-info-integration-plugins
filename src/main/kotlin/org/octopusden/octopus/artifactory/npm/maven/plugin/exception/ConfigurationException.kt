package org.octopusden.octopus.artifactory.npm.maven.plugin.exception

class ConfigurationException(
    message: String,
    cause: Throwable? = null
) : PluginException(message, cause)