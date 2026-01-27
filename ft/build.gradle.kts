plugins {
    id("org.octopusden.octopus.oc-template") version "1.0-SNAPSHOT"
}

description = "Functional tests for Artifactory NPM Maven and Gradle plugins"

ext {
    System.getenv().let {
        set("dockerRegistry", System.getenv().getOrDefault("DOCKER_REGISTRY", project.properties["docker.registry"]))
        set("okdActiveDeadlineSeconds", it.getOrDefault("OKD_ACTIVE_DEADLINE_SECONDS", properties["okd.active-deadline-seconds"]))
        set("okdProject", it.getOrDefault("OKD_PROJECT", properties["okd.project"]))
        set("okdClusterDomain", it.getOrDefault("OKD_CLUSTER_DOMAIN", properties["okd.cluster-domain"]))
        set("okdWebConsoleUrl", (it.getOrDefault("OKD_WEB_CONSOLE_URL", properties["okd.web-console-url"]) as String).trimEnd('/'))
    }

//    val mandatoryProperties = listOf("dockerRegistry", "okdActiveDeadlineSeconds", "okdProject", "okdClusterDomain")
    val mandatoryProperties = listOf<String>()

    val undefinedProperties = mandatoryProperties.filter { (project.ext[it] as String).isBlank() }
    if (undefinedProperties.isNotEmpty()) {
        throw IllegalArgumentException(
            "Start gradle build with" +
                    (if (undefinedProperties.contains("dockerRegistry")) " -Pdocker.registry=..." else "") +
                    (if (undefinedProperties.contains("octopusGithubDockerRegistry")) " -Poctopus.github.docker.registry=..." else "") +
                    (if (undefinedProperties.contains("okdActiveDeadlineSeconds")) " -Pokd.active-deadline-seconds=..." else "") +
                    (if (undefinedProperties.contains("okdProject")) " -Pokd.project=..." else "") +
                    (if (undefinedProperties.contains("okdClusterDomain")) " -Pokd.cluster-domain=..." else "") +
                    " or set env variable(s):" +
                    (if (undefinedProperties.contains("dockerRegistry")) " DOCKER_REGISTRY" else "") +
                    (if (undefinedProperties.contains("octopusGithubDockerRegistry")) " OCTOPUS_GITHUB_DOCKER_REGISTRY" else "") +
                    (if (undefinedProperties.contains("okdActiveDeadlineSeconds")) " OKD_ACTIVE_DEADLINE_SECONDS" else "") +
                    (if (undefinedProperties.contains("okdProject")) " OKD_PROJECT" else "") +
                    (if (undefinedProperties.contains("okdClusterDomain")) " OKD_CLUSTER_DOMAIN" else "")
        )
    }
}

fun String.getExt() = project.ext[this] as String
fun String.getPort() = when (this) {
    "artifactory" -> 8081
    "postgres" -> 5432
    else -> throw Exception("Unknown service '$this'")
}
fun getOkdInternalHost(serviceName: String) = "${ocTemplate.getPod(serviceName)}-service:${serviceName.getPort()}"

ocTemplate {
    workDir.set(layout.buildDirectory.dir("okd"))
    clusterDomain.set("okdClusterDomain".getExt())
    namespace.set("okdProject".getExt())

    prefix.set("ft")

    "okdWebConsoleUrl".getExt().takeIf { it.isNotBlank() }?.let{
        webConsoleUrl.set(it)
    }

    service("postgres") {
        templateFile.set(rootProject.layout.projectDirectory.file("okd/postgres.yaml"))
        parameters.set(mapOf(
            "ACTIVE_DEADLINE_SECONDS" to "okdActiveDeadlineSeconds".getExt(),
            "DOCKER_REGISTRY" to "dockerRegistry".getExt()
        ))
    }

    service("artifactory") {
        templateFile.set(rootProject.layout.projectDirectory.file("okd/artifactory.yaml"))
        parameters.set(mapOf(
            "ACTIVE_DEADLINE_SECONDS" to "okdActiveDeadlineSeconds".getExt(),
            "POSTGRES_HOST" to getOkdInternalHost("postgres")
        ))
        dependsOn.set(listOf("postgres"))
    }
}

dependencies {
    testImplementation(project(":octopus-artifactory-npm-maven-plugin"))
    testImplementation(project(":octopus-artifactory-npm-gradle-plugin"))

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.octopusden.octopus.octopus-external-systems-clients:artifactory-client:2.0.75")
    testApi("com.platformlib:platformlib-process-local:0.1.4")
}

tasks.test {
    useJUnitPlatform()

    dependsOn(":octopus-artifactory-npm-maven-plugin:publishToMavenLocal")
    dependsOn(":octopus-artifactory-npm-gradle-plugin:publishToMavenLocal")

    doFirst {
//        systemProperty("artifactoryTestHost", "http://${ocTemplate.getOkdHost("artifactory")}")
        systemProperty("artifactoryTestHost", "http://localhost:8082")
        systemProperty("octopusArtifactoryIntegrationPluginVersion", project.version.toString())
    }
}
