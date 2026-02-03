# Example Mod (1.21.11)

The **Reference Implementation** for SledgeMC on Minecraft **1.21.11**.

This mod serves as a template and guide for developers to build their own mods using SledgeMC.

## Role
- Demonstrates mod initialization and lifecycle hooks.
- Shows how to use the SledgeMC Event Bus.
- Provides examples of SpongePowered Mixins for Minecraft 1.21.11.

## Features
- **Version Specific**: Configured to target Minecraft 1.21.11 in `sledge.mod.json`.
- **Runtime Mixins**: Demonstrates targeting Minecraft classes searching for Intermediary names.
- **Maven Integration**: Includes a pre-configured `build.gradle` for easy development.

## 🛠️ Build
To build the mod jar:
```bash
./gradlew clean build
```
