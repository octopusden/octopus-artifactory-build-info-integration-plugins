package org.octopusden.octopus.artifactory.npm.gradle.plugin.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
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
    abstract val artifactoryUrl: Property<String>

    @get:Input
    @get:Optional
    abstract val artifactoryAccessToken: Property<String>

    @get:Input
    @get:Optional
    abstract val artifactoryUsername: Property<String>

    @get:Input
    @get:Optional
    abstract val artifactoryPassword: Property<String>

    @get:Input
    abstract val npmRepository: Property<String>

    @get:Input
    abstract val buildName: Property<String>

    @get:Input
    abstract val buildNumber: Property<String>

    @get:Input
    abstract val npmBuildNameSuffix: Property<String>

    @get:Input
    abstract val packageJsonPath: Property<String>

    @get:Input
    abstract val cleanupNpmBuildInfo: Property<Boolean>

    protected lateinit var integrationService: NpmBuildInfoIntegrationService

    protected fun validateParameters() {
        require(artifactoryUrl.isPresent) {
            "artifactoryUrl must be configured"
        }

        require(buildName.isPresent) {
            "buildName must be configured"
        }

        require(buildNumber.isPresent) {
            "buildNumber must be configured"
        }

        val packageJsonFile = getPackageJsonFile()
        require(packageJsonFile.exists()) {
            "package.json not found at: ${packageJsonFile.absolutePath}"
        }

        require(packageJsonFile.isFile) {
            "package.json path is not a file: ${packageJsonFile.absolutePath}"
        }
    }

    protected fun getPackageJsonFile(): File {
        val path = packageJsonPath.get()
        val baseFile = if (path.isEmpty()) {
            project.projectDir
        } else {
            File(project.projectDir, path)
        }

        return if (baseFile.isDirectory) {
            File(baseFile, "package.json")
        } else {
            baseFile
        }
    }

    protected fun initializeServices() {
        val commandExecutor = CommandExecutorServiceImpl()
        val jfrogCliService = JFrogNpmCliServiceImpl(commandExecutor)
        val buildInfoService = ArtifactoryBuildInfoServiceImpl(createArtifactoryClient())

        integrationService = NpmBuildInfoIntegrationServiceImpl(jfrogCliService, buildInfoService)
    }

    protected fun createBuildInfoConfiguration(): BuildInfoConfiguration {
        return BuildInfoConfiguration(
            buildName.get(),
            buildNumber.get(),
            npmBuildNameSuffix.get(),
            npmRepository.get(),
            cleanupNpmBuildInfo.get()
        )
    }

    protected fun createArtifactoryConfiguration(): ArtifactoryConfiguration {
        return ArtifactoryConfiguration(
            artifactoryUrl.get(),
            artifactoryUsername.orNull,
            artifactoryPassword.orNull,
            artifactoryAccessToken.orNull
        )
    }

    private fun createArtifactoryClient(): ArtifactoryClient {
        val credentialProvider: CredentialProvider = when {
            artifactoryAccessToken.isPresent && artifactoryAccessToken.get().isNotBlank() ->
                StandardBearerTokenCredentialProvider(artifactoryAccessToken.get())

            artifactoryUsername.isPresent && artifactoryPassword.isPresent &&
                    artifactoryUsername.get().isNotBlank() && artifactoryPassword.get().isNotBlank() ->
                StandardBasicCredCredentialProvider(artifactoryUsername.get(), artifactoryPassword.get())

            else ->
                throw GradleException(
                    "Artifactory credentials are not properly configured. " +
                            "Please set 'artifactoryAccessToken' or both 'artifactoryUsername' and 'artifactoryPassword'."
                )
        }

        return ArtifactoryClassicClient(object : ClientParametersProvider {
            override fun getApiUrl(): String = artifactoryUrl.get()
            override fun getAuth(): CredentialProvider = credentialProvider
        })
    }
}