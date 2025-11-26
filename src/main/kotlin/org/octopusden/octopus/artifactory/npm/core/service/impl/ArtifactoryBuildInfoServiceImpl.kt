package org.octopusden.octopus.artifactory.npm.core.service.impl

import org.octopusden.octopus.artifactory.npm.core.exception.ArtifactoryException
import org.octopusden.octopus.artifactory.npm.core.service.ArtifactoryBuildInfoService
import org.octopusden.octopus.infrastructure.artifactory.client.ArtifactoryClient
import org.octopusden.octopus.infrastructure.artifactory.client.dto.Agent
import org.octopusden.octopus.infrastructure.artifactory.client.dto.BuildAgent
import org.octopusden.octopus.infrastructure.artifactory.client.dto.BuildInfo
import org.octopusden.octopus.infrastructure.artifactory.client.dto.DeleteBuildRequest
import org.octopusden.octopus.infrastructure.artifactory.client.exception.ArtifactoryClientException
import org.octopusden.octopus.infrastructure.artifactory.client.exception.NotFoundException
import org.slf4j.LoggerFactory

class ArtifactoryBuildInfoServiceImpl(
    private val artifactoryClient: ArtifactoryClient
) : ArtifactoryBuildInfoService {
    
    private val logger = LoggerFactory.getLogger(ArtifactoryBuildInfoServiceImpl::class.java)
    
    override fun getBuildInfo(buildName: String, buildNumber: String) =
        try {
            logger.info("Get build info $buildName:$buildNumber")
            artifactoryClient.getBuildInfo(buildName, buildNumber).buildInfo
        } catch (e: NotFoundException) {
            throw ArtifactoryException("Build info not found. Please ensure the build info has been published ($buildName:$buildNumber)", e)
        } catch (e: ArtifactoryClientException) {
            throw ArtifactoryException("Failed to retrieve build info from Artifactory", e)
        }

    override fun mergeBuildInfo(mavenBuildInfo: BuildInfo, npmBuildInfo: BuildInfo): BuildInfo {
        logger.info("Merging NPM build info (${npmBuildInfo.name}:${npmBuildInfo.number}) into Maven build info (${mavenBuildInfo.name}:${mavenBuildInfo.number})")

        val mergedModules = (mavenBuildInfo.modules?.toList() ?: emptyList()).toMutableList()
        mergedModules += npmBuildInfo.modules?.toList() ?: emptyList()

        return BuildInfo(
            mavenBuildInfo.name,
            mavenBuildInfo.number,
            mavenBuildInfo.version,
            Agent("Octopus NPM Maven Plugin", ""),
            BuildAgent("GENERIC", "2.66.0"),
            mavenBuildInfo.started,
            null,
            mergedModules,
            mavenBuildInfo.statuses
        )
    }

    override fun uploadBuildInfo(buildInfo: BuildInfo) {
        try {
            logger.info("Uploading build info ${buildInfo.name}:${buildInfo.number}")
            artifactoryClient.uploadBuildInfo(buildInfo)
        } catch (e: ArtifactoryClientException) {
            throw ArtifactoryException("Error uploading build info ${buildInfo.name}:${buildInfo.number}", e)
        }
    }

    override fun deleteBuildInfo(buildName: String, buildNumbers: List<String>) {
        try {
            logger.info("Deleting build info for $buildName with numbers: $buildNumbers")
            artifactoryClient.deleteBuild(DeleteBuildRequest(buildName, buildNumbers))
        } catch (e: ArtifactoryClientException) {
            throw ArtifactoryException("Error deleting build info for $buildName with numbers: $buildNumbers", e)
        }
    }

}