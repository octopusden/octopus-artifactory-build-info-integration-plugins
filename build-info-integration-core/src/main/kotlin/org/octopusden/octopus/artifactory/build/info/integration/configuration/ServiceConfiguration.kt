package org.octopusden.octopus.artifactory.build.info.integration.configuration

data class ServiceConfiguration(
    val componentsRegistryServiceUrl: String,
    val releaseManagementServiceUrl: String,
)
