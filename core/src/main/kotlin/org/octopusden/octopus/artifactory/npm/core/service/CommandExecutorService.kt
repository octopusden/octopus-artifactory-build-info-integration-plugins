package org.octopusden.octopus.artifactory.npm.core.service

interface CommandExecutorService {
    fun executeCommand(command: List<String>, workingDirectory: String? = null, environmentVariables: Map<String, String> = emptyMap()): CommandResult
    data class CommandResult(val exitCode: Int, val output: String, val errorOutput: String) {
        fun isSuccess() = exitCode == 0
    }
}