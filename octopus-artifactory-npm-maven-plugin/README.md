# octopus-artifactory-npm-maven-plugin
A Maven plugin that integrates NPM dependency build information into an existing Maven build info published to JFrog artifactory.
Primarily intended for Maven projects that also use NPM.

## Available Mojo Goals
### `integrate-npm-build-info`
1. Collect NPM module information:
    - Generate a temporary NPM build info using JFrog CLI.
    - Extract the NPM module data from the NPM build info.
2. Append the extracted NPM module data to the existing Maven build info.
3. Publish the updated Maven build info to Artifactory.
4. Clean up temporary NPM build info.

## Usage
Add the plugin to your `pom.xml`:
```xml
<plugin>
    <groupId>org.octopusden.octopus</groupId>
    <artifactId>octopus-artifactory-npm-maven-plugin</artifactId>
    <version>${octopus-artifactory-npm-maven-plugin.version}</version>
    <executions>
        <execution>
            <id>integrate-npm-build-info</id>
            <phase>deploy</phase>
            <goals>
                <goal>integrate-npm-build-info</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```
Required properties:
- `artifactoryUrl`
- `artifactoryUsername` & `artifactoryPassword` or `artifactoryAccessToken`
- `artifactory.build.name` - Maven build info name to append
- `artifactory.build.version` - Maven build info version to append

## Planned Features

> ⚠️ **Note:** The Gradle plugin (`octopus-artifactory-npm-gradle-plugin`) already supports collecting NPM modules from the dependency. This feature is planned for the Maven plugin but not yet implemented. See [TECH_DEBT.md](../docs/TECH_DEBT.md) for details.

## Development Notes
This plugin uses a `MavenExecutionListener` and runs on the `sessionEnded` event.
This ensures integration happens after the Artifactory Maven plugin publishes its build info (which occurs at the very end of the build lifecycle).