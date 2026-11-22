#!/usr/bin/env python3
"""Stream the complete Bengali FineWiki subset into a UTF-8 corpus."""
import json
import re
from pathlib import Path
from datasets import load_dataset

OUT = Path("freemind-scratch/data/wikipedia_bn")
OUT.mkdir(parents=True, exist_ok=True)
corpus_path = OUT / "bn_wikipedia.txt"
meta_path = OUT / "dataset_manifest.json"

# FineWiki's bn subset is the Bengali Wikipedia snapshot (Aug 2025).
EXPECTED_PAGES = 187386
ds = load_dataset("HuggingFaceFW/finewiki", "bn", split="train", streaming=True)

count = 0
characters = 0
with corpus_path.open("w", encoding="utf-8", newline="\n") as out:
    for row in ds:
        title = str(row.get("title") or "").strip()
        body = str(row.get("text") or "").strip()
        if not body:
            continue
        # Keep article boundaries and titles; do not silently truncate articles.
        out.write("\n\n<|article|>\n")
        if title:
            out.write(title + "\n")
        out.write(body)
        out.write("\n")
        count += 1
        characters += len(title) + len(body)
        if count % 5000 == 0:
            print(f"articles={count:,} characters={characters:,}", flush=True)

manifest = {
    "dataset": "HuggingFaceFW/finewiki",
    "configuration": "bn",
    "split": "train",
    "snapshot": "August 2025",
    "expected_articles_from_dataset_card": EXPECTED_PAGES,
    "articles_written": count,
    "characters_written": characters,
    "corpus_file": str(corpus_path),
    "license": "CC-BY-SA-4.0 (Wikipedia source; attribution/share-alike obligations apply)",
    "source": "https://huggingface.co/datasets/HuggingFaceFW/finewiki",
}
meta_path.write_text(json.dumps(manifest, ensure_ascii=False, indent=2), encoding="utf-8")
print(json.dumps(manifest, ensure_ascii=False, indent=2), flush=True)
if count != EXPECTED_PAGES:
    raise RuntimeError(f"Incomplete Bengali Wikipedia extraction: expected {EXPECTED_PAGES}, wrote {count}")
