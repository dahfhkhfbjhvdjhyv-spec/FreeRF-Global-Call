# SDXS Tiny AI — Android Text-to-Image

A dedicated Android text-to-image app. No photo editor, video editor, cloud inference, accounts, or ads.

## Model

Target model: **SDXS-512 Tiny Distilled Q8_0 GGUF** (about 683 MB), hosted by [concedo on Hugging Face](https://huggingface.co/concedo/sdxs-512-tinySDdistilled-GGUF). Download the model file from the repository's Files tab and select it in the app's model picker. Model inference is intended to run locally through the Android native stable-diffusion.cpp runtime.

- Expected output: 512 × 512
- One-step distilled model
- First model download requires internet; after it is stored locally, generation can run offline.
- Keep at least several GB of free storage and RAM. Performance depends strongly on the phone; low-memory devices may close the app.
- The GGUF model page identifies the model as OpenRAIL++ and ~683 MB. Review and comply with the model license and its use restrictions before redistribution or commercial use.

## Build status and honesty

This branch packages an Android app shell based on the MIT-licensed [sdcpp-on-android](https://github.com/akleine/sdcpp-on-android) project and its stable-diffusion.cpp native runtime. The GitHub Actions workflow builds a debug APK and publishes it as an artifact.

**Important:** the APK build is not proof that SDXS Tiny inference works on every Android phone. The model is selected/imported by the user; this branch does not bundle the 683 MB model inside the APK. The first prototype currently relies on the upstream file picker; a built-in one-tap model downloader is still a follow-up task. A real device test with the exact GGUF file is required before calling the model integration verified. If the runtime rejects the GGUF architecture, the native backend must be updated to a compatible stable-diffusion.cpp revision.

## App scope

- Text prompt → generated image
- 512 × 512 target output
- Save generated image
- Local model selection
- No photo editing or video editing tools
- No ads or cloud image APIs

## Upstream attribution

- Android UI/runtime base: [akleine/sdcpp-on-android](https://github.com/akleine/sdcpp-on-android), MIT
- Native engine: [stable-diffusion.cpp](https://github.com/leejet/stable-diffusion.cpp)
- Model: [concedo/sdxs-512-tinySDdistilled-GGUF](https://huggingface.co/concedo/sdxs-512-tinySDdistilled-GGUF)
- SDXS research: [SDXS paper](https://arxiv.org/abs/2403.16627)

The model is not included in this source repository or APK artifact.
