# Companion Mod

A NeoForge companion mod for Minecraft 1.21.1 that adds a companion entity which follows the player and has an accessible inventory.

## Features

- **Companion Entity**: A friendly companion that follows you around
- **Follow Behavior**: Automatically follows the player who first interacts with it
- **Inventory System**: 27-slot inventory (3x9 grid) accessible by right-clicking
- **Persistent Data**: Companion remembers its owner and inventory contents across game sessions

## Requirements

- **Java 21** or higher
- **Minecraft 1.21.1**
- **NeoForge 21.1.209**

## Building on Kubuntu/Linux

### Prerequisites

Install Java 21 if you haven't already:

```bash
sudo apt update
sudo apt install openjdk-21-jdk
```

Verify installation:

```bash
java -version
```

### Clone and Build

1. Clone the repository:

```bash
git clone https://github.com/anon923882/companion-mod.git
cd companion-mod
```

2. Make the Gradle wrapper executable:

```bash
chmod +x gradlew
```

3. Build the mod:

```bash
./gradlew build
```

The compiled mod will be located at:
```
build/libs/companionmod-1.0.0.jar
```

### Development and Testing

#### Run the Client

To test the mod in a development environment:

```bash
./gradlew runClient
```

This will launch Minecraft with the mod loaded.

#### Run the Server

To test on a dedicated server:

```bash
./gradlew runServer
```

## Installation

1. Build the mod using the instructions above
2. Copy `build/libs/companionmod-1.0.0.jar` to your Minecraft `mods` folder
3. Make sure you have NeoForge 21.1.209+ installed
4. Launch Minecraft

## Usage

1. **Spawn a Companion**: Use `/summon companionmod:companion` or a spawn egg (if you add one)
2. **Claim a Companion**: Right-click an unowned companion to claim it
3. **Access Inventory**: Right-click your companion to open its inventory
4. **Follow Behavior**: The companion will automatically follow you, staying 3-10 blocks away

## Current Limitations

### Embedded Textures

The entity skin and GUI background are embedded directly inside
`src/main/java/com/yourname/companionmod/client/CompanionTextures.java` as Base64 strings.
This keeps the repository free from binary files (per Codex restrictions) while still
shipping the reference art extracted from the legacy mod. The current companion skin
is the `medieval-barmaid.png` variant from `entities/female`, and the inventory panel
comes from `textures/inventory.png`. If you want to swap in new artwork, replace the
Base64 strings in that class with the encoded contents of your preferred PNG files.

## Project Structure

```
companion-mod/
├── src/main/java/com/yourname/companionmod/
│   ├── CompanionMod.java          # Main mod class
│   ├── entity/
│   │   ├── ModEntities.java       # Entity registration
│   │   ├── custom/
│   │   │   └── CompanionEntity.java   # Companion logic
│   │   └── client/
│   │       └── CompanionRenderer.java # Entity renderer
│   ├── menu/
│   │   ├── ModMenuTypes.java      # Menu registration
│   │   └── CompanionMenu.java     # Inventory menu
│   └── screen/
│       └── CompanionScreen.java   # Inventory GUI
├── src/main/resources/
│   ├── META-INF/
│   │   └── neoforge.mods.toml     # Mod metadata
│   ├── assets/companionmod/
│   │   └── lang/
│   │       └── en_us.json         # English translations
│   └── pack.mcmeta                # Resource pack metadata
├── build.gradle                   # Build configuration
├── settings.gradle                # Project settings
└── gradle.properties              # Mod properties
```

## Troubleshooting

### Build Errors

If you encounter build errors:

```bash
./gradlew clean build --refresh-dependencies
```

### Java Version Issues

Make sure Java 21 is set as default:

```bash
sudo update-alternatives --config java
```

### Permission Denied on gradlew

If you get permission denied:

```bash
chmod +x gradlew
```

## License

MIT License - Feel free to modify and distribute

## Contributing

Feel free to submit pull requests or open issues for bugs and feature requests.

## Roadmap

- [ ] Add entity textures
- [ ] Add GUI textures
- [ ] Add spawn egg
- [ ] Add companion commands (sit, stay)
- [ ] Add companion health/feeding system
- [ ] Add companion customization options
- [ ] Add different companion types/variants