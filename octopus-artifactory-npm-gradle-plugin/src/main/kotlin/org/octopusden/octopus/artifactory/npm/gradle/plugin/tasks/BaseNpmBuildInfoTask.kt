package org.octopusden.octopus.artifactory.npm.gradle.plugin.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.octopusden.octopus.artifactory.build.info.integration.configuration.ArtifactoryConfiguration
import org.octopusden.octopus.artifactory.build.info.integration.configuration.BuildInfoConfiguration
import org.octopusden.octopus.artifactory.build.info.integration.configuration.ServiceConfiguration
import org.octopusden.octopus.artifactory.build.info.integration.service.NpmBuildInfoIntegrationService
import org.octopusden.octopus.artifactory.build.info.integration.service.impl.ArtifactoryBuildInfoServiceImpl
import org.octopusden.octopus.artifactory.build.info.integration.service.impl.CommandExecutorServiceImpl
import org.octopusden.octopus.artifactory.build.info.integration.service.impl.DependenciesBuildInfoResolverImpl
import org.octopusden.octopus.artifactory.build.info.integration.service.impl.JFrogNpmCliServiceImpl
import org.octopusden.octopus.artifactory.build.info.integration.service.impl.NpmBuildInfoIntegrationServiceImpl
import org.octopusden.octopus.infrastructure.artifactory.client.ArtifactoryClassicClient
import org.octopusden.octopus.infrastructure.artifactory.client.ArtifactoryClient
import org.octopusden.octopus.infrastructure.client.commons.ClientParametersProvider
import org.octopusden.octopus.infrastructure.client.commons.CredentialProvider
import org.octopusden.octopus.infrastructure.client.commons.StandardBasicCredCredentialProvider
import org.octopusden.octopus.infrastructure.client.commons.StandardBearerTokenCredentialProvider

abstract class BaseNpmBuildInfoTask : DefaultTask() {

    @get:Input
    abstract val buildName: Property<String>

    @get:Input
    abstract val buildNumber: Property<String>

    @get:Input
    abstract val npmRepository: Property<String>

    @get:Input
    abstract val npmBuildNameSuffix: Property<String>

    @get:Input
    abstract val cleanupNpmBuildInfo: Property<Boolean>

    @get:Input
    abstract val skipWaitForXrayScan: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val componentsRegistryServiceUrl: Property<String>

    @get:Input
    @get:Optional
    abstract val releaseManagementServiceUrl: Property<String>

    @get:Internal
    protected lateinit var integrationService: NpmBuildInfoIntegrationService

    protected fun initializeServices() {
        val commandExecutor = CommandExecutorServiceImpl()
        val jfrogCliService = JFrogNpmCliServiceImpl(commandExecutor)
        val buildInfoService = ArtifactoryBuildInfoServiceImpl(createArtifactoryClient())

        integrationService = NpmBuildInfoIntegrationServiceImpl(jfrogCliService, buildInfoService) {
            DependenciesBuildInfoResolverImpl(ServiceConfiguration(getComponentsRegistryServiceUrl(), getReleaseManagementServiceUrl()))
        }
    }

    protected fun createBuildInfoConfiguration(): BuildInfoConfiguration {
        return BuildInfoConfiguration(
            getBuildName(),
            getBuildNumber(),
            npmBuildNameSuffix.get(),
            npmRepository.get(),
            cleanupNpmBuildInfo.get()
        )
    }

    protected fun createArtifactoryConfiguration(): ArtifactoryConfiguration {
        return ArtifactoryConfiguration(
            getArtifactoryUrl(),
            getArtifactoryUsername(),
            getArtifactoryPassword(),
            getArtifactoryAccessToken()
        )
    }

    private fun createArtifactoryClient(): ArtifactoryClient {
        val credentialProvider: CredentialProvider = when {
            getArtifactoryAccessToken() != null ->
                StandardBearerTokenCredentialProvider(getArtifactoryAccessToken()!!)

            getArtifactoryUsername() != null && getArtifactoryPassword() != null ->
                StandardBasicCredCredentialProvider(getArtifactoryUsername()!!, getArtifactoryPassword()!!)

            else ->
                throw GradleException(
                    "Artifactory credentials are not properly configured. " +
                            "Please set system property 'artifactory.accessToken' or both 'artifactory.username' and 'artifactory.password'."
                )
        }

        return ArtifactoryClassicClient(object : ClientParametersProvider {
            override fun getApiUrl(): String = getArtifactoryUrl()
            override fun getAuth(): CredentialProvider = credentialProvider
        })
    }

    private fun getArtifactoryUrl(): String {
        return getEnvOrSystemProperty("ARTIFACTORY_URL", "artifactory.url")
            ?: throw GradleException("Environment variable 'ARTIFACTORY_URL' or system property 'artifactory.url' must be provided")
    }

    private fun getArtifactoryAccessToken(): String? = getEnvOrSystemProperty("ARTIFACTORY_ACCESS_TOKEN", "artifactory.accessToken")

    private fun getArtifactoryUsername(): String? = getEnvOrSystemProperty("ARTIFACTORY_DEPLOYER_USERNAME", "artifactory.username")

    private fun getArtifactoryPassword(): String? = getEnvOrSystemProperty("ARTIFACTORY_DEPLOYER_PASSWORD", "artifactory.password")

    private fun getEnvOrSystemProperty(envVariable: String, systemPropertyName: String): String? =
        System.getenv(envVariable)?.takeIf { it.isNotBlank() }
            ?: System.getProperty(systemPropertyName)?.takeIf { it.isNotBlank() }

    private fun getBuildName(): String = getProjectOrSettingsProperty("buildInfo.build.name", buildName)

    private fun getBuildNumber(): String = getProjectOrSettingsProperty("buildInfo.build.number", buildNumber)

    private fun getComponentsRegistryServiceUrl(): String = getProjectOrSettingsProperty("component-registry-service-url", componentsRegistryServiceUrl)

    private fun getReleaseManagementServiceUrl(): String = getProjectOrSettingsProperty("release-management-service-url", releaseManagementServiceUrl)

    protected fun getProjectOrSettingsProperty(
        projectPropertyKey: String,
        settingsProvider: Provider<String>
    ): String =
        (project.findProperty(projectPropertyKey) as? String)
            ?.takeIf { it.isNotBlank() }
            ?: settingsProvider.orNull?.takeIf { it.isNotBlank() }
            ?: throw GradleException("Parameter '$projectPropertyKey' is not provided")
}