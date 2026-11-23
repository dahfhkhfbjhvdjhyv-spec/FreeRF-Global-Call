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

## Prototype controls
- Walk: left-side touchscreen drag; keyboard WASD/arrows in the Unity Editor
- Run: Left Shift in the Editor
- Enter nearby car: E
- Drive: right-side touchscreen drag; keyboard WASD/arrows in the Editor
- Exit car: F

## Generate and test the scene
Open the `FreeCity` folder in Unity 2022.3.50f1, wait for packages to resolve, then select **FreeCity > Generate Prototype City Scene**. Open `Assets/Scenes/FreeCity_Prototype.unity` and press Play.

## Android build
The repository includes a GitHub Actions workflow at **Actions > Free City Android APK**. It requires a valid Unity Personal license file stored as the repository Actions secret `UNITY_LICENSE`. Run the workflow manually after adding that secret. The APK is uploaded as the `FreeCity-Android-APK` artifact.

## Current status
The initial gameplay prototype source and a reproducible Android build entry point are committed. No APK has been built or runtime-tested yet; Unity compilation and the Android build still require the Unity license secret and a successful Actions run.

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
