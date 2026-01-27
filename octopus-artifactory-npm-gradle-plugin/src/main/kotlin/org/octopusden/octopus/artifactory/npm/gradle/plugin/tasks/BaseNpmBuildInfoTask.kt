package org.octopusden.octopus.artifactory.npm.gradle.plugin.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.octopusden.octopus.artifactory.build.info.integration.configuration.ArtifactoryConfiguration
import org.octopusden.octopus.artifactory.build.info.integration.configuration.BuildInfoConfiguration
import org.octopusden.octopus.artifactory.build.info.integration.service.NpmBuildInfoIntegrationService
import org.octopusden.octopus.artifactory.build.info.integration.service.impl.ArtifactoryBuildInfoServiceImpl
import org.octopusden.octopus.artifactory.build.info.integration.service.impl.CommandExecutorServiceImpl
import org.octopusden.octopus.artifactory.build.info.integration.service.impl.JFrogNpmCliServiceImpl
import org.octopusden.octopus.artifactory.build.info.integration.service.impl.NpmBuildInfoIntegrationServiceImpl
import org.octopusden.octopus.infrastructure.artifactory.client.ArtifactoryClassicClient
import org.octopusden.octopus.infrastructure.artifactory.client.ArtifactoryClient
import org.octopusden.octopus.infrastructure.client.commons.ClientParametersProvider
import org.octopusden.octopus.infrastructure.client.commons.CredentialProvider
import org.octopusden.octopus.infrastructure.client.commons.StandardBasicCredCredentialProvider
import org.octopusden.octopus.infrastructure.client.commons.StandardBearerTokenCredentialProvider
import java.io.File

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

    @get:Internal
    protected lateinit var integrationService: NpmBuildInfoIntegrationService

    protected fun initializeServices() {
        val commandExecutor = CommandExecutorServiceImpl()
        val jfrogCliService = JFrogNpmCliServiceImpl(commandExecutor)
        val buildInfoService = ArtifactoryBuildInfoServiceImpl(createArtifactoryClient())

        integrationService = NpmBuildInfoIntegrationServiceImpl(jfrogCliService, buildInfoService)
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
        return getSystemProperty("artifactory.url")
            ?: throw GradleException("System property 'artifactory.url' must be provided")
    }

    private fun getArtifactoryAccessToken(): String? = getSystemProperty("artifactory.accessToken")

    private fun getArtifactoryUsername(): String? = getSystemProperty("artifactory.username")

    private fun getArtifactoryPassword(): String? = getSystemProperty("artifactory.password")

    private fun getSystemProperty(key: String): String? {
        return System.getProperty(key)?.takeIf { it.isNotBlank() }
    }

    private fun getBuildName(): String = resolveBuildInfo("buildInfo.build.name", buildName)

    private fun getBuildNumber(): String = resolveBuildInfo("buildInfo.build.number", buildNumber)

    private fun resolveBuildInfo(
        projectPropertyKey: String,
        settingsProvider: Provider<String>
    ): String =
        (project.findProperty(projectPropertyKey) as? String)
            ?.takeIf { it.isNotBlank() }
            ?: settingsProvider.orNull
            ?: throw GradleException("Build info parameter '$projectPropertyKey' is not provided")
}