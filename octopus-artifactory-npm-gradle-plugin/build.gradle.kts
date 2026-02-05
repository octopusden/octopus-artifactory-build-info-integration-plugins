plugins {
    `java-gradle-plugin`
}

description = "Gradle plugin for publishing NPM packages to Artifactory"

dependencies {
    implementation(project(":build-info-integration-core"))
    implementation("org.octopusden.octopus.octopus-external-systems-clients:artifactory-client:${property("octopus-artifactory-client.version")}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${property("kotlin.version")}")

    testImplementation(platform("org.junit:junit-bom:${property("junit.version")}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

gradlePlugin {
    plugins {
        create("artifactoryNpmPlugin") {
            id = "org.octopusden.octopus.artifactory-npm-gradle-plugin"
            implementationClass = "org.octopusden.octopus.artifactory.npm.gradle.plugin.ArtifactoryNpmGradlePlugin"
            displayName = "Artifactory NPM Integration Plugin"
            description = project.description
        }
    }
}

publishing {
    publications {
        named<MavenPublication>("pluginMaven") {
            pom {
                name.set("Artifactory NPM Gradle Plugin")
                description.set("Gradle plugin that uploads NPM dependencies and includes them in Artifactory build info")
                url.set("https://github.com/octopusden/octopus-artifactory-npm-maven-plugin")
                inceptionYear.set("2025")

                licenses {
                    license {
                        name.set("The GNU Lesser General Public License, Version 3.0")
                        url.set("http://www.gnu.org/licenses/lgpl-3.0.txt")
                        distribution.set("repo")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/octopusden/octopus-artifactory-npm-maven-plugin.git")
                    developerConnection.set("scm:git:git@github.com:octopusden/octopus-artifactory-npm-maven-plugin.git")
                    url.set("https://github.com/octopusden/octopus-artifactory-npm-maven-plugin")
                }
                developers {
                    developer {
                        id.set("octopus")
                        name.set("octopus")
                    }
                }
            }
        }
    }
}

signing {
    isRequired = project.ext["signingRequired"] as Boolean
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications["pluginMaven"])
}
