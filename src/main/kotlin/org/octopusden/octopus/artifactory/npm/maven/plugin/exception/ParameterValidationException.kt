package org.octopusden.octopus.artifactory.npm.maven.plugin.exception

class ParameterValidationException(
    message: String,
    val field: String? = null,
    val value: String? = null,
    cause: Throwable? = null
) : PluginException(message, cause) {
    
    override fun toString(): String {
        val details = mutableListOf<String>()
        field?.let { details.add("Field: $it") }
        value?.let { details.add("Value: $it") }
        
        return if (details.isNotEmpty()) {
            "${super.toString()} (${details.joinToString(", ")})"
        } else {
            super.toString()
        }
    }
}