plugins {
    kotlin("jvm") version "2.0.20"
    id("com.jfrog.artifactory") version "5.2.0"
    id("org.octopusden.octopus.artifactory-npm-gradle-plugin")
    `maven-publish`
}

group = "com.example"

repositories {
    mavenCentral()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

artifactory {
    setContextUrl(System.getProperty("artifactory.url") + "/artifactory")
    publish {
        buildInfo {
            buildName = System.getProperty("buildInfo.build.name") ?: rootProject.name
            buildNumber = System.getProperty("buildInfo.build.number") ?: version.toString()
        }
        repository {
            repoKey = System.getProperty("artifactory.repoKey") ?: "example-repo-local"
            username = System.getProperty("artifactory.username") ?: "admin"
            password = System.getProperty("artifactory.password") ?: "password"
        }
        defaults {
            publications("maven")
            setPublishArtifacts(true)
            setPublishPom(true)
        }
    }
}

artifactoryNpm {
    configuration {
        cleanupNpmBuildInfo.set(false)
    }
}

tasks.named("publish") {
    finalizedBy("artifactoryPublish")
}