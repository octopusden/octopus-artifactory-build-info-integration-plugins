package org.octopusden.octopus.artifactory.integration.plugins.ft.runner

class BuildToolConfig {
    lateinit var buildTool: BuildTool
    lateinit var testProjectName: String
    var additionalArguments: List<String> = listOf()
    var tasks: List<String> = listOf()
    var defaultArguments: List<String> = listOf()
    var envVariables: Map<String, String> = emptyMap()
}