import argparse
import json
import math
import os
import random
from pathlib import Path

import torch
import torch.nn as nn
import torch.nn.functional as F

ROOT = Path(__file__).resolve().parent
CONFIG_PATH = ROOT / "config.json"
DATA_DIR = ROOT / "data" / "corpus"
OUT_DIR = ROOT / "outputs"


def load_config(smoke=False):
    cfg = json.loads(CONFIG_PATH.read_text(encoding="utf-8"))
    if smoke:
        cfg.update(context_length=64, d_model=128, n_layers=2, n_heads=4, batch_size=2, dropout=0.0)
    return cfg


class CausalBlock(nn.Module):
    def __init__(self, d_model, n_heads, dropout):
        super().__init__()
        self.ln1 = nn.LayerNorm(d_model)
        self.attn = nn.MultiheadAttention(d_model, n_heads, dropout=dropout, batch_first=True)
        self.ln2 = nn.LayerNorm(d_model)
        self.ff = nn.Sequential(
            nn.Linear(d_model, 4 * d_model),
            nn.GELU(),
            nn.Linear(4 * d_model, d_model),
            nn.Dropout(dropout),
        )

    def forward(self, x):
        n = x.size(1)
        # True entries are masked by PyTorch MultiheadAttention.
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
        self.blocks = nn.ModuleList([
            CausalBlock(d, cfg["n_heads"], cfg["dropout"])
            for _ in range(cfg["n_layers"])
        ])
        self.ln_f = nn.LayerNorm(d)
        self.lm_head = nn.Linear(d, cfg["vocab_size"], bias=False)
        self.lm_head.weight = self.token_emb.weight
        self.apply(self._init_weights)

    @staticmethod
    def _init_weights(module):
        if isinstance(module, (nn.Linear, nn.Embedding)):
            nn.init.normal_(module.weight, mean=0.0, std=0.02)
            if isinstance(module, nn.Linear) and module.bias is not None:
                nn.init.zeros_(module.bias)

    def forward(self, idx, targets=None):
        b, t = idx.shape
        if t > self.context_length:
            raise ValueError(f"sequence length {t} exceeds context {self.context_length}")
        pos = torch.arange(t, device=idx.device)
        x = self.drop(self.token_emb(idx) + self.pos_emb(pos)[None, :, :])
        for block in self.blocks:
            x = block(x)
        logits = self.lm_head(self.ln_f(x))
        loss = None
        if targets is not None:
            loss = F.cross_entropy(logits.reshape(-1, logits.size(-1)), targets.reshape(-1))
        return logits, loss


def count_params(model):
    return sum(p.numel() for p in model.parameters() if p.requires_grad)


def read_corpus():
    paths = sorted(DATA_DIR.glob("*.txt"))
    if not paths:
        raise FileNotFoundError(f"No .txt corpus files found in {DATA_DIR}")
    chunks = [p.read_text(encoding="utf-8", errors="replace") for p in paths]
    raw = "\n\n".join(chunks).encode("utf-8")
    if len(raw) < 1000:
        raise ValueError(f"Corpus has only {len(raw)} bytes; add substantially more UTF-8 text.")
    return torch.tensor(list(raw), dtype=torch.long)


def get_batch(data, batch_size, context, device):
    max_start = len(data) - context - 1
    starts = torch.randint(0, max_start, (batch_size,))
    x = torch.stack([data[i:i + context] for i in starts])
    y = torch.stack([data[i + 1:i + context + 1] for i in starts])
    return x.to(device), y.to(device)


@torch.no_grad()
def estimate_loss(model, train_data, val_data, cfg, device, batches=8):
    model.eval()
    out = {}
    for name, data in (("train", train_data), ("val", val_data)):
        losses = []
        for _ in range(batches):
            x, y = get_batch(data, cfg["batch_size"], cfg["context_length"], device)
            _, loss = model(x, y)
            losses.append(loss.item())
        out[name] = sum(losses) / len(losses)
    model.train()
    return out


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--steps", type=int, default=500)
    parser.add_argument("--batch-size", type=int, default=None)
    parser.add_argument("--count-params", action="store_true")
    parser.add_argument("--save-every", type=int, default=100)
    args = parser.parse_args()

    smoke = os.getenv("FREEMIND_SMOKE") == "1"
    cfg = load_config(smoke)
    if args.batch_size:
        cfg["batch_size"] = args.batch_size

    random.seed(cfg["seed"])
    torch.manual_seed(cfg["seed"])
    device = "cuda" if torch.cuda.is_available() else "cpu"
    model = FreeMindGPT(cfg).to(device)
    nparams = count_params(model)
    print(f"device={device} trainable_parameters={nparams:,} config={cfg}", flush=True)
    if args.count_params:
        return

    data = read_corpus()
    split = int(len(data) * 0.9)
    train_data, val_data = data[:split], data[split:]
    minimum = cfg["context_length"] + 2
    if len(val_data) < minimum:
        raise ValueError("Validation split too small; add more corpus text.")

    optimizer = torch.optim.AdamW(
        model.parameters(),
        lr=cfg["learning_rate"],
        weight_decay=cfg["weight_decay"],
    )
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    model.train()
    for step in range(1, args.steps + 1):
        x, y = get_batch(train_data, cfg["batch_size"], cfg["context_length"], device)
        _, loss = model(x, y)
        optimizer.zero_grad(set_to_none=True)
        loss.backward()
        torch.nn.utils.clip_grad_norm_(model.parameters(), 1.0)
        optimizer.step()

        if step == 1 or step % 25 == 0 or step == args.steps:
            metrics = estimate_loss(model, train_data, val_data, cfg, device, batches=4)
            print(
                f"step={step}/{args.steps} train_loss={metrics['train']:.4f} "
                f"val_loss={metrics['val']:.4f}",
                flush=True,
            )

        if step % args.save_every == 0 or step == args.steps:
            path = OUT_DIR / "freemind_scratch.pt"
            torch.save({
                "model_state_dict": model.state_dict(),
                "config": cfg,
                "step": step,
                "trainable_parameters": nparams,
                "note": "Random-init byte-level GPT; quality depends entirely on corpus and compute.",
            }, path)
            print(f"saved={path}", flush=True)


if __name__ == "__main__":
    main()
