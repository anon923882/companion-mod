# companion-mod

A NeoForge companion mod for Minecraft 21.1.1 that adds a loyal companion entity which follows the summoner and offers a portable 27-slot inventory UI.

## Features
- **Companion charm**: craftable via data pack or obtainable in creative tabs (Spawn Eggs and Tools) to summon your personal companion.
- **Following behavior**: the companion stays near its owner and will teleport back if it falls behind.
- **Shared storage**: right-click your companion to open a dedicated inventory screen and manage items just like a standard chest.
- **Persistence**: companions are persistent entities and drop any stored items if they die.

## Development
- Target Minecraft `21.1.1` with NeoForge `21.1.209`.
- Gradle wrapper is configured for Gradle `8.14`.

### Preparing the Gradle wrapper

The repository omits the `gradle-wrapper.jar` binary so pull requests stay text-only. Before running any Gradle task, fetch the wrapper jar once:

1. `./scripts/fetch-gradle-wrapper.sh`
2. Run your Gradle commands as usual, e.g. `./gradlew runClient`
