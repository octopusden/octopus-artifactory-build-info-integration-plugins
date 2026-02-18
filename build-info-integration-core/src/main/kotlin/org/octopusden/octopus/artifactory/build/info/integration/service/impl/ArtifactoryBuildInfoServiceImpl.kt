package org.octopusden.octopus.artifactory.build.info.integration.service.impl

import org.octopusden.octopus.artifactory.build.info.integration.dto.DependencyBuildInfo
import org.octopusden.octopus.artifactory.build.info.integration.exception.ArtifactoryException
import org.octopusden.octopus.artifactory.build.info.integration.service.ArtifactoryBuildInfoService
import org.octopusden.octopus.infrastructure.artifactory.client.ArtifactoryClient
import org.octopusden.octopus.infrastructure.artifactory.client.dto.Agent
import org.octopusden.octopus.infrastructure.artifactory.client.dto.BuildInfo
import org.octopusden.octopus.infrastructure.artifactory.client.dto.DeleteBuildRequest
import org.octopusden.octopus.infrastructure.artifactory.client.dto.Module
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

    override fun getNpmDependenciesModules(dependencies: List<DependencyBuildInfo>): List<Module> =
        dependencies.flatMap { dependency ->
            logger.info("Retrieving build info for dependency build ${dependency.buildName}:${dependency.buildNumber}")
            try {
                val dependencyBuildInfo = artifactoryClient.getBuildInfo(dependency.buildName, dependency.buildNumber).buildInfo
                dependencyBuildInfo.modules
                    ?.filter { it.type == "npm" }
                    ?: emptyList()
            } catch (e: NotFoundException) {
                logger.warn("Build info for dependency build ${dependency.buildName}:${dependency.buildNumber} not found. Skipping.", e)
                emptyList()
            } catch (e: ArtifactoryClientException) {
                logger.error("Error retrieving build info for dependency build ${dependency.buildName}:${dependency.buildNumber}. Skipping.", e)
                emptyList()
            }
        }.distinctBy { it.id }

    override fun mergeBuildInfo(mavenBuildInfo: BuildInfo, npmBuildInfo: BuildInfo?, npmDependenciesModules: List<Module>): BuildInfo {
        if (npmBuildInfo != null) {
            logger.info("Merging NPM build info (${npmBuildInfo.name}:${npmBuildInfo.number}) into Maven build info (${mavenBuildInfo.name}:${mavenBuildInfo.number})")
        }

        val mergedModules = (mavenBuildInfo.modules?.toList() ?: emptyList()).toMutableList()
        mergedModules += npmBuildInfo?.modules?.map { it.copy(artifacts = emptyList()) } ?: emptyList()

        logger.info("Merging ${npmDependenciesModules.size} NPM dependency modules into Maven build info (${mavenBuildInfo.name}:${mavenBuildInfo.number})")
        mergedModules += npmDependenciesModules.map { it.copy(artifacts = emptyList()) }

        return BuildInfo(
            mavenBuildInfo.name,
            mavenBuildInfo.number,
            mavenBuildInfo.version,
            Agent(NPM_BUILD_INFO_CI_AGENT_NAME, NPM_BUILD_INFO_AGENT_VERSION),
            mavenBuildInfo.buildAgent,
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

    companion object {
        private const val NPM_BUILD_INFO_CI_AGENT_NAME = "jfrog-cli-go"
        private const val NPM_BUILD_INFO_AGENT_VERSION = "2.66.0"
    }

}