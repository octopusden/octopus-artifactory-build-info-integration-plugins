package org.octopusden.octopus.artifactory.build.info.integration.service

import org.octopusden.octopus.infrastructure.artifactory.client.dto.BuildInfo

interface ArtifactoryBuildInfoService {
    fun getBuildInfo(buildName: String, buildNumber: String): BuildInfo
    fun mergeBuildInfo(mavenBuildInfo: BuildInfo, npmBuildInfo: BuildInfo): BuildInfo
    fun uploadBuildInfo(buildInfo: BuildInfo)
    fun deleteBuildInfo(buildName: String, buildNumbers: List<String>)
}