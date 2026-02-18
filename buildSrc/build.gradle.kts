plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(gradleApi())
    implementation("org.mock-server:mockserver-client-java:5.11.1")
    implementation("com.google.guava:guava:32.1.3-jre")
    implementation("org.apache.httpcomponents:httpclient:4.5.14")
}

