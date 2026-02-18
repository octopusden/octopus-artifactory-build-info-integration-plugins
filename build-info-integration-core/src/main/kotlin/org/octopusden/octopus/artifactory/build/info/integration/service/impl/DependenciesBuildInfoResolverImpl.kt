package org.octopusden.octopus.artifactory.build.info.integration.service.impl

import org.octopusden.octopus.artifactory.build.info.integration.configuration.ServiceConfiguration
import org.octopusden.octopus.artifactory.build.info.integration.dto.DependencyBuildInfo
import org.octopusden.octopus.artifactory.build.info.integration.dto.DependencyVersion
import org.octopusden.octopus.artifactory.build.info.integration.service.DependenciesBuildInfoResolver
import org.octopusden.octopus.components.registry.client.impl.ClassicComponentsRegistryServiceClient
import org.octopusden.octopus.components.registry.client.impl.ClassicComponentsRegistryServiceClientUrlProvider
import org.octopusden.octopus.releasemanagementservice.client.impl.ClassicReleaseManagementServiceClient
import org.octopusden.octopus.releasemanagementservice.client.impl.ReleaseManagementServiceClientParametersProvider
import org.slf4j.LoggerFactory

class DependenciesBuildInfoResolverImpl(
    private val serviceConfiguration: ServiceConfiguration
): DependenciesBuildInfoResolver {

    private val logger = LoggerFactory.getLogger(DependenciesBuildInfoResolverImpl::class.java)

    private val componentsRegistryServiceClient by lazy {
        ClassicComponentsRegistryServiceClient(
            object : ClassicComponentsRegistryServiceClientUrlProvider {
                override fun getApiUrl() = serviceConfiguration.componentsRegistryServiceUrl
            }
        )
    }

    private val releaseManagementServiceClient by lazy {
        ClassicReleaseManagementServiceClient(
            object : ReleaseManagementServiceClientParametersProvider {
                override fun getApiUrl() = serviceConfiguration.releaseManagementServiceUrl
                override fun getTimeRetryInMillis() = 180000
                override fun getConnectTimeoutInMillis() = 0
                override fun getReadTimeoutInMillis() = 0
            }
        )
    }

    override fun getAllDependenciesBuildInfo(directDependencies: List<DependencyVersion>): List<DependencyBuildInfo> {
        if (directDependencies.isEmpty()) {
            logger.info("No direct dependencies provided, returning empty build info list")
            return emptyList()
        }

        val result = mutableListOf<DependencyBuildInfo>()
        val visited = mutableSetOf<String>() // component:version
        val queue = ArrayDeque<Pair<String, String>>()

        logger.info("Found ${directDependencies.size} direct-dependencies versions: ${directDependencies.joinToString(", ") { "${it.name}:${it.version}" }}")

        directDependencies.forEach { queue.add(it.name to it.version) }

        while (queue.isNotEmpty()) {
            val (component, version) = queue.removeFirst()
            val key = "$component:$version"

            if (!visited.add(key)) continue

            val build = releaseManagementServiceClient.getBuild(component, version)
            result.add(
                DependencyBuildInfo(
                    buildName = getComponentBuildName(component),
                    buildNumber = build.version
                )
            )

            build.dependencies.forEach { queue.add(it.component to it.version) }
        }

        return result
    }

    private val buildNameCache = mutableMapOf<String, String>()

    private fun getComponentBuildName(component: String): String =
        buildNameCache.getOrPut(component) {
            val distribution = componentsRegistryServiceClient.getById(component).distribution
            val explicitFlag = if (distribution?.explicit == true) "e" else "i"
            val externalFlag = if (distribution?.external == true) "e" else "i"
            "${component}_${explicitFlag}${externalFlag}"
        }
}