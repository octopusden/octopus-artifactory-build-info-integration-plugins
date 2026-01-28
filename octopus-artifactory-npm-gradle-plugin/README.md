# octopus-artifactory-npm-gradle-plugin
A Gradle plugin that integrates NPM dependency build information into an existing Gradle build info published to JFrog Artifactory.

## Available Tasks
### `integrateNpmBuildInfo`
1. Collect NPM module information:
    - Generate a temporary NPM build info using JFrog CLI.
    - Extract the NPM module data from the NPM build info.
2. Append the extracted NPM module data to the existing Gradle build info.
3. Publish the updated Gradle build info to Artifactory.
4. Clean up temporary NPM build info.

**Automatic Execution:**
- Automatically triggered after build finishes successfully
- Only runs when required project properties (`buildInfo.build.name` and `buildInfo.build.number`) are present
- Only configured if `package.json` file is found at the specified `packageJsonPath` (or project root if not specified)


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

### Required System Properties

Artifactory credentials and connection details must be provided as system properties:

### Required Project Properties

The plugin automatically triggers **only if** these project properties are specified:
- `buildInfo.build.name` - Gradle build info name to append
- `buildInfo.build.number` - Gradle build info number to append

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

## Development Notes
This plugin hooks into the Gradle build lifecycle and executes after the Artifactory Gradle plugin publishes its build info, ensuring the NPM dependencies are properly integrated into the final build information.
