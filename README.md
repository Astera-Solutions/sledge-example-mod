# 📦 Example Mod (1.21.4)

The **Reference Implementation** for SledgeMC on Minecraft **1.21.4**.

This mod serves as a template and guide for developers to build their own mods using SledgeMC.

## 🏗️ Role
- Demonstrates mod initialization and lifecycle hooks.
- Shows how to use the SledgeMC Event Bus.
- Provides examples of SpongePowered Mixins for Minecraft 1.21.4.

## ✨ Features
- **Version Specific**: Configured to target Minecraft 1.21.4 in `sledge.mod.json`.
- **Runtime Mixins**: Demonstrates targeting Minecraft classes searching for Intermediary names.
- **Maven Integration**: Includes a pre-configured `build.gradle` for easy development.

## 🛠️ Build
To build the mod jar:
```bash
./gradlew clean build
```
The artifact will be located in `build/libs/example-mod-1.0.0.jar`.
