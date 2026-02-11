dependencies {
    implementation("org.slf4j:slf4j-api:${property("slf4j.version")}")
    implementation("org.octopusden.octopus.octopus-external-systems-clients:artifactory-client:${property("octopus-artifactory-client.version")}")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name.set(project.name)
                description.set("Octopus module: ${project.name}")
                url.set("https://github.com/octopusden/octopus-artifactory-build-info-integration-plugins.git")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                scm {
                    url.set("https://github.com/octopusden/octopus-artifactory-build-info-integration-plugins.git")
                    connection.set("scm:git://github.com/octopusden/octopus-artifactory-build-info-integration-plugins.git")
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
    sign(publishing.publications["maven"])
}