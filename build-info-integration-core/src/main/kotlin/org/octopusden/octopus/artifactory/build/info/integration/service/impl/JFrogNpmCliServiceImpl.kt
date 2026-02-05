package org.octopusden.octopus.artifactory.build.info.integration.service.impl

import org.octopusden.octopus.artifactory.build.info.integration.configuration.ArtifactoryConfiguration
import org.octopusden.octopus.artifactory.build.info.integration.exception.JFrogCliException
import org.octopusden.octopus.artifactory.build.info.integration.service.CommandExecutorService
import org.octopusden.octopus.artifactory.build.info.integration.service.JFrogNpmCliService
import org.slf4j.LoggerFactory

class JFrogNpmCliServiceImpl(
    private val commandExecutor: CommandExecutorService
) : JFrogNpmCliService {
    
    private val logger = LoggerFactory.getLogger(JFrogNpmCliServiceImpl::class.java)
    
    companion object {
        private const val JFROG_CLI_COMMAND = "jfrog"
    }

    override fun configureNpmRepository(packageJsonPath: String, npmRepository: String) {
        logger.info("Configuring NPM repository for resolution...")

        val result = commandExecutor.executeCommand(
            listOf(JFROG_CLI_COMMAND, "npm-config", "--repo-resolve", npmRepository),
            packageJsonPath
        )

        if (!result.isSuccess()) {
            throw JFrogCliException("NPM repository configuration failed", result.exitCode, result.errorOutput)
        }

        logger.info("NPM repository configured successfully")
    }
    
    override fun installNpmDependencies(packageJsonPath: String, buildName: String, buildNumber: String) {
        logger.info("Installing NPM dependencies with build info collection...")

        val result = commandExecutor.executeCommand(
            listOf(JFROG_CLI_COMMAND, "npm", "install", "--build-name", buildName, "--build-number", buildNumber),
            packageJsonPath
        )
        
        if (!result.isSuccess()) {
            throw JFrogCliException("Failed to install NPM dependencies", result.exitCode, result.errorOutput)
        }

        logger.info("NPM dependencies installed successfully with build info $buildName:$buildNumber")
    }
    
    override fun publishNpmBuildInfo(packageJsonPath: String, buildName: String, buildNumber: String, artifactoryConfig: ArtifactoryConfiguration) {
        logger.info("Publishing NPM build info...")

        val command = listOf(
            JFROG_CLI_COMMAND, "rt", "build-publish",
            buildName, buildNumber,
            "--url", artifactoryConfig.url
        )

        val env = mutableMapOf<String, String>()
        when {
            artifactoryConfig.token != null -> {
                logger.info("Using token-based authentication for publishing build info via JFrog CLI")
                env["JFROG_CLI_ACCESS_TOKEN"] = artifactoryConfig.token
            }
            artifactoryConfig.username != null && artifactoryConfig.password != null -> {
                logger.info("Using username/password authentication for publishing build info via JFrog CLI")
                env["JFROG_CLI_USER"] = artifactoryConfig.username
                env["JFROG_CLI_PASSWORD"] = artifactoryConfig.password
            }
        }

        val result = commandExecutor.executeCommand(command, packageJsonPath, env)
        
        if (!result.isSuccess()) {
            throw JFrogCliException("Failed to publish NPM build info", result.exitCode, result.errorOutput)
        }

        logger.info("NPM build info ($buildName:$buildNumber) published successfully")
    }
    
    override fun isJFrogCliAvailable(): Boolean {
        logger.debug("Checking JFrog CLI availability...")
        
        val command = listOf(JFROG_CLI_COMMAND, "--version")
        val result = commandExecutor.executeCommand(command)
        
        if (result.isSuccess()) {
            logger.debug("JFrog CLI is available: ${result.output.trim()}")
            return true
        }

        logger.error("JFrog CLI is not available or not configured properly. Error: ${result.errorOutput}")
        return false
    }

}