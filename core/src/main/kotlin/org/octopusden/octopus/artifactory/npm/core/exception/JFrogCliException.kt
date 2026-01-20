package org.octopusden.octopus.artifactory.npm.core.exception

class JFrogCliException(
    message: String,
    private val exitCode: Int? = null,
    private val errorOutput: String? = null,
    cause: Throwable? = null
) : CoreException(message, cause) {
    
    override fun toString(): String {
        val details = mutableListOf<String>()
        exitCode?.let { details.add("Exit Code: $it") }
        errorOutput?.let { details.add("Error: $it") }
        
        return if (details.isNotEmpty()) {
            "${super.toString()} (${details.joinToString(", ")})"
        } else {
            super.toString()
        }
    }
}