import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.net.InetAddress
import java.time.Duration
import java.util.zip.CRC32

plugins {
    kotlin("jvm")
    idea
    signing
    id("io.github.gradle-nexus.publish-plugin")
    id("com.jfrog.artifactory")
}

val defaultVersion = "${
    with(CRC32()) {
        update(InetAddress.getLocalHost().hostName.toByteArray())
        value
    }
}-SNAPSHOT"

allprojects {
    group = "org.octopusden.octopus"
    if (version == "unspecified") {
        version = defaultVersion
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
    apply(plugin = "com.jfrog.artifactory")

    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    artifactory {
        publish {
            val baseUrl = System.getenv("ARTIFACTORY_URL") ?: project.findProperty("artifactoryUrl") as String?
            if (!baseUrl.isNullOrBlank()) {
                contextUrl = "$baseUrl/artifactory"
            }
            repository {
                repoKey = "rnd-maven-dev-local"
                val repoUser = System.getenv("ARTIFACTORY_DEPLOYER_USERNAME") ?: project.findProperty("NEXUS_USER") as String?
                val repoPassword = System.getenv("ARTIFACTORY_DEPLOYER_PASSWORD") ?: project.findProperty("NEXUS_PASSWORD") as String?
                if (!repoUser.isNullOrBlank()) username = repoUser
                if (!repoPassword.isNullOrBlank()) password = repoPassword
            }
            defaults {
                publications("ALL_PUBLICATIONS")
            }
        }
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(8))
        }
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        withJavadocJar()
        withSourcesJar()
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.release.set(8)
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