# Jarry's Holy Bible

A readable in-game Holy Bible for Minecraft Java. It adds a Bible item with the complete King James Version, which you can open and read by Testament, Book, and Chapter.

- Rare in village chests
- Crafted from a book and a gold ingot
- Given to every player once on first join
- Required on both the client and the server

The current release is **1.0.0**. The Fabric, NeoForge, and Forge builds share the same mod id (`jarrys_holy_bible`) and item id (`jarrys_holy_bible:bible`).

## Supported versions

| Loader | Minecraft versions | Notes |
| --- | --- | --- |
| Fabric | 26.3, 26.2, 26.1.2, 1.21.11, 1.21.10 | Requires [Fabric API](https://modrinth.com/mod/fabric-api) |
| NeoForge | 26.3, 26.2, 26.1.2, 1.21.11, 1.21.10 | |
| Forge | 1.21.11, 1.21.10 | No Forge build for the 26.x line in this release |

Java 21 is required for the 1.21.x builds. Java 25 is required for the 26.x builds.

## Install

1. Install the loader for your edition of the game (Fabric Loader, NeoForge, or Minecraft Forge) that matches your Minecraft version.
2. For Fabric, also install Fabric API for that same game version.
3. Download the jar for your loader and game version and put it in the `mods` folder.
4. Use the same mod on the client and the server.

## Report bugs and get help

- **Bug reports and crashes:** open a GitHub Issue: https://github.com/ojarry/jarrys-holy-bible/issues
- **Questions and support:** use GitHub Discussions: https://github.com/ojarry/jarrys-holy-bible/discussions

Please include your Minecraft version, loader (Fabric, NeoForge, or Forge), loader version, and mod version. For crashes, attach the latest log or crash report.

## Source layout

This repository holds all three loader ports:

- `fabric/` — Fabric (Loom) builds for each supported game version
- `neoforge/` — NeoForge builds
- `forge/` — Minecraft Forge builds

Each loader folder has shared code in `common/`, version-specific bridges in `shims/`, and one Gradle project per game version under `versions/<minecraft>/`. From a version folder you can run `./gradlew build`. The `build_all.sh` and `client_smoke.sh` scripts at the loader root run the full test set.

## Bible text

The bundled Bible text is the **King James Version** (Authorized Version, 1611). It is in the public domain in most countries and is **not** covered by the MIT license that applies to the mod code.

The text packaged in the mod was prepared from [`en_kjv.json`](https://github.com/thiagobodruk/bible) (thiagobodruk/bible). In the United Kingdom the Authorized Version is under Crown letters patent; it is included here as the widely used public-domain text.

To regenerate the packaged book files, set `BIBLE_KJV_JSON` to that `en_kjv.json` and run `fabric/tools/build_bible.py`.

## License

The mod code and the original item icon are licensed under the MIT License. See [LICENSE](LICENSE). Copyright (c) 2026 Jarry.
