# Octopus Artifactory Build Info Integration Plugins

A collection of plugins and services for integrating additional modules to existing build information produced by Maven and Gradle on JFrog Artifactory.

## Project Structure

This project consists of **3 main modules** and **1 test module**:

### `build-info-integration-core`
The **core integration service used by both Maven and Gradle plugins**. This module provides the foundational functionality for build info integration.

**Functionality:**
- Integrates NPM build info into existing build info based on the specified `package.json`
  - Generates NPM build info using JFrog CLI
  - Updates the existing build info generated from Maven or Gradle plugins
  - Publishes the updated build info back to Artifactory

This service acts as the shared foundation that both plugins depend on, ensuring consistent behavior across Maven and Gradle projects.

### `octopus-artifactory-npm-maven-plugin`
A Maven plugin that integrates NPM dependencies into Maven build info.

**For detailed usage instructions, see:** [`octopus-artifactory-npm-maven-plugin/README.md`](./octopus-artifactory-npm-maven-plugin/README.md)

**Key Features:**
- Automatically runs during the `deploy` phase
- Appends NPM module data to existing Maven build info
- Uses a `MavenExecutionListener` to ensure execution after Artifactory Maven plugin publishes

### `octopus-artifactory-npm-gradle-plugin`
A Gradle plugin that integrates NPM dependencies into Gradle build info.

**For detailed usage instructions, see:** [`octopus-artifactory-npm-gradle-plugin/README.md`](./octopus-artifactory-npm-gradle-plugin/README.md)

**Key Features:**
- Automatically triggered after build finishes successfully
- Configurable via `artifactoryNpm` extension

### `ft` (Functional Tests)
Functional tests that run on OpenShift (OKD) to verify end-to-end integration of both Maven and Gradle plugins.

## Running Functional Tests

The functional tests (`ft` module) run on OKD and require specific parameters to be activated.

To activate the functional tests, specify the `platform` parameter:

```bash
./gradlew :ft:test -Ptest.platform=okd
```

### Required Parameters

When running tests on OKD, the following parameters are required:

**Via Gradle properties:**
```bash
./gradlew :ft:test \
  -Ptest.platform=okd \
  -Pdocker.registry=<your-docker-registry> \
  -Pokd.active-deadline-seconds=<timeout-seconds> \
  -Pokd.project=<okd-namespace> \
  -Pokd.cluster-domain=<cluster-domain>
```

**Or via environment variables:**
```bash
export TEST_PLATFORM=okd
export DOCKER_REGISTRY=<your-docker-registry>
export OKD_ACTIVE_DEADLINE_SECONDS=<timeout-seconds>
export OKD_PROJECT=<okd-namespace>
export OKD_CLUSTER_DOMAIN=<cluster-domain>

./gradlew :ft:test
```

**Optional parameter:**
- `OKD_WEB_CONSOLE_URL` / `-Pokd.web-console-url`: OpenShift web console URL