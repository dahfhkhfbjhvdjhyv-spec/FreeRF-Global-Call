#!/usr/bin/env python3
"""Build weakly supervised Bengali instruction pairs from article leads."""
import argparse
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent
SOURCE = ROOT / "data/wikipedia_bn/bn_wikipedia.txt"
OUTPUT = ROOT / "data/instruction_bn/wikipedia_leads.jsonl"

def clean(text):
    text = re.sub(r"<[^>]+>", " ", text)
    text = re.sub(r"\{\{.*?\}\}", " ", text, flags=re.S)
    text = re.sub(r"\[\[(?:[^]|]+\|)?([^\]]+)\]\]", r"\1", text)
    text = re.sub(r"\[([^ ]+)\]", r"\1", text)
    text = re.sub(r"\s+", " ", text)
    return text.strip()

def first_sentences(text):
    text = clean(text)
    # Keep a short lead rather than feeding entire articles as answers.
    parts = re.split(r"(?<=[।!?])\s+", text)
    lead = " ".join(parts[:2]).strip()
    return lead[:420]

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--min-examples", type=int, default=10000)
    args = ap.parse_args()
    if not SOURCE.exists():
        raise FileNotFoundError(f"Missing {SOURCE}; run Wikipedia preparation first")
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    count = 0
    with SOURCE.open(encoding="utf-8") as src, OUTPUT.open("w", encoding="utf-8") as dst:
        title = None
        body = []
        def emit():
            nonlocal count
            if not title:
                return
            answer = first_sentences(" ".join(body))
            if len(title) < 2 or len(answer) < 35:
                return
            # Avoid malformed or excessively long prompts.
            short_title = clean(title)[:40]
            prompt = f"প্রশ্ন: {short_title} সম্পর্কে সংক্ষেপে বলো।\nউত্তর:"
            row = {"prompt": prompt, "answer": " " + answer}
            dst.write(json.dumps(row, ensure_ascii=False) + "\n")
            count += 1
        for line in src:
            if line.strip() == "<|article|>":
                emit()
                title, body = None, []
                continue
            if title is None:
                candidate = line.strip()
                if candidate:
                    title = candidate
            else:
                body.append(line.strip())
        emit()
    print(f"instruction_examples={count:,} output={OUTPUT} bytes={OUTPUT.stat().st_size:,}", flush=True)
    if count < args.min_examples:
        raise RuntimeError(f"Too few usable examples: {count:,} < {args.min_examples:,}")
    print("NOTICE: These are automatically generated weak-supervision pairs, not human-verified QA.", flush=True)

if __name__ == "__main__":
    main()
