# Artifactory NPM Gradle Plugin

Gradle plugin that uploads NPM dependencies and includes them in Artifactory build info. This plugin executes automatically after the build finishes successfully.

## Usage

### Apply the Plugin

Add the plugin to your `build.gradle.kts`:

```kotlin
plugins {
    id("org.octopusden.octopus.artifactory-npm-gradle-plugin")
}
```
With `settings.gradle.kts`:

```kotlin
pluginManagement {
    plugins {
        id("org.octopusden.octopus.artifactory-npm-gradle-plugin") version settings.extra["octopus-artifactory-npm-maven-plugin.version"] as String
    }
}
```

### Tasks
The plugin provides the following task:
- `integrateNpmBuildInfo`: 
    1. Collect NPM module information:
       - Generate a temporary NPM build info using JFrog CLI.
       - Extract the NPM module data from the NPM build info.
    2. Append the extracted NPM module data to the existing Maven build info.
    3. Publish the updated Maven build info to Artifactory.
    4. Clean up temporary NPM build info.

#### Task execution
Task will be automatically triggered after build finished successfully, **only if** these following project properties are specified:
- `buildInfo.build.name`
- `buildInfo.build.number`

For `integrateNpmBuildInfo`, the task will only be configured if the `package.json` file is found at the specified `packageJsonPath` (or project root if not specified). 
```kotlin
artifactoryNpm {
    settings {
        packageJsonPath.set("path/to/package.json")
    }
}
```

Task can be skipped by specify configuration:
```kotlin
artifactoryNpm {
    settings {
        skip.set(true)
    }
}
```


### Provide Credentials via System Properties

Artifactory credentials are **not** configured in build.gradle. Instead, provide them as system properties when running Gradle:

```bash
# Using access token
./gradlew build \
  -Dartifactory.url=https://artifactory.example.com \
  -Dartifactory.accessToken=YOUR_TOKEN

# OR using username/password
./gradlew build \
  -Dartifactory.url=https://artifactory.example.com \
  -Dartifactory.username=YOUR_USERNAME \
  -Dartifactory.password=YOUR_PASSWORD
```

### Optional Configurations

Optional configurations can be set in `build.gradle.kts`:

```kotlin
artifactoryNpm {
    settings {
        // Optional: Build information (can be overridden by project properties)
        buildName.set(project.name)
        buildNumber.set(project.version.toString())
        
        // Optional: NPM repository (default: "npm")
        npmRepository.set("npm")
        
        // Optional: NPM build name suffix (default: "_npm")
        npmBuildNameSuffix.set("_npm")
        
        // Optional: Path to package.json (default: project root)
        packageJsonPath.set("") // or "path/to/package.json"
        
        // Optional: Skip plugin execution (default: false)
        skip.set(false)
        
        // Optional: Cleanup NPM build info after integration (default: true)
        cleanupNpmBuildInfo.set(true)
    }
}
```