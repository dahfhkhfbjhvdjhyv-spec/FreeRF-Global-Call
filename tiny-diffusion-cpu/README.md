# TinyDiffusion CPU — Android Text-to-Image

A small, dedicated Android text-to-image app prototype. The app has no photo editor, video editor, cloud inference, ads, or account requirement.

## CPU-first target

- Android arm64-v8a
- CPU backend only (OpenCL is disabled)
- 512 × 512 text-to-image target
- Local inference after the model file is downloaded
- Images saved locally
- English and Bengali prompts can be entered as text; prompt understanding depends on the selected model/tokenizer

## Model

Initial compact-model candidate: **SDXS-512 Tiny distilled GGUF**, from https://huggingface.co/concedo/sdxs-512-tinySDdistilled-GGUF (approximately 683 MB as listed by the model publisher). It is a one-step distilled image model, not Google's MobileDiffusion.

The APK does not bundle the model. Download a compatible model file separately and select it in the app. Exact model/runtime compatibility must be confirmed by a real generation test on the target phone. If the runtime rejects the GGUF model architecture, the model cannot generate until the native runtime is updated or a supported model variant is substituted.

## Low-memory warning

The phone described by the user has 8 GB total RAM and around 2 GB currently free. That is not a guarantee of successful generation: the Android OS, model weights, text encoder, image decoder, and temporary tensors all need memory. Close background apps before trying. CPU generation may be slow and may fail with an out-of-memory error on this device. No performance claim is made before a physical-device test.

## Build

GitHub Actions builds a debug APK from the MIT-licensed Android T2I shell at https://github.com/akleine/sdcpp-on-android, pinned to commit `98141505007657192d3aa37584079c1ac476761b`. The upstream project uses stable-diffusion.cpp and documents an Android native build with `SD_OPENCL=OFF`; this workflow keeps the CPU path and does not bundle OpenCL.

Build success only confirms APK compilation. It does not prove that the selected model successfully generates images on a particular phone.

## Upstream / license

- Android app shell: https://github.com/akleine/sdcpp-on-android (MIT)
- Native inference engine: https://github.com/leejet/stable-diffusion.cpp
- Candidate model: https://huggingface.co/concedo/sdxs-512-tinySDdistilled-GGUF

Check and follow each upstream/model license before redistribution.
