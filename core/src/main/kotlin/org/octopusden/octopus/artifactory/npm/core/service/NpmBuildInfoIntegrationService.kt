package org.octopusden.octopus.artifactory.npm.core.service

import org.octopusden.octopus.artifactory.npm.core.configuration.ArtifactoryConfiguration
import org.octopusden.octopus.artifactory.npm.core.configuration.BuildInfoConfiguration

interface NpmBuildInfoIntegrationService {
    fun generateNpmBuildInfo(packageJsonPath: String, buildInfoConfig: BuildInfoConfiguration, artifactoryConfig: ArtifactoryConfiguration)
    fun integrateNpmBuildInfo(buildInfoConfig: BuildInfoConfiguration)
}