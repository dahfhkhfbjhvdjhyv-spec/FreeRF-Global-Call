# FreeMind Scratch — from-zero language-model experiment

This branch adds a **decoder-only Transformer initialized from random weights**. It does not download or reuse pretrained model weights.

## Current target profile

- Byte-level UTF-8 tokenizer (256 byte values; multilingual text needs no external tokenizer training)
- 8 Transformer blocks, width 384, 6 attention heads, context length 256
- Approximately 14.4 million parameters (verify with `python train.py --count-params`)
- CPU-compatible PyTorch implementation; CUDA is used automatically if available
- Checkpoints saved to `outputs/`

This is the largest conservative profile selected for the previously reported CPU-only environment with roughly 5.8 GiB RAM. It is **not** a 1.5B model. If a GPU or more RAM becomes available, change `config.json` after checking actual free memory.

## Important quality limitation

A model trained from scratch learns language only from the text it sees. The included `data/corpus/seed.txt` is a tiny pipeline-test corpus, not a serious pretraining dataset. Training on it will overfit and will not create a useful assistant. A meaningful language model needs a large, legally usable, deduplicated Bengali-English corpus and substantial compute. The smoke workflow only checks that code runs; it is not model training at useful scale.

## Run locally / on a cloud GPU

```bash
python -m pip install -r requirements.txt
python train.py --count-params
python train.py --steps 5000 --batch-size 8
```

Put UTF-8 `.txt` files in `data/corpus/`. Do not include private data or copyrighted material without permission. The trainer concatenates text files and creates random next-byte prediction windows.

For a short pipeline check:

```bash
FREEMIND_SMOKE=1 python train.py --steps 8 --batch-size 2
```

The smoke profile intentionally uses a much smaller network. It is not the 14.4M target profile.

## Checkpoint contents

`outputs/freemind_scratch.pt` contains the randomly initialized-and-trained model state, model configuration, step count, and validation loss. A checkpoint is not proof of good conversational quality; evaluate it on held-out text and task-specific benchmarks before calling it usable.

## License and data

Code: MIT. Dataset licensing remains the responsibility of whoever adds corpus files. Keep source/license records alongside any external corpus.
