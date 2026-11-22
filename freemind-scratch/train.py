import argparse
import json
import os
import random
from pathlib import Path

import torch
import torch.nn as nn
import torch.nn.functional as F

ROOT = Path(__file__).resolve().parent
CONFIG_PATH = ROOT / "config.json"
DATA_DIR = ROOT / "data"
OUT_DIR = ROOT / "outputs"


def load_config():
    return json.loads(CONFIG_PATH.read_text(encoding="utf-8"))


class CausalBlock(nn.Module):
    def __init__(self, d_model, n_heads, dropout):
        super().__init__()
        self.ln1 = nn.LayerNorm(d_model)
        self.attn = nn.MultiheadAttention(d_model, n_heads, dropout=dropout, batch_first=True)
        self.ln2 = nn.LayerNorm(d_model)
        self.ff = nn.Sequential(
            nn.Linear(d_model, 4 * d_model), nn.GELU(),
            nn.Linear(4 * d_model, d_model), nn.Dropout(dropout)
        )

    def forward(self, x):
        n = x.size(1)
        mask = torch.triu(torch.ones(n, n, device=x.device, dtype=torch.bool), diagonal=1)
        h = self.ln1(x)
        a, _ = self.attn(h, h, h, attn_mask=mask, need_weights=False)
        x = x + a
        return x + self.ff(self.ln2(x))


class FreeMindGPT(nn.Module):
    def __init__(self, cfg):
        super().__init__()
        d = cfg["d_model"]
        self.context_length = cfg["context_length"]
        self.token_emb = nn.Embedding(cfg["vocab_size"], d)
        self.pos_emb = nn.Embedding(cfg["context_length"], d)
        self.drop = nn.Dropout(cfg["dropout"])
        self.blocks = nn.ModuleList([CausalBlock(d, cfg["n_heads"], cfg["dropout"]) for _ in range(cfg["n_layers"])])
        self.ln_f = nn.LayerNorm(d)
        self.lm_head = nn.Linear(d, cfg["vocab_size"], bias=False)
        self.lm_head.weight = self.token_emb.weight
        self.apply(self._init_weights)

    @staticmethod
    def _init_weights(m):
        if isinstance(m, (nn.Linear, nn.Embedding)):
            nn.init.normal_(m.weight, mean=0.0, std=0.02)
            if isinstance(m, nn.Linear) and m.bias is not None:
                nn.init.zeros_(m.bias)

    def forward(self, idx, targets=None):
        b, t = idx.shape
        if t > self.context_length:
            raise ValueError("Sequence exceeds configured context length")
        pos = torch.arange(t, device=idx.device)
        x = self.drop(self.token_emb(idx) + self.pos_emb(pos)[None, :, :])
        for block in self.blocks:
            x = block(x)
        logits = self.lm_head(self.ln_f(x))
        loss = F.cross_entropy(logits.reshape(-1, logits.size(-1)), targets.reshape(-1)) if targets is not None else None
        return logits, loss


def read_corpus():
    # Use the complete Wikipedia text file as a uint8 memory-mapped tensor.
    # This avoids expanding ~1 GB of UTF-8 bytes into an 8 GB int64 tensor.
    candidates = sorted((DATA_DIR / "wikipedia_bn").glob("bn_wikipedia.txt"))
    if not candidates:
        raise FileNotFoundError("Complete Bengali Wikipedia corpus missing. Run prepare_wikipedia_bn.py first.")
    path = candidates[0]
    size = path.stat().st_size
    if size < 100_000_000:
        raise ValueError(f"Wikipedia corpus unexpectedly small: {size:,} bytes")
    data = torch.from_file(str(path), shared=False, size=size, dtype=torch.uint8)
    print(f"corpus_file={path} corpus_bytes={size:,} memory_dtype=uint8", flush=True)
    return data


def get_batch(data, batch_size, context, device):
    max_start = len(data) - context - 1
    starts = torch.randint(0, max_start, (batch_size,))
    x = torch.stack([data[i:i + context] for i in starts]).to(device=device, dtype=torch.long)
    y = torch.stack([data[i + 1:i + context + 1] for i in starts]).to(device=device, dtype=torch.long)
    return x, y


@torch.no_grad()
def estimate_loss(model, train_data, val_data, cfg, device, batches=8):
    model.eval()
    result = {}
    for name, data in (("train", train_data), ("validation", val_data)):
        losses = []
        for _ in range(batches):
            x, y = get_batch(data, cfg["batch_size"], cfg["context_length"], device)
            _, loss = model(x, y)
            losses.append(loss.item())
        result[name] = sum(losses) / len(losses)
    model.train()
    return result


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--steps", type=int, default=10000)
    parser.add_argument("--save-every", type=int, default=500)
    args = parser.parse_args()

    cfg = load_config()
    random.seed(cfg["seed"])
    torch.manual_seed(cfg["seed"])
    device = "cuda" if torch.cuda.is_available() else "cpu"
    if device == "cpu":
        torch.set_num_threads(int(os.getenv("OMP_NUM_THREADS", "4")))
    model = FreeMindGPT(cfg).to(device)
    params = sum(p.numel() for p in model.parameters() if p.requires_grad)
    print(f"device={device} trainable_parameters={params:,} config={cfg}", flush=True)

    data = read_corpus()
    split = int(len(data) * 0.98)
    train_data, val_data = data[:split], data[split:]
    if len(val_data) <= cfg["context_length"] + 2:
        raise ValueError("Validation split too small")

    optimizer = torch.optim.AdamW(model.parameters(), lr=cfg["learning_rate"], weight_decay=cfg["weight_decay"])
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    model.train()
    for step in range(1, args.steps + 1):
        x, y = get_batch(train_data, cfg["batch_size"], cfg["context_length"], device)
        _, loss = model(x, y)
        optimizer.zero_grad(set_to_none=True)
        loss.backward()
        torch.nn.utils.clip_grad_norm_(model.parameters(), 1.0)
        optimizer.step()

        if step == 1 or step % 100 == 0:
            metrics = estimate_loss(model, train_data, val_data, cfg, device, batches=4)
            print(f"step={step}/{args.steps} train_loss={metrics['train']:.4f} validation_loss={metrics['validation']:.4f}", flush=True)

        if step % args.save_every == 0 or step == args.steps:
            path = OUT_DIR / "freemind_bn_wikipedia.pt"
            torch.save({
                "model_state_dict": model.state_dict(), "config": cfg,
                "step": step, "trainable_parameters": params,
                "corpus": "Complete Bengali FineWiki bn subset; UTF-8 byte-level",
                "corpus_bytes": len(data), "validation_fraction": 0.02,
                "note": "From-scratch language-model pretraining; not instruction/chat fine-tuning."
            }, path)
            print(f"checkpoint_saved={path}", flush=True)


if __name__ == "__main__":
    main()
