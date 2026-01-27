package org.octopusden.octopus.artifactory.integration.plugins.ft.runner

class BuildToolConfig {
    lateinit var buildTool: BuildTool
    lateinit var testProjectName: String
    var additionalArguments: Array<String> = arrayOf()
    var tasks: Array<String> = arrayOf()
    var defaultArguments: Array<String> = arrayOf()
    var envVariables: Map<String, String> = emptyMap()
}