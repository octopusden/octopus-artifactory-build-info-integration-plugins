plugins {
    `java-gradle-plugin`
}

description = "Gradle plugin for publishing NPM packages to Artifactory"

dependencies {
    implementation(project(":build-info-integration-core"))
    implementation(
        "org.octopusden.octopus.octopus-external-systems-clients:artifactory-client:${property("octopus-artifactory-client.version")}",
    )
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${property("kotlin.version")}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${property("jackson.version")}")

    testImplementation(platform("org.junit:junit-bom:${property("junit.version")}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

// On Gradle 9, java-gradle-plugin turns stricter validation on for a plugin project that applies a
// publishing plugin (maven-publish here), which also requires every task type to declare why it is not
// cacheable. Keep the validation that Gradle 8 ran, so the published plugin classes stay as they are.
tasks.validatePlugins {
    enableStricterValidation.set(false)
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
        withType<MavenPublication> {
            pom {
                name.set("Artifactory NPM Gradle Plugin")
                description.set("Gradle plugin that uploads NPM dependencies and includes them in Artifactory build info")
                url.set("https://github.com/octopusden/octopus-artifactory-build-info-integration-plugins")
                inceptionYear.set("2025")

                licenses {
                    license {
                        name.set("The GNU Lesser General Public License, Version 3.0")
                        url.set("http://www.gnu.org/licenses/lgpl-3.0.txt")
                        distribution.set("repo")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/octopusden/octopus-artifactory-build-info-integration-plugins.git")
                    developerConnection.set("scm:git:git@github.com:octopusden/octopus-artifactory-build-info-integration-plugins.git")
                    url.set("https://github.com/octopusden/octopus-artifactory-build-info-integration-plugins")
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
    sign(publishing.publications)
}
