package org.octopusden.octopus.artifactory.build.info.integration.service

import org.octopusden.octopus.artifactory.build.info.integration.configuration.ArtifactoryConfiguration
import org.octopusden.octopus.artifactory.build.info.integration.configuration.BuildInfoConfiguration

interface NpmBuildInfoIntegrationService {
    fun generateNpmBuildInfo(packageJsonPath: String, buildInfoConfig: BuildInfoConfiguration, artifactoryConfig: ArtifactoryConfiguration)
    fun integrateNpmBuildInfo(buildInfoConfig: BuildInfoConfiguration, skipWaitForXrayScan: Boolean)
}