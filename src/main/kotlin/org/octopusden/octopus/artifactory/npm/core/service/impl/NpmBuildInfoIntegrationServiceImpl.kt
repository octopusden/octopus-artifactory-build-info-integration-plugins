package org.octopusden.octopus.artifactory.npm.core.service.impl

import org.octopusden.octopus.artifactory.npm.core.configuration.ArtifactoryConfiguration
import org.octopusden.octopus.artifactory.npm.core.configuration.BuildInfoConfiguration
import org.octopusden.octopus.artifactory.npm.core.exception.ConfigurationException
import org.octopusden.octopus.artifactory.npm.core.service.ArtifactoryBuildInfoService
import org.octopusden.octopus.artifactory.npm.core.service.JFrogNpmCliService
import org.octopusden.octopus.artifactory.npm.core.service.NpmBuildInfoIntegrationService
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

class NpmBuildInfoIntegrationServiceImpl(
    private val jfrogNpmCliService: JFrogNpmCliService,
    private val buildInfoService: ArtifactoryBuildInfoService
) : NpmBuildInfoIntegrationService {
    
    private val logger = LoggerFactory.getLogger(NpmBuildInfoIntegrationServiceImpl::class.java)

    override fun generateNpmBuildInfo(
        packageJsonPath: String,
        buildInfoConfig: BuildInfoConfiguration,
        artifactoryConfig: ArtifactoryConfiguration
    ) {
        logger.info("Generate NPM build info for build ${buildInfoConfig.buildName}:${buildInfoConfig.buildNumber}")

        if (!jfrogNpmCliService.isJFrogCliAvailable()) {
            throw ConfigurationException("JFrog CLI is not available or not properly configured")
        }

        jfrogNpmCliService.configureNpmRepository(packageJsonPath, buildInfoConfig.npmRepository)
        jfrogNpmCliService.installNpmDependencies(packageJsonPath, buildInfoConfig.npmBuildName, buildInfoConfig.buildNumber)
        jfrogNpmCliService.publishNpmBuildInfo(packageJsonPath, buildInfoConfig.npmBuildName, buildInfoConfig.buildNumber, artifactoryConfig)
    }

    override fun integrateNpmBuildInfo(buildInfoConfig: BuildInfoConfiguration) {
        logger.info("Integrate NPM build info into Maven build info for build ${buildInfoConfig.buildName}:${buildInfoConfig.buildNumber}")

        val mavenBuildInfo = buildInfoService.getBuildInfo(buildInfoConfig.buildName, buildInfoConfig.buildNumber)
        val npmBuildInfo = buildInfoService.getBuildInfo(buildInfoConfig.npmBuildName, buildInfoConfig.buildNumber)
        val mergedBuildInfo = buildInfoService.mergeBuildInfo(mavenBuildInfo, npmBuildInfo)
        buildInfoService.uploadBuildInfo(mergedBuildInfo)

        // TODO: Implement proper check for Xray scan status/availability before uploading merged build info to avoid race conditions
        logger.warn(
            "Waiting for 1 minute before uploading merged build info to prevent Xray indexing race condition"
        )
        Thread.sleep(TimeUnit.MINUTES.toMillis(1))

        if (buildInfoConfig.cleanupNpmBuildInfo) {
            buildInfoService.deleteBuildInfo(buildInfoConfig.npmBuildName, listOf(buildInfoConfig.buildNumber))
        }

        logger.info("NPM build info integration completed successfully!")
    }

}