# Companion Auto Replanter

A minimal NeoForge mod for Minecraft 1.21.1 that automatically replants mature crops right after you harvest them. It follows the
official NeoForge userdev/MDK setup (no custom frameworks, mixins, or client assets) so it stays small, readable, and easy to audit.

## Building (lightweight + by the book)

1. Install JDK 21 (matching the toolchain in `build.gradle`).
2. Install a local Gradle 8.14+ (wrapper files are intentionally omitted to avoid committing binaries).
3. From the project root run `gradle build`. The produced mod jar will be in `build/libs/`.

This mirrors the steps from the official NeoForge docs while keeping the repository lean—only source, metadata, and Gradle config.

## Why it stays lightweight

- Server-only logic: a single event handler on `BlockEvent.BreakEvent` so there is no client footprint.
- No reflection, mixins, or capability registries—just standard NeoForge hooks and vanilla crop state helpers.
- No textures, models, data packs, or language files beyond the required `mods.toml`/`pack.mcmeta`.
- The handler immediately cancels the original break, drops the vanilla loot for non-creative players, and replaces the block with a stage-0 crop; no extra ticks, schedulers, or chunk scans.

## How it works

The mod listens for crop break events on the server. When a player harvests a fully grown crop planted on farmland, the mature crop
drops its normal items (unless the player is in creative mode) and is immediately replaced with a freshly planted seed at growth
stage 0. This keeps fields planted without extra clicks while keeping the mod lightweight.
