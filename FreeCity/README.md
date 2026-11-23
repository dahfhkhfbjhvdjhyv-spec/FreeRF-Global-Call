# FreeCity

A Unity-based, offline, third-person open-world Android game prototype.

## Selected direction
- Engine: Unity
- Visual target: cinematic presentation, with scalable Android quality settings
- World: large open-world layout, streamed in additive zones
- Platform: Android (ARM64)
- First milestone: a playable vertical slice, not a GTA-scale finished game

## Project setup
Open this folder in a compatible Unity LTS editor. Install Android Build Support, Android SDK & NDK Tools, and OpenJDK through Unity Hub. Open the scene created under `Assets/Scenes` (the initial scene is assembled in the editor).

## Current status
This repository contains the initial gameplay code and project configuration. It is not an APK yet. A reproducible Android build also requires a Unity editor installation and a valid Unity license in the build environment.

## Planned milestones
1. Player movement and follow camera
2. Drivable vehicle prototype
3. Small city block with roads, buildings and collision
4. Interaction and mission framework
5. Traffic, pedestrians and police AI
6. Expand the map using additive scene streaming
7. Android profiling and quality presets

## Performance strategy
Cinematic is the art direction, not a promise of maximum settings on every phone. Android will use scalable quality, baked lighting, LODs, occlusion culling, texture compression and streamed world zones.
