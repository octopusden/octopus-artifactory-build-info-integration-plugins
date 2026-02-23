# Architecture & Ecosystem Diagram

This document describes how `octopus-artifactory-build-info-integration-plugins` relates to other Octopus projects in the ecosystem.

## High-Level Architecture

```mermaid
flowchart TB
    subgraph "Build Tools"
        GRADLE["Gradle Build"]
        MAVEN["Maven Build"]
    end

    subgraph "Dependencies Generation (upstream)"
        RM_GRADLE_PLUGIN["octopus-rm-gradle-plugin"]
    end

    subgraph "octopus-artifactory-build-info-integration-plugins"
        subgraph "Gradle Plugin Module"
            GRADLE_PLUGIN["octopus-artifactory-npm-gradle-plugin"]
            INTEGRATE_TASK["IntegrateNpmBuildInfoTask"]
            DEPS_FILE["dependencies.json<br/>(List&lt;DependencyVersion&gt;)<br/>[{name, version}, ...]"]
        end

        subgraph "Maven Plugin Module"
            MAVEN_PLUGIN["octopus-artifactory-npm-maven-plugin"]
            MOJO["ArtifactoryNpmMavenPluginMojo"]
        end

        subgraph "Core Module"
            INTEGRATION_SVC["NpmBuildInfoIntegrationService"]
            DEPS_RESOLVER["DependenciesBuildInfoResolver"]
            BUILD_INFO_SVC["ArtifactoryBuildInfoService"]
            JFROG_CLI_SVC["JFrogNpmCliService"]
        end
    end

    subgraph "External Octopus Services"
        RM_SERVICE["octopus-release-management-service<br/>(REST API)"]
        CR_SERVICE["octopus-components-registry-service<br/>(REST API)"]
    end

    subgraph "External Infrastructure"
        ARTIFACTORY["JFrog Artifactory<br/>(Build Info Storage)"]
        JFROG_CLI["JFrog CLI"]
    end

    %% Upstream: octopus-rm-gradle-plugin generates dependencies file
    RM_GRADLE_PLUGIN -->|"Generates"| DEPS_FILE

    %% Build tools trigger plugins
    GRADLE -->|"Applies"| GRADLE_PLUGIN
    MAVEN -->|"Applies"| MAVEN_PLUGIN

    %% Gradle plugin reads dependencies file & delegates to core
    GRADLE_PLUGIN --> INTEGRATE_TASK
    INTEGRATE_TASK -->|"Reads"| DEPS_FILE
    INTEGRATE_TASK -->|"Delegates to"| INTEGRATION_SVC

    %% Maven plugin delegates to core
    MAVEN_PLUGIN --> MOJO
    MOJO -->|"Delegates to"| INTEGRATION_SVC

    %% Core: integration service orchestrates
    INTEGRATION_SVC --> JFROG_CLI_SVC
    INTEGRATION_SVC --> BUILD_INFO_SVC
    INTEGRATION_SVC -->|"Passes direct<br/>dependencies"| DEPS_RESOLVER

    %% DependenciesBuildInfoResolver calls external services
    DEPS_RESOLVER -->|"1. Get transitive<br/>dependencies (BFS)"| RM_SERVICE
    DEPS_RESOLVER -->|"2. Get distribution type<br/>(explicit/external flags)"| CR_SERVICE

    %% Build info operations
    JFROG_CLI_SVC -->|"npm install &<br/>publish build info"| JFROG_CLI
    BUILD_INFO_SVC -->|"Get/Merge/Upload<br/>build info"| ARTIFACTORY
    JFROG_CLI -->|"Publishes NPM<br/>build info"| ARTIFACTORY

    classDef upstream fill:#E8F5E9,stroke:#2E7D32,color:#1B5E20
    classDef plugin fill:#E3F2FD,stroke:#1565C0,color:#0D47A1
    classDef core fill:#FFF3E0,stroke:#E65100,color:#BF360C
    classDef external fill:#F3E5F5,stroke:#6A1B9A,color:#4A148C
    classDef infra fill:#FFEBEE,stroke:#B71C1C,color:#B71C1C

    class RM_GRADLE_PLUGIN upstream
    class GRADLE_PLUGIN,INTEGRATE_TASK,DEPS_FILE,MAVEN_PLUGIN,MOJO plugin
    class CORE,INTEGRATION_SVC,DEPS_RESOLVER,BUILD_INFO_SVC,JFROG_CLI_SVC core
    class RM_SERVICE,CR_SERVICE external
    class ARTIFACTORY,JFROG_CLI infra
```

## Build Name Calculation

The `DependenciesBuildInfoResolver` calculates the Artifactory build name for each dependency using the component's distribution type from the Components Registry:

```
Build Name Format: {component}_{explicitFlag}{externalFlag}

Where:
  - explicitFlag = "e" if distribution.explicit == true, else "i"
  - externalFlag = "e" if distribution.external == true, else "i"

Examples:
  - "my-component_ii" → implicit distribution, internal
  - "my-component_ei" → explicit distribution, internal
  - "my-component_ee" → explicit distribution, external
```

