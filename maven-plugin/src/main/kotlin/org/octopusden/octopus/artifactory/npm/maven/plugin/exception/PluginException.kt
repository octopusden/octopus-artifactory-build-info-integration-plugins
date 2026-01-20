package org.octopusden.octopus.artifactory.npm.maven.plugin.exception

import org.apache.maven.plugin.MojoFailureException

sealed class PluginException(
    message: String,
    cause: Throwable? = null
) : MojoFailureException(message, cause)