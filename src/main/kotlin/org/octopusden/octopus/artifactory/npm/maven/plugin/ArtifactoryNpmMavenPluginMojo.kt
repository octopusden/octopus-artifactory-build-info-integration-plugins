package org.octopusden.octopus.artifactory.npm.maven.plugin

import org.apache.maven.execution.MavenSession
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.plugins.annotations.ResolutionScope
import org.apache.maven.project.MavenProject
import org.octopusden.octopus.artifactory.npm.maven.plugin.configuration.ArtifactoryConfiguration
import org.octopusden.octopus.artifactory.npm.maven.plugin.configuration.PluginConfiguration
import org.octopusden.octopus.artifactory.npm.maven.plugin.service.NpmBuildInfoIntegrationService
import org.octopusden.octopus.artifactory.npm.maven.plugin.service.impl.ArtifactoryBuildInfoServiceImpl
import org.octopusden.octopus.artifactory.npm.maven.plugin.service.impl.CommandExecutorServiceImpl
import org.octopusden.octopus.artifactory.npm.maven.plugin.service.impl.JFrogNpmCliServiceImpl
import org.octopusden.octopus.artifactory.npm.maven.plugin.service.impl.NpmBuildInfoIntegrationServiceImpl
import org.octopusden.octopus.artifactory.npm.maven.plugin.utils.ParameterValidator
import org.octopusden.octopus.infrastructure.artifactory.client.ArtifactoryClassicClient
import org.octopusden.octopus.infrastructure.artifactory.client.ArtifactoryClient
import org.octopusden.octopus.infrastructure.client.commons.ClientParametersProvider
import org.octopusden.octopus.infrastructure.client.commons.CredentialProvider
import org.octopusden.octopus.infrastructure.client.commons.StandardBasicCredCredentialProvider
import org.octopusden.octopus.infrastructure.client.commons.StandardBearerTokenCredentialProvider
import java.io.File

@Mojo(
    name = "integrate-npm-build-info",
    requiresDependencyResolution = ResolutionScope.RUNTIME
)
class ArtifactoryNpmMavenPluginMojo : AbstractMojo() {

    @Parameter(defaultValue = "\${project}", readonly = true, required = true)
    private lateinit var project: MavenProject

    @Parameter(defaultValue = "\${session}", readonly = true, required = true)
    private lateinit var session: MavenSession

    @Parameter(property = "artifactoryUrl", required = true)
    private lateinit var artifactoryUrl: String

    @Parameter(property = "artifactoryAccessToken")
    private var artifactoryAccessToken: String? = null

    @Parameter(property = "artifactoryUsername")
    private var artifactoryUsername: String? = null

    @Parameter(property = "artifactoryPassword")
    private var artifactoryPassword: String? = null

    @Parameter(property = "artifactory.npm.repository", defaultValue = "npm")
    private var npmRepository: String = "npm"

    @Parameter(property = "artifactory.build.name", required = true)
    private lateinit var buildName: String

    @Parameter(property = "artifactory.build.version", required = true)
    private lateinit var buildNumber: String

    @Parameter(property = "artifactory.npm.build.name.suffix", defaultValue = "_npm")
    private var npmBuildNameSuffix: String = "_npm"

    @Parameter(property = "package.json.path", defaultValue = "")
    private var packageJsonPath: String = ""

    @Parameter(property = "artifactory.npm.skip", defaultValue = "false")
    private var skip: Boolean = false

    @Parameter(property = "artifactory.npm.cleanup.build.info", defaultValue = "true")
    private var cleanupNpmBuildInfo: Boolean = true

    private lateinit var integrationService: NpmBuildInfoIntegrationService

    override fun execute() {
        if (skip) {
            log.info("Skipping NPM build info integration (artifactory.npm.skip=true)")
            return
        }

        log.info("Starting NPM build info integration for build $buildName:$buildNumber")

        validateParameters()
        initializeServices()
        val pluginConfiguration = PluginConfiguration(
            buildName, buildNumber,
            npmBuildNameSuffix, npmRepository,
            File(project.basedir, packageJsonPath).absolutePath,
            skip, cleanupNpmBuildInfo
        )
        val artifactoryConfiguration = ArtifactoryConfiguration(
            artifactoryUrl, artifactoryUsername,
            artifactoryPassword, artifactoryAccessToken
        )

        integrationService.generateNpmBuildInfo(pluginConfiguration, artifactoryConfiguration)

        val originalListener = session.request.executionListener
        session.request.executionListener = ArtifactoryNpmBuildInfoListener(originalListener) {
            integrationService.integrateNpmBuildInfo(pluginConfiguration)
        }
    }

    private fun validateParameters() {
        ParameterValidator.validateArtifactoryUrl(artifactoryUrl)
        ParameterValidator.validatePackageJsonFile(File(project.basedir, packageJsonPath))
    }

    private fun initializeServices() {
        val commandExecutor = CommandExecutorServiceImpl()
        val jfrogCliService = JFrogNpmCliServiceImpl(commandExecutor)
        val buildInfoService = ArtifactoryBuildInfoServiceImpl(createArtifactoryClient())

        integrationService = NpmBuildInfoIntegrationServiceImpl(jfrogCliService, buildInfoService)
    }

    private fun createArtifactoryClient(): ArtifactoryClient {
        val credentialProvider = when {
            !artifactoryAccessToken.isNullOrBlank() ->
                StandardBearerTokenCredentialProvider(artifactoryAccessToken!!)

            !artifactoryUsername.isNullOrBlank() && !artifactoryPassword.isNullOrBlank() ->
                StandardBasicCredCredentialProvider(artifactoryUsername!!, artifactoryPassword!!)

            else ->
                throw MojoExecutionException(
                    "Artifactory credentials are not properly configured. " +
                            "Please set `artifactoryAccessToken` or both `artifactoryUsername` and `artifactoryPassword`."
                )
        }

        return ArtifactoryClassicClient(object : ClientParametersProvider {
            override fun getApiUrl(): String = artifactoryUrl
            override fun getAuth(): CredentialProvider = credentialProvider
        })
    }
}