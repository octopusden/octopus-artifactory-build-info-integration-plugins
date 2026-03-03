package org.octopusden.octopus.artifactory.build.info.integration.service

import org.octopusden.octopus.artifactory.build.info.integration.dto.DependencyBuildInfo
import org.octopusden.octopus.infrastructure.artifactory.client.dto.BuildInfo
import org.octopusden.octopus.infrastructure.artifactory.client.dto.Module

interface ArtifactoryBuildInfoService {
    fun getBuildInfo(buildName: String, buildNumber: String): BuildInfo
    fun getNpmDependenciesModules(dependencies: List<DependencyBuildInfo>): List<Module>
    fun mergeBuildInfo(mavenBuildInfo: BuildInfo, npmBuildInfo: BuildInfo?, npmDependenciesModules: List<Module>): BuildInfo
    fun uploadBuildInfo(buildInfo: BuildInfo)
    fun deleteBuildInfo(buildName: String, buildNumbers: List<String>)
}