# octopus-artifactory-npm-gradle-plugin
A Gradle plugin that integrates NPM dependency build information into an existing Gradle build info published to JFrog Artifactory.

## How It Works
### NPM Modules Collection
The plugin collects NPM module information in two ways:
#### From `package.json` (if present):
If the project contains a package.json file (either in the project root or at a configured packageJsonPath), the plugin will:
1. Generate a temporary NPM build info using JFrog CLI.
2. Extract the NPM module data from the generated build info.
3. Mark this data for inclusion in the final Gradle build info.
#### From Dependency Graph:
For all cases, the plugin will:
1. Read the dependencies.json file (specified on dependenciesFilePath setting or project property)
   - Contains a list of direct dependencies and their versions (name and version).
   - If `dependenciesFilePath` is not specified or the file is not found, the plugin will skip resolving NPM build info from dependencies.
   - Example of dependencies.json:
      ```json
      [
         {"name": "dependency-ee", "version": "1.0.0"},
         {"name": "dependency-ei", "version": "1.0.0"}
      ]
      ```
2. Resolve all transitive dependencies using the Release Management Service.
3. For each resolved dependency:
   - Retrieve its build info from Artifactory. 
   - Check whether the build info contains modules of type npm. 
   - Extract those NPM modules if present.

### NPM Modules Integration into Gradle Build Info
After collecting the NPM module information, the plugin will:
1. Append the extracted NPM module data to the existing Gradle build info.
2. Publish the updated Gradle build info to Artifactory.
3. Clean up temporary NPM build info.

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

Artifactory credentials and connection details must be provided as **environment variable** or **system properties**:
- `ARTIFACTORY_URL` or `artifactory.url` - Base URL of the Artifactory instance
- `ARTIFACTORY_DEPLOYER_USERNAME` or `artifactory.username` - Username for Artifactory authentication
- `ARTIFACTORY_DEPLOYER_PASSWORD` or `artifactory.password` - Password for Artifactory authentication
- `ARTIFACTORY_ACCESS_TOKEN` or `artifactory.accessToken` - Access token for Artifactory authentication (alternative to username/password)

### Required Project Properties

The plugin automatically triggers **only if** these project properties are specified:
- `buildInfo.build.name` - Gradle build info name to append
- `buildInfo.build.number` - Gradle build info number to append

Other required properties to be set:
- `component-registry-service-url` - URL of the Component Registry Service (CRS) to fetch component information of NPM dependencies
- `release-management-service-url` - URL of the Release Management Service (RMS) to fetch dependencies information for NPM dependencies

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
        
        // Optional: Directory path of package.json (default: project root)
        packageJsonPath.set("") // or "src/frontend" if package.json is located in src/frontend
        
        // Optional: Skip plugin execution (default: false)
        skip.set(false)
        
        // Optional: Cleanup NPM build info after integration (default: true)
        cleanupNpmBuildInfo.set(true)
    }
}
```

## Development Notes
This plugin hooks into the Gradle build lifecycle and executes after the Artifactory Gradle plugin publishes its build info, ensuring the NPM dependencies are properly integrated into the final build information.
