pluginManagement {
    plugins {
        id("org.octopusden.octopus.artifactory-npm-gradle-plugin") version settings.extra["octopus-artifactory-npm-gradle-plugin.version"] as String
    }
}

rootProject.name = "simple-projects"