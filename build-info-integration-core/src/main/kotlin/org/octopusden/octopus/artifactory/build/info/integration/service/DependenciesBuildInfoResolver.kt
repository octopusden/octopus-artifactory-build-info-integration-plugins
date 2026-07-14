package org.octopusden.octopus.artifactory.build.info.integration.service

import org.octopusden.octopus.artifactory.build.info.integration.dto.DependencyBuildInfo
import org.octopusden.octopus.artifactory.build.info.integration.dto.DependencyVersion

interface DependenciesBuildInfoResolver {
    fun getAllDependenciesBuildInfo(directDependencies: List<DependencyVersion>): List<DependencyBuildInfo>
}
