# FreeMind AI v0.4 — experimental Bengali/English model

This folder is a reproducible **small-model training experiment**, not a finished assistant and not a copy of GPT-6.

## What is included
- A bilingual seed dataset in `data/seed.jsonl`
- A CPU-compatible character-level GRU encoder/decoder training script
- Validation-loss tracking and a small interactive terminal chat mode
- A manually triggered GitHub Actions smoke-training workflow

## Run locally or in a notebook
Python 3.10+ is recommended.

```bash
python -m pip install -r requirements.txt
python train.py --epochs 30
python train.py --chat
```

The checkpoint is written to `artifacts/freemind_gru.pt`.

## Dataset and quality notes
The seed set is deliberately small and mostly instructional/conversational. It is suitable for checking that the pipeline works, **not** for building a competitive general-purpose model. Arithmetic and templated examples can cause memorization. Add licensed, reviewed, diverse Bengali and English data, remove duplicates, and maintain a held-out test set before drawing quality conclusions.

## GitHub Actions
Open **Actions → FreeMind CPU smoke training → Run workflow**. GitHub-hosted runners are temporary CI machines with usage limits; they are not persistent AI servers and should not be treated as a free, always-on inference backend. The workflow only smoke-tests the training pipeline and does not create an Android app or deploy an AI service.

## Important
Training a model here does not change GPT-6's weights. The generated checkpoint is a separate experimental model. No claim of model quality should be made from training loss alone; test with unseen prompts and human review.
