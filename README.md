# Sledge Loader

The **Core Engine** of SledgeMC for Minecraft

This version of **Sledge Loader** is specifically built and optimized for Minecraft

## Role
- Discovers and loads mods from the `mods/` folder.
- Manages dependencies and conflict detection.
- Handles bytecode transformation and runtime remapping.

## Features
- **Version Specific**: Hardcoded to target and support Minecraft.
- **Runtime Mapping Service**: Automatically downloads and applies Intermediary mappings.
- **Mixin Integration**: Bootstraps the SpongePowered Mixin framework.
- **Mod Discovery**: Scans and validates `sledge.mod.json` metadata.

## Build
To build the Loader jar:
```bash
./gradlew clean build
```
