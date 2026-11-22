# Baseline experiment report

Date: 2026-11-22

## Reproduction
The current `smarttalk-ai/corpus.txt` (3,721 characters, 52 lines) and the architecture/training loop in `train.py` were reproduced with PyTorch CPU, seed 7, 300 optimizer steps.

Training loss at logged checkpoints:
- step 50: 3.4200
- step 100: 3.1526
- step 150: 2.8757
- step 200: 2.7737
- step 250: 2.6015
- step 300: 2.5276

## Held-out check
The separate `eval.txt` has 441 characters and character coverage was 100% for this tiny sample. Mean chunk cross-entropy: 2.4698; perplexity: 11.82.

Perplexity is next-character prediction quality, not answer accuracy.

## Qualitative generation
Prompt: `প্রশ্ন: বাংলাদেশের রাজধানী কী? উত্তর:`

Greedy sample from this reproduced run:
`প্রশ্ন: বাংলাদেশের রাজধানী কী? উত্তর: কর কর ক্য কা কর কার কর কর কার করে ক্ত্র ক্ত কর কর ক্র কর কর কর কর কর কর কর কর ক`

This is not a correct answer. The model fails the basic held-out question-answer generation test.

## Decision
**FAIL — do not integrate into Android.** The model is a tiny randomly initialized character-level experiment trained on only 52 short lines. Lower training loss does not mean useful Bengali comprehension. Expand the corpus substantially with documented rights/provenance, improve tokenizer/model/data pipeline, and evaluate on a larger independently authored benchmark before mobile integration.

Note: these are results from a local reproduction of the committed training recipe and corpus, not a claim that a GitHub Actions artifact was retrieved or that its run completed.
