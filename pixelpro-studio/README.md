# PixelPro Studio

Android-first photo and video editor. Package: `com.pixelpro.studio`.

## Confirmed product decisions
- AI mode: **on-device/local only**; no cloud inference or required account.
- UI: Bangla + English (localization rollout is planned; current prototype contains mixed Bangla/English strings).
- Photo target: Pro quality.
- Offline editing, ad-free product, and 4K export target.

## Current implementation (prototype)
- Kotlin + Jetpack Compose Android shell with original PixelPro branding.
- Android Photo Picker for still images and up to 20 video clips.
- Classical, non-AI photo pipeline: high-quality resampling, 3x3 bilateral denoise, luminance sharpening/detail, local contrast and color finishing.
- Saves enhanced JPEG copies to `Pictures/PixelProStudio`.
- Video clip selection list only; concatenation/rendering/export are **not implemented**.

## Important quality limits
The current photo engine is a conventional image-processing baseline, not an AI restoration model. It cannot reconstruct details that were never captured. 4K export is a target and must be gated by available memory, codec and device capability. It must not be presented as a verified feature until a real 4K output has been generated and tested on devices.

## Local AI strategy
See [LOCAL_AI_PLAN.md](LOCAL_AI_PLAN.md). The project will use an appropriately licensed pretrained on-device model for inference; training a high-quality restoration model from scratch on a phone is not a realistic first milestone. Model files must be versioned, license-reviewed, checksummed, and tested on supported Android hardware before the UI advertises the feature as available.

## Build
Requires JDK 17 and Android SDK 35. From this directory run `gradle assembleDebug`.
GitHub Actions workflow: `.github/workflows/pixelpro-android.yml`.

## Roadmap
1. Stabilize and test the Android shell and baseline photo processing.
2. Add bounded-memory image pipeline, before/after comparison, and real 4K photo export with capability checks.
3. Integrate a licensed local photo restoration/upscaling model and benchmark latency/RAM.
4. Build a real multi-track video timeline and offline Media3 rendering pipeline.
5. Add local AI features only where model quality/performance is acceptable; keep unavailable features clearly marked.
6. Complete Bangla/English resources, accessibility, device QA, and release signing.
