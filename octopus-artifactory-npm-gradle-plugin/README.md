# Artifactory NPM Gradle Plugin

Gradle plugin that uploads NPM dependencies and includes them in Artifactory build info. This plugin executes automatically after the build finishes successfully.

## Usage

### Apply the Plugin

Add the plugin to your `build.gradle.kts`:

```kotlin
plugins {
    id("org.octopusden.octopus.artifactory-npm") version "3.0-SNAPSHOT"
}
```

### Configure the Plugin

Configure the plugin in your `build.gradle.kts`:

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

### Override Build Info via Project Properties

You can override build name and build number using project properties (they take priority over settings):

```bash
./gradlew build \
  -Dartifactory.url=https://artifactory.example.com \
  -Dartifactory.accessToken=YOUR_TOKEN \
  -PbuildInfo.build.name=my-custom-build \
  -PbuildInfo.build.number=1.2.3
```

### Automatic Execution

The plugin automatically executes **after the build finishes successfully**. You don't need to call any specific task - just run your normal build:

```bash
./gradlew build -Dartifactory.url=... -Dartifactory.accessToken=...
```

The plugin will:
1. Wait for the build to complete
2. Generate NPM build info from your `package.json`
3. Integrate the NPM build info with the main Artifactory build info

### Manual Execution

If you want to run the integration manually (without waiting for build finish), you can execute:

```bash
./gradlew integrateNpmBuildInfo \
  -Dartifactory.url=https://artifactory.example.com \
  -Dartifactory.accessToken=YOUR_TOKEN
```

## Configuration Properties

### Settings (in build.gradle.kts)

| Property | Required | Default | Description |
|----------|----------|---------|-------------|
| `buildName` | No* | - | Build name for Artifactory |
| `buildNumber` | No* | - | Build number/version |
| `npmRepository` | No | `"npm"` | Target NPM repository in Artifactory |
| `npmBuildNameSuffix` | No | `"_npm"` | Suffix for NPM build name |
| `packageJsonPath` | No | `""` | Path to package.json (empty = project root) |
| `skip` | No | `false` | Skip plugin execution |
| `cleanupNpmBuildInfo` | No | `true` | Cleanup NPM build info after integration |

\* Can be provided via project properties instead

### System Properties (at runtime)

| Property | Required | Description |
|----------|----------|-------------|
| `artifactory.url` | Yes | Artifactory server URL |
| `artifactory.accessToken` | No** | Artifactory access token |
| `artifactory.username` | No** | Artifactory username |
| `artifactory.password` | No** | Artifactory password |

\** Either `artifactory.accessToken` or both `artifactory.username` and `artifactory.password` must be provided.

### Project Properties (at runtime - override settings)

| Property | Description |
|----------|-------------|
| `buildInfo.build.name` | Override build name from settings |
| `buildInfo.build.number` | Override build number from settings |

## Example Configurations

### Minimal Configuration

```kotlin
// build.gradle.kts
plugins {
    id("org.octopusden.octopus.artifactory-npm") version "3.0-SNAPSHOT"
}

artifactoryNpm {
    settings {
        buildName.set(project.name)
        buildNumber.set(project.version.toString())
    }
}
```

Run with:
```bash
./gradlew build \
  -Dartifactory.url=https://artifactory.example.com \
  -Dartifactory.accessToken=$ARTIFACTORY_TOKEN
```

### Full Configuration

```kotlin
// build.gradle.kts
artifactoryNpm {
    settings {
        buildName.set("my-project")
        buildNumber.set("1.0.0")
        npmRepository.set("npm-local")
        npmBuildNameSuffix.set("_npm_deps")
        packageJsonPath.set("frontend/package.json")
        cleanupNpmBuildInfo.set(true)
    }
}
```

### CI/CD Configuration

In your CI/CD pipeline, you can inject credentials from environment variables:

```bash
# GitLab CI, GitHub Actions, etc.
./gradlew build \
  -Dartifactory.url=$ARTIFACTORY_URL \
  -Dartifactory.accessToken=$ARTIFACTORY_TOKEN \
  -PbuildInfo.build.name=$CI_PROJECT_NAME \
  -PbuildInfo.build.number=$CI_PIPELINE_ID
```

Or using a gradle.properties file (not recommended for credentials):
```properties
# gradle.properties
systemProp.artifactory.url=https://artifactory.example.com
```

## How It Works

1. **Plugin Application**: When you apply the plugin, it registers itself to listen for build completion events
2. **Build Completion**: After your Gradle build finishes successfully
3. **Credential Resolution**: Reads Artifactory credentials from System properties at runtime
4. **Build Info Resolution**: Uses project properties if provided, otherwise falls back to settings
5. **NPM Build Info Generation**: The plugin analyzes your `package.json` and generates build info
6. **Integration**: The NPM build info is merged with the main Artifactory build info
7. **Upload**: The combined build info is uploaded to Artifactory

## Requirements

- Gradle 7.0 or higher
- JDK 11 or higher
- `package.json` file in your project
- Artifactory server with NPM repository

## Troubleshooting

### Plugin doesn't execute

- Check that your build completes successfully (plugin only runs on success)
- Verify `skip` property is not set to `true`
- Check logs for any error messages

### Authentication errors

- Verify your Artifactory credentials are correct
- Ensure you're providing either `artifactory.accessToken` OR both `artifactory.username` and `artifactory.password` as system properties
- Check that your credentials have permission to upload to the NPM repository

### package.json not found

- Verify the `packageJsonPath` is correct
- If empty, ensure `package.json` exists in the project root
- Use absolute or relative paths from project root

### Build name/number not resolved

- Check that you've either configured them in settings or provided them via project properties
- Project properties (`-PbuildInfo.build.name`) take priority over settings
- Ensure the property names are correct: `buildInfo.build.name` and `buildInfo.build.number`

