dependencies {
    implementation("org.octopusden.octopus.artifactory:build-info-integration-core:${project.version}")
    implementation("org.octopusden.octopus.octopus-external-systems-clients:artifactory-client:2.0.75")

    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.0.0")
    implementation("org.eclipse.sisu:org.eclipse.sisu.plexus:0.9.0.M2")
    implementation("org.slf4j:slf4j-api:2.0.9")

    // Maven plugin dependencies (provided scope)
    implementation("org.apache.maven:maven-plugin-api:3.9.6")
    implementation("org.apache.maven:maven-core:3.9.6")
    implementation("org.apache.maven.plugin-tools:maven-plugin-annotations:3.10.2")
}

//tasks.register<Exec>("generatePluginDescriptor") {
//    dependsOn(tasks.named("classes"))
//
//    commandLine(
//        "/Users/aksetiyawan/MacOS/MAVEN/LATEST/bin/mvn",
//        "-B",
//        "-f", "${layout.buildDirectory}/pom.xml",
//        "org.apache.maven.plugins:maven-plugin-plugin:3.10.2:descriptor"
//    )
//}
//
//tasks.named("jar") {
//    dependsOn("generatePluginDescriptor")
//}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            pom {
                name.set("Artifactory NPM Maven Plugin")
                description.set("Maven plugin that uploads NPM dependencies within a Maven project and includes them in the same build info")
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
    sign(publishing.publications["maven"])
}