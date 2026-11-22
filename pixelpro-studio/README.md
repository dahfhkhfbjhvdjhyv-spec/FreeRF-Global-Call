# PixelPro Studio — Android Photo & Video Editor

## Photo Enhancement Engine (non-AI)
Implemented in `app/src/main/java/com/pixelpro/studio/PhotoEnhancementEngine.kt`.

Pipeline:
1. High-quality bitmap resampling to a 1920-pixel long edge.
2. Optional 3x3 median denoise passes.
3. Unsharp-mask sharpening.
4. Contrast and saturation tuning.
5. Save a separate JPEG copy under `Pictures/PixelProStudio`.

This is classical image processing and runs locally on Android. It does not call a model, cloud API, or AI service. It can improve mild softness/noise and make edges appear clearer, but cannot recover detail absent from the original capture. Strong sharpening can create halos; aggressive denoise can erase texture. The initial preset is deliberately restrained.

## Android starter features
- Kotlin + Jetpack Compose app shell and original launcher icon.
- Android Photo Picker for image selection.
- Non-AI enhancement workflow and output saving.
- Multi-video selection list (timeline prototype).

## Not implemented yet
- Real video concatenation/render/export.
- Clip trimming, transitions, audio mixing, subtitles, stabilization.
- AI super-resolution, generative image restoration, or AI video generation.
- DSLR optical depth-of-field or genuine camera capture quality reconstruction.

## Build
Requires JDK 17 and Android SDK 35. From this directory:
`gradle assembleDebug`

Package: `com.pixelpro.studio` · Min Android: API 26 · Target API: 35
