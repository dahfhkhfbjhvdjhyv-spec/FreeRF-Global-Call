# FreeCity

Unity-based offline open-world Android game prototype, targeting a playable first vertical slice.

## Game direction
- Engine: Unity 2022.3.50f1
- Platform: Android ARM64, landscape
- Mode: offline single-player
- World: 500 x 500 m prototype city block
- Art direction: cinematic target with scalable mobile settings

## Current gameplay systems in source
- Third-person player controller and follow camera
- Visible on-screen movement pad plus gas, brake, steering, enter and exit buttons
- Drivable starter vehicle with camera handoff
- Six lightweight ambient traffic cars
- Twelve low-cost pedestrian walkers on sidewalk loops
- Expanded district road grid and shared-material streetlights
- First Drive mission with a checkpoint and completion state
- Procedural prototype city scene generator

## Generate and play in Unity
1. Open the `FreeCity` folder in Unity 2022.3.50f1.
2. Install Android Build Support, Android SDK & NDK Tools, and OpenJDK through Unity Hub if building for Android.
3. Wait for packages to resolve.
4. Select **FreeCity > Generate Prototype City Scene**.
5. Open `Assets/Scenes/FreeCity_Prototype.unity` and press Play.
6. For editor keyboard testing: WASD/arrows move/drive, E enters a nearby car, F exits.

## Current prototype scope
The generated 500 x 500 m scene includes a central avenue, a secondary road grid, simple block buildings, streetlights, ambient traffic, pedestrians, a drivable starter car, and a first checkpoint mission. These are procedural placeholder assets; they are not final cinematic art or realistic traffic simulation.

## Android APK
The repository includes **Actions > Free City Android APK**. It requires a valid Unity Personal license file in the repository Actions secret named `UNITY_LICENSE`. Run the workflow manually after adding the secret. A successful run uploads `FreeCity-Android-APK`.

## Honest build status
The gameplay source and scene generator are committed. This environment has not run Unity Editor, compiled the project, launched gameplay, or produced an APK. Those checks must pass before this can be called a verified playable Android release. The city, traffic, mission and controls are an initial vertical slice—not a finished GTA-scale game.

## Next milestones
- Test and fix Unity compile/runtime issues
- Add pedestrian AI, traffic rules, collision/vehicle physics and multiple missions
- Expand districts with streamed scenes and optimized assets
- Add LODs, baked lighting, occlusion and device-specific quality presets
- Profile on real Android devices and publish a signed APK

## Performance approach
Keep the mobile build lightweight first: limited ambient traffic, simple materials, scalable quality, and streamed districts. "Cinematic" is the visual target, not a promise of maximum settings on every phone.
