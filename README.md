# MasihMikirNamaPlugin

Minecraft server utility plugin for Spigot-compatible servers. Built with Gradle and Java 17.

[![Release](https://img.shields.io/github/v/release/fadhlanel/Still-Thinking-of-a-Name)](https://github.com/fadhlanel/Still-Thinking-of-a-Name/releases/latest)
[![Build](https://github.com/fadhlanel/Still-Thinking-of-a-Name/actions/workflows/release.yml/badge.svg)](https://github.com/fadhlanel/Still-Thinking-of-a-Name/actions/workflows/release.yml)

## Features

- Welcome message and join firework
- Persistent boss bar visibility preference
- Starter kit with configurable items, armor, and offhand item
- Starter kit administration tools
- Last death location and delayed return teleport
- Scheduled server restart system
- GitHub release update checker for administrators

## Requirements

- Java 17 or newer
- Spigot API 1.20.4-compatible server

## Installation

1. Download `MasihMikirNamaPlugin.jar` from [GitHub Releases](https://github.com/fadhlanel/Still-Thinking-of-a-Name/releases/latest).
2. Put the JAR in your server's `plugins` directory.
3. Start or restart the server.
4. Edit the generated `plugins/MasihMikirNamaPlugin/config.yml` if needed.

## Commands

| Command | Description |
| --- | --- |
| `/togglebar` | Show or hide the boss bar for yourself |
| `/returnlastdeath` | Return to your latest death location |
| `/starterkit` | Claim your starter kit |
| `/sk help` | Show starter kit admin commands |
| `/sk clone` | Copy your inventory into the starter kit |
| `/sk give <target>` | Give starter kits |
| `/sk remove <target>` | Remove claimed status |
| `/sk reset <target>` | Reset starter kit state |
| `/sk reload` | Reload starter kit configuration |
| `/autorestart` | Manage scheduled restarts |
| `/pluginupdate` | Check plugin version and release link |

`/autorestart` aliases: `/sar`, `/simpleautorestart`.

## Permissions

| Permission | Default | Description |
| --- | --- | --- |
| `masihmikirnamaplugin.autorestart` | Operator | Use auto restart commands |
| `masihmikirnamaplugin.update` | Operator | Check for plugin updates |

Starter kit administration uses operator status, matching current plugin behavior.

## Configuration

Configuration is stored in `src/main/resources/config.yml` and copied to the server on first startup. It controls:

- Boss bar preferences
- Starter kit contents and player state
- First-join timestamps and warning counts
- Restart schedules, warnings, titles, and console commands

## Update system

On startup and every six hours, plugin asynchronously checks GitHub Releases API. Failed requests are ignored safely. No JAR is downloaded or replaced automatically.

Operators with `masihmikirnamaplugin.update` receive an in-game notice on join when newer release exists. `/pluginupdate` shows current version, latest version, and download link.

Latest releases: https://github.com/fadhlanel/Still-Thinking-of-a-Name/releases/latest

## Build

```bash
./gradlew clean build
```

Windows PowerShell:

```powershell
.\gradlew.bat clean build
```

Plugin artifact:

```text
build/libs/MasihMikirNamaPlugin.jar
```

## Release

Release workflow runs only for tags matching `v*.*.*`:

```bash
git add .
git commit -m "Prepare release"
git push origin main
git tag v1.0.0
git push origin v1.0.0
```

GitHub Actions builds the plugin, validates the exact plugin artifact, and publishes one GitHub Release asset. Failed builds do not create releases.

## Development

Source code is under `src/main/java`. Resource files are under `src/main/resources`. Gradle wrapper is included, so local Gradle installation is not required.

## Contributing

Open an issue or pull request in the [GitHub repository](https://github.com/fadhlanel/Still-Thinking-of-a-Name).

## License

Released under the [MIT License](LICENSE).
