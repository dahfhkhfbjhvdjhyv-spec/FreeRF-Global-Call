# SmartTalk AI — Bengali model research

This is a separate project area on the `smarttalk-ai` branch. It does not modify the FreeRF Android app.

## Phase 1: from-scratch baseline
A small character-level causal Transformer is initialized with random weights and trained on the checked-in Bengali starter corpus. It is a research/demo baseline, not a human-level assistant. The tiny corpus is not sufficient for broad factual knowledge or 95% accuracy.

## Run locally
```bash
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
python train.py --steps 300 --out model-output
python generate.py --checkpoint model-output/model.pt --prompt "বাংলা ভাষা"
```

## GitHub Actions
Pushing changes under `smarttalk-ai/` runs the CPU training experiment and stores the checkpoint as a workflow artifact. Training may take several minutes. It does not automatically prove model quality.

## Next phases
1. Expand and license-review a large Bengali corpus.
2. Add held-out evaluation for grammar, comprehension, factuality and instruction following.
3. Improve tokenizer/model architecture and train with adequate compute.
4. Convert a verified checkpoint to a mobile runtime format and integrate on-device inference into Android.
5. Build and test the APK on real Android devices.

Never claim 95% accuracy without a published, reproducible evaluation set and measured results.
