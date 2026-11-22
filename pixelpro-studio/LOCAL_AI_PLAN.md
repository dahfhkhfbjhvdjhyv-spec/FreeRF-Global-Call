# PixelPro Studio — Local AI Implementation Plan

## Product constraints
- Inference must run on the Android device. No cloud inference endpoint, API key, or upload of user photos/videos.
- Offline editing remains available even when no AI model is installed or the device is too weak.
- Ad-free means no advertising SDK, ad network, or ad slot.
- 4K is a capability-gated export target, not a promise that every device can process every source at 4K.

## Model integration gates
1. Select a photo restoration/super-resolution model with explicit commercial redistribution rights and a documented model card.
2. Verify Android runtime support, operator coverage, model input/output dimensions, color space, quantization, and license.
3. Package or download the model through an explicit optional offline model pack. If downloaded, use a user-visible download, SHA-256 verification, and storage estimate; never upload user media.
4. Run inference on-device using supported acceleration (NNAPI/GPU where stable, CPU fallback). Keep a deterministic non-AI fallback.
5. Benchmark on low-, mid-, and high-tier devices: peak memory, cold/warm latency, battery/thermal behavior, output dimensions, and visual artifacts.
6. Expose AI controls only after the model and device pass the gates. Show model name/version and an honest processing state.

## First local AI milestone
Still-photo restoration/upscaling only. Start with one model and one well-defined task; do not bundle a large collection of unverified models. Do not claim face identity reconstruction or recovered factual detail. Preserve original files and save results separately.

## Video milestone
Offline timeline and standard effects first. Local AI video generation is a separate, much heavier research milestone; do not promise it on ordinary Android hardware until a model is licensed and real device benchmarks prove it feasible.

## 4K safety and memory
- Estimate decoded bitmap and intermediate tensor memory before processing.
- Use tiled/overlapped inference or bounded-resolution processing where supported.
- Check encoder support and available storage before export.
- Offer 1080p fallback and cancel/resume-safe export behavior.
- Test output playback, audio sync, orientation, color and metadata on physical devices.

## Release acceptance
- Airplane-mode test confirms editing and local inference work offline.
- Network inspection confirms no media upload or inference calls.
- No advertising SDK or ad placements.
- Verify 4K output on a named test device and codec; report device limitations accurately.
- Run automated tests and physical-device install/export tests before release.
