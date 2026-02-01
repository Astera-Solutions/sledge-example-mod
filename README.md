# Example Mod (1.21.8)

The **Reference Implementation** for SledgeMC on Minecraft **1.21.8**.

This mod serves as a template and guide for developers to build their own mods using SledgeMC.

## Role
- Demonstrates mod initialization and lifecycle hooks.
- Shows how to use the SledgeMC Event Bus.
- Provides examples of SpongePowered Mixins for Minecraft 1.21.8.

## Features
- **Version Specific**: Configured to target Minecraft 1.21.8 in `sledge.mod.json`.
- **Runtime Mixins**: Demonstrates targeting Minecraft classes searching for Intermediary names.
- **Maven Integration**: Includes a pre-configured `build.gradle` for easy development.
- **Mappings**: We're using Mojmaps right now but we can make our own mappings later.

## Build
To build the mod jar:
```bash
./gradlew clean build
```
**Note:** *All the details are explained in the comment notes within the classes.*
