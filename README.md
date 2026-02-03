# ⚙️ Sledge Loader (1.21.8)

The **Core Engine** of SledgeMC for Minecraft **1.21.8**.

This version of **Sledge Loader** is specifically built and optimized for Minecraft 1.21.8.

## 🏗️ Role
- Discovers and loads mods from the `mods/` folder.
- Manages dependencies and conflict detection.
- Handles bytecode transformation and runtime remapping.

## ✨ Features
- **Version Specific**: Hardcoded to target and support MC 1.21.8.
- **Runtime Mapping Service**: Automatically downloads and applies Intermediary mappings.
- **Mixin Integration**: Bootstraps the SpongePowered Mixin framework.
- **Mod Discovery**: Scans and validates `sledge.mod.json` metadata.

## 🛠️ Build
To build the Loader jar:
```bash
./gradlew clean build
```
The shadow (all-in-one) artifact will be located in `build/libs/sledge-loader-1.21.8-all.jar`.
