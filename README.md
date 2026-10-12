# MasihMikirNamaPlugin

Minecraft server utility plugin for Spigot-compatible servers. Built with Gradle and Java 17.

[![GitHub Release](https://img.shields.io/github/v/release/fadhlanel/Still-Thinking-of-a-Name?style=for-the-badge)](https://github.com/fadhlanel/Still-Thinking-of-a-Name/releases)
[![Build](https://img.shields.io/github/actions/workflow/status/fadhlanel/Still-Thinking-of-a-Name/release.yml?label=build&style=for-the-badge)](https://github.com/fadhlanel/Still-Thinking-of-a-Name/actions/workflows/release.yml)

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
| `/autorestart` | Manage scheduled restarts (`in 30m`, `blindness off`) |
| `/nmmupdate` | Check plugin version and release link |

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

On startup and every six hours, plugin asynchronously checks GitHub Releases API and logs each check to console. Failed requests are ignored safely. No JAR is downloaded or replaced automatically.

Operators with `masihmikirnamaplugin.update` receive an in-game notice on join when newer release exists. `/nmmupdate` shows current version, latest version, and download link.

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

## Development

Source code is under `src/main/java`. Resource files are under `src/main/resources`. Gradle wrapper is included, so local Gradle installation is not required.

## Contributing

Open an issue or pull request in the [GitHub repository](https://github.com/fadhlanel/Still-Thinking-of-a-Name).

## License

Released under the [MIT License](LICENSE).
