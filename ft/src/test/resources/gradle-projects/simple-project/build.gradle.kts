buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.jfrog.buildinfo:build-info-extractor-gradle:5.2.0")
    }
}

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
//    setContextUrl(System.getProperty("artifactory.url") as String? ?: "")
    setContextUrl("http://localhost:8082/artifactory")
    publish {
        repository {
            repoKey = System.getProperty("artifactory.repoKey") as String? ?: ""
            username = System.getProperty("artifactory.username") as String? ?: ""
            password = System.getProperty("artifactory.password") as String? ?: ""
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