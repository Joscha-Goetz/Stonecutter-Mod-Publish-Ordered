# Publish Ordered

A Gradle plugin that publishes multi-version Minecraft mods one version after another, oldest first, so the newest Minecraft version ends up on top on Modrinth and CurseForge.

## Requirements

- Gradle 9 or newer
- A multi-project build whose projects are named after Minecraft versions, e.g. `1.20.1`, `1.20.1-fabric` or `:fabric:1.20.1` (Stonecutter works out of the box)
- The [Mod Publish Plugin](https://github.com/modmuss50/mod-publish-plugin) in each version project

## Usage

In `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}
```

In the root build script (`stonecutter.gradle.kts` for Stonecutter projects):

```kotlin
plugins {
    id("de.joscha-goetz.publish-ordered") version "1.1.0"
}
```

Then run:

| Command | Effect |
|---|---|
| `./gradlew publishOrdered` | Publish to all platforms |
| `./gradlew publishOrdered -PpublishPlatform=Modrinth` | Modrinth only |
| `./gradlew publishOrdered -PpublishPlatform=Curseforge` | CurseForge only |
| add `-PpublishDryRun` | Dry run |

## License

MIT
