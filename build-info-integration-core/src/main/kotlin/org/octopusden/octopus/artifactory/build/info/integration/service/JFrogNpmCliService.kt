package org.octopusden.octopus.artifactory.build.info.integration.service

import org.octopusden.octopus.artifactory.build.info.integration.configuration.ArtifactoryConfiguration

interface JFrogNpmCliService {
    fun configureNpmRepository(packageJsonPath: String, npmRepository: String)
    fun installNpmDependencies(packageJsonPath: String, buildName: String, buildNumber: String)
    fun publishNpmBuildInfo(packageJsonPath: String, buildName: String, buildNumber: String, artifactoryConfig: ArtifactoryConfiguration)
    fun isJFrogCliAvailable(): Boolean
}