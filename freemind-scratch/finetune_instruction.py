#!/usr/bin/env python3
"""Answer-only supervised fine-tuning for the byte-level FreeMind checkpoint."""
import argparse
import json
import random
from pathlib import Path

import torch
import torch.nn.functional as F
from train import FreeMindGPT, load_config

ROOT = Path(__file__).resolve().parent
DATA = ROOT / "data/instruction_bn/wikipedia_leads.jsonl"
PRETRAINED = ROOT / "outputs/freemind_bn_wikipedia.pt"
OUT = ROOT / "outputs/freemind_bn_instruction.pt"

def load_examples(context):
    rows = []
    with DATA.open(encoding="utf-8") as f:
        for line in f:
            row = json.loads(line)
            p = row["prompt"].encode("utf-8")
            a = row["answer"].encode("utf-8")
            # Keep the prompt intact; shorten answer to fit the context.
            if len(p) >= context - 8:
                p = p[-(context - 80):]
            a = a[: max(1, context - len(p) - 1)]
            ids = list(p + a)
            if len(ids) >= 16:
                rows.append((ids, len(p)))
    if not rows:
        raise RuntimeError("No usable instruction examples")
    return rows

def make_batch(rows, batch_size, context, device):
    chosen = random.choices(rows, k=batch_size)
    x = torch.zeros((batch_size, context), dtype=torch.long)
    y = torch.full((batch_size, context), -100, dtype=torch.long)
    for i, (ids, prompt_len) in enumerate(chosen):
        n = min(len(ids) - 1, context)
        x[i, :n] = torch.tensor(ids[:n], dtype=torch.long)
        targets = ids[1:n + 1]
        # The first answer byte is predicted at position prompt_len - 1.
        answer_start = max(0, prompt_len - 1)
        if answer_start < len(targets):
            y[i, answer_start:n] = torch.tensor(targets[answer_start:], dtype=torch.long)
    return x.to(device), y.to(device)

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--steps", type=int, default=1500)
    ap.add_argument("--batch-size", type=int, default=8)
    args = ap.parse_args()
    cfg = load_config()
    random.seed(cfg["seed"] + 1)
    torch.manual_seed(cfg["seed"] + 1)
    device = "cuda" if torch.cuda.is_available() else "cpu"
    if device == "cpu":
        torch.set_num_threads(4)
    if not PRETRAINED.exists():
        raise FileNotFoundError(f"Missing pretrained checkpoint: {PRETRAINED}")
    checkpoint = torch.load(PRETRAINED, map_location="cpu", weights_only=False)
    model = FreeMindGPT(cfg).to(device)
    model.load_state_dict(checkpoint["model_state_dict"])
    rows = load_examples(cfg["context_length"])
    print(f"loaded_pretraining_step={checkpoint.get('step')} examples={len(rows):,} device={device}", flush=True)
    optimizer = torch.optim.AdamW(model.parameters(), lr=cfg["learning_rate"] * 0.15, weight_decay=0.01)
    model.train()
    for step in range(1, args.steps + 1):
        x, y = make_batch(rows, args.batch_size, cfg["context_length"], device)
        logits, _ = model(x)
        loss = F.cross_entropy(logits.reshape(-1, 256), y.reshape(-1), ignore_index=-100)
        optimizer.zero_grad(set_to_none=True)
        loss.backward()
        torch.nn.utils.clip_grad_norm_(model.parameters(), 1.0)
        optimizer.step()
        if step == 1 or step % 100 == 0:
            print(f"instruction_step={step}/{args.steps} answer_loss={loss.item():.4f}", flush=True)
    OUT.parent.mkdir(parents=True, exist_ok=True)
    torch.save({
        "model_state_dict": model.state_dict(), "config": cfg,
        "pretraining_step": checkpoint.get("step"),
        "instruction_steps": args.steps,
        "trainable_parameters": sum(p.numel() for p in model.parameters()),
        "training_examples": len(rows),
        "dataset": "Automatically generated Bengali Wikipedia title/lead weak-supervision pairs",
        "note": "Experimental SFT; examples are not human-verified and do not establish factual QA quality."
    }, OUT)
    print(f"instruction_checkpoint_saved={OUT} bytes={OUT.stat().st_size:,}", flush=True)

if __name__ == "__main__":
    main()
