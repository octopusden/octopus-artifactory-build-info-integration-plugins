package org.octopusden.octopus.artifactory.build.info.integration.service.impl

import org.octopusden.octopus.artifactory.build.info.integration.configuration.ArtifactoryConfiguration
import org.octopusden.octopus.artifactory.build.info.integration.configuration.BuildInfoConfiguration
import org.octopusden.octopus.artifactory.build.info.integration.exception.ConfigurationException
import org.octopusden.octopus.artifactory.build.info.integration.service.ArtifactoryBuildInfoService
import org.octopusden.octopus.artifactory.build.info.integration.service.JFrogNpmCliService
import org.octopusden.octopus.artifactory.build.info.integration.service.NpmBuildInfoIntegrationService
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

    override fun integrateNpmBuildInfo(buildInfoConfig: BuildInfoConfiguration, skipWaitForXrayScan: Boolean) {
        logger.info("Integrate NPM build info into Maven build info for build ${buildInfoConfig.buildName}:${buildInfoConfig.buildNumber}")

        val mavenBuildInfo = buildInfoService.getBuildInfo(buildInfoConfig.buildName, buildInfoConfig.buildNumber)
        val npmBuildInfo = buildInfoService.getBuildInfo(buildInfoConfig.npmBuildName, buildInfoConfig.buildNumber)
        val mergedBuildInfo = buildInfoService.mergeBuildInfo(mavenBuildInfo, npmBuildInfo)

        if (skipWaitForXrayScan) {
            logger.debug("Skipping wait for Xray indexing before uploading merged build info as per configuration")
        } else {
            // TODO: Implement proper check for Xray scan status/availability before uploading merged build info to avoid race conditions
            logger.warn(
                "Waiting for $XRAY_INDEXING_WAIT_MINUTES minute(s) before uploading merged build info to prevent Xray indexing race condition"
            )
            Thread.sleep(TimeUnit.MINUTES.toMillis(XRAY_INDEXING_WAIT_MINUTES))
        }

        buildInfoService.uploadBuildInfo(mergedBuildInfo)

        if (buildInfoConfig.cleanupNpmBuildInfo) {
            buildInfoService.deleteBuildInfo(buildInfoConfig.npmBuildName, listOf(buildInfoConfig.buildNumber))
        }

        logger.info("NPM build info integration completed successfully!")
    }

    companion object {
        private const val XRAY_INDEXING_WAIT_MINUTES = 1L
    }

}