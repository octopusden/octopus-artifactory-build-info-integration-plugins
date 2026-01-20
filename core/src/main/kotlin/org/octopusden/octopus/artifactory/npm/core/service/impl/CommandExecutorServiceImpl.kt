package org.octopusden.octopus.artifactory.npm.core.service.impl

import org.octopusden.octopus.artifactory.npm.core.service.CommandExecutorService
import org.octopusden.octopus.artifactory.npm.core.service.CommandExecutorService.CommandResult
import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.TimeUnit

class CommandExecutorServiceImpl : CommandExecutorService {
    
    private val logger = LoggerFactory.getLogger(CommandExecutorServiceImpl::class.java)
    
    companion object {
        private const val DEFAULT_TIMEOUT_MINUTES = 10L
    }

    override fun executeCommand(command: List<String>, workingDirectory: String?, environmentVariables: Map<String, String>): CommandResult {
        try {
            val processBuilder = ProcessBuilder(command)

            workingDirectory?.let { 
                processBuilder.directory(File(it))
                logger.debug("Working directory: $it")
            }

            if (environmentVariables.isNotEmpty()) {
                val env = processBuilder.environment()
                for ((key, value) in environmentVariables) {
                    env[key] = value
                    logger.debug("Setting environment variable: $key=$value")
                }
            }

            processBuilder.redirectErrorStream(false)
            
            val process = processBuilder.start()

            val outputBuilder = StringBuilder()
            val errorBuilder = StringBuilder()
            
            val outputThread = Thread {
                process.inputStream.bufferedReader().use { reader ->
                    reader.lines().forEach { line ->
                        outputBuilder.appendLine(line)
                        logger.debug("OUT: $line")
                    }
                }
            }
            
            val errorThread = Thread {
                process.errorStream.bufferedReader().use { reader ->
                    reader.lines().forEach { line ->
                        errorBuilder.appendLine(line)
                        logger.debug("ERR: $line")
                    }
                }
            }
            
            outputThread.start()
            errorThread.start()

            val finished = process.waitFor(DEFAULT_TIMEOUT_MINUTES, TimeUnit.MINUTES)
            
            if (!finished) {
                process.destroyForcibly()
                val errorMsg = "Command timed out after $DEFAULT_TIMEOUT_MINUTES minutes"
                logger.error(errorMsg)
                return CommandResult(
                    exitCode = -1,
                    output = outputBuilder.toString(),
                    errorOutput = errorMsg
                )
            }

            outputThread.join(5000)
            if (outputThread.isAlive) {
                logger.warn("Output thread did not complete within timeout")
            }
            errorThread.join(5000)
            if (errorThread.isAlive) {
                logger.warn("Error thread did not complete within timeout")
            }
            
            val exitCode = process.exitValue()
            val output = outputBuilder.toString().trim()
            val errorOutput = errorBuilder.toString().trim()
            
            logger.debug("Command finished with exit code: $exitCode")

            if (exitCode != 0) {
                val errorMsg = "Command failed with exit code $exitCode\nError: $errorOutput"
                logger.error(errorMsg)
                return CommandResult(
                    exitCode = exitCode,
                    output = outputBuilder.toString(),
                    errorOutput = errorMsg
                )
            }
            
            return CommandResult(
                exitCode = exitCode,
                output = output,
                errorOutput = errorOutput
            )

        } catch (e: RuntimeException) {
            throw e
        } catch (e: Exception) {
            logger.error("Error executing command", e)
            return CommandResult(
                exitCode = -1,
                output = "",
                errorOutput = "Exception occurred: ${e.message}"
            )
        }
    }

}