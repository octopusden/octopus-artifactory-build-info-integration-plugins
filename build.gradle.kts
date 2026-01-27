import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.time.Duration

plugins {
    kotlin("jvm") version "2.0.0"
    idea
    signing
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
}

allprojects {
    group = "org.octopusden.octopus"
    if (version == "unspecified") {
        version = "3.0-SNAPSHOT"
    }
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
            username.set(System.getenv("MAVEN_USERNAME"))
            password.set(System.getenv("MAVEN_PASSWORD"))
        }
    }
    transitionCheckOptions {
        maxRetries.set(60)
        delayBetween.set(Duration.ofSeconds(30))
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "idea")

    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    java {
        withJavadocJar()
        withSourcesJar()
        JavaVersion.VERSION_21.let {
            sourceCompatibility = it
            targetCompatibility = it
        }
    }

    kotlin {
        compilerOptions.jvmTarget = JvmTarget.JVM_21
    }

    idea.module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }

    ext {
        System.getenv().let {
            set("signingRequired", it.containsKey("ORG_GRADLE_PROJECT_signingKey") && it.containsKey("ORG_GRADLE_PROJECT_signingPassword"))
        }
    }
}